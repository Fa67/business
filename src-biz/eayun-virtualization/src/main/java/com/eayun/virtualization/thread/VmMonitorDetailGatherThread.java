package com.eayun.virtualization.thread;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.redis.JedisUtil;
import com.eayun.datacenter.model.DcDataCenter;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.service.VmMonitorService;

public class VmMonitorDetailGatherThread implements Runnable {

    private static final Logger     log    = LoggerFactory
                                               .getLogger(VmMonitorDetailGatherThread.class);

    private DcDataCenter            dataCenter;
    private CloudProject            project;
    private MongoTemplate           mongoTemplate;
    private VmMonitorService        vmMonitorService;
    private JedisUtil               jedisUtil;
    private Date   now;

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private static Calendar         cal    = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

    private static String[][]       meters = new String[][] { { "resource_id", "cpu_util" },
            { "resource_id", "memory.usage" }, 
            { "resource_id", "disk.read.bytes.rate" },
            { "resource_id", "disk.write.bytes.rate" },
            { "metadata.instance_id", "network.incoming.bytes.rate" },
            { "metadata.instance_id", "network.outgoing.bytes.rate" } };

    public VmMonitorDetailGatherThread(DcDataCenter dataCenter, CloudProject project, 
                                       MongoTemplate mongoTemplate,
                                       VmMonitorService vmMonitorService, JedisUtil jedisUtil,
                                       Date now) {
        this.dataCenter = dataCenter;
        this.project = project;
        this.mongoTemplate = mongoTemplate;
        this.vmMonitorService = vmMonitorService;
        this.jedisUtil = jedisUtil;
        this.now = now;
        format.setCalendar(cal);
    }

    @Override
    public void run() {
        log.info("开始采集项目【" + project.getPrjName() + "】的指标");
        Date nowTime = new Date(now.getTime() - 1000 * 60);
        Date late = new Date(now.getTime() - 1000 * 120);
        for (String[] meter : meters) {
            JSONArray array = vmMonitorService.getMeter(meter, dataCenter.getId(),
                project.getProjectId() , format.format(nowTime) , format.format(late));
            if (null == array || array.isEmpty()) {
                continue;
            }
            for(int i = 0; i< array.size();i++){
                JSONObject obj = array.getJSONObject(i);
                if (null == obj || obj.isEmpty()) {
                    continue;
                }
                String vmId = obj.getString(meter[0]);
                // 设置时间
                obj.put("timestamp", now);
                
                // 重置数值类型
                Double counter_volume = Double.parseDouble(obj.getString("counter_volume"));
                
                switch (meter[1]) {
                    case "disk.read.bytes.rate":
                    case "disk.write.bytes.rate":
                        obj.put("counter_unit", "MB/s");
                        counter_volume = counter_volume/(1024*1024d);
                        break;
                    case "network.incoming.bytes.rate":
                    case "network.outgoing.bytes.rate":
                        obj.put("counter_unit", "Mb/s");
                        counter_volume = (counter_volume/(1024*1024d))*8;
                        vmId = obj.getJSONObject("resource_metadata").getString("instance_id");
                        break;
                    case "cpu_util":
                    case "memory.usage":
                        obj.put("counter_unit", "%");
                }
                obj.put("counter_volume", counter_volume);
                // 去掉resource_metadata
                obj.remove("resource_metadata");
                // 补充vm_id段，用于查询
                obj.put("vm_id", vmId);

                Double last_volume = 0d;
                // 入库
                obj.put("real_time", new Date());
                try {
                    last_volume = jedisUtil.getDouble("monitor:"+meter[1]+":"+vmId);
                    mongoTemplate.insert(obj, meter[1] + ".detail");
                    
                    BigDecimal db = new BigDecimal(counter_volume);
                    String value = db.toPlainString();
                    
                    BigDecimal dd = new BigDecimal(last_volume);
                    String lastvalue = dd.toPlainString();
                    
                    jedisUtil.set("monitor:"+meter[1]+":"+ vmId, value);
                    jedisUtil.set("monitor:"+meter[1]+":last"+ vmId, lastvalue);
                } catch (Exception e) {
                    log.error(e.getMessage(),e);
                }
            }
            
        }
    }
}
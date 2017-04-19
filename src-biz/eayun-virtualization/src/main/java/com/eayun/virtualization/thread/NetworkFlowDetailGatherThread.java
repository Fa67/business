package com.eayun.virtualization.thread;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.MongoCollectionName;
import com.eayun.datacenter.model.DcDataCenter;
import com.eayun.eayunstack.service.OpenstackMeterService;
import com.eayun.virtualization.model.CloudProject;

/**
 *                       
 * @Filename: NetworkFlowDetailGatherThread.java
 * @Description: 
 * @Version: 1.0
 * @Author: chenhao
 * @Email: hao.chen@eayun.com
 * @History:<br>
 *<li>Date: 2015年12月7日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class NetworkFlowDetailGatherThread implements Runnable {

    private static final Logger     log    = LoggerFactory
                                               .getLogger(NetworkFlowDetailGatherThread.class);

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private static Calendar         cal    = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

    private CloudProject            project;
    private MongoTemplate           mongoTemplate;
    private DcDataCenter            dataCenter;
    private OpenstackMeterService   openstackMeterService;
    private Date   now;

    public NetworkFlowDetailGatherThread(DcDataCenter dataCenter, CloudProject project,
                                         MongoTemplate mongoTemplate,
                                         OpenstackMeterService openstackMeterService,
                                         Date now) {
        this.dataCenter = dataCenter;
        this.project = project;
        this.mongoTemplate = mongoTemplate;
        this.openstackMeterService = openstackMeterService;
        this.now = now;
        format.setCalendar(cal);
    }

    @Override
    public void run() {
        log.info("开始采集项目【" + project.getPrjName() + "】的流量数据");
        Date startTime = new Date(now.getTime() - 1000 * 360);
        Date end = new Date(now.getTime() - 1000 * 300);
        
        String[] labelIds = new String[]{null != project.getLabelInId()?project.getLabelInId():"",
            null != project.getLabelOutId()?project.getLabelOutId():""};//  0:in    1:out
        
        for (int i=0;i<labelIds.length;i++) {
            String labelId = labelIds[i];
            if(labelId.equals("")){
                continue;
            }
            JSONArray array = openstackMeterService.getNetwork(labelId, dataCenter.getId(),project.getProjectId(),
                format.format(startTime), format.format(end));
                if(array == null){
                    continue;
                }
                for(int j = 0;j < array.size();j++){
                    JSONObject obj = array.getJSONObject(j);
                    // 设置时间
                    obj.put("timestamp", now);
                    // 重置数值类型
                    Double volume = Double.parseDouble(obj.getString("counter_volume"));
                    volume = volume/(1024*1024d);
                    obj.put("counter_volume", volume);
                    obj.put("counter_unit", "MB");
                    obj.put("real_time", new Date());
                    // 入库
                    if(i == 0){
                        mongoTemplate.insert(obj, MongoCollectionName.BANDWIDTH_NETWORK_INCOMING_DETAIL);
                    }else if(i==1){
                        mongoTemplate.insert(obj, MongoCollectionName.BANDWIDTH_NETWORK_OUTGOING_DETAIL);
                    }
                }
            }
        }
}

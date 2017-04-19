package com.eayun.database.monitor.thread;

import java.math.BigDecimal;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.redis.JedisUtil;
import com.eayun.database.instance.model.CloudRDSInstance;
import com.eayun.database.monitor.service.InstanceMonitorService;
import com.eayun.virtualization.model.CloudProject;

public class InstanceVolumeDetailGatherThread implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(InstanceVolumeDetailGatherThread.class);
	
    private CloudProject        		project;
    private CloudRDSInstance    		rds;
    private MongoTemplate       		mongoTemplate;
    private InstanceMonitorService    	instanceMonitorService;
    private JedisUtil           		jedisUtil;
    private Date   						now;
    
	public InstanceVolumeDetailGatherThread(CloudProject project,
			CloudRDSInstance rds, MongoTemplate mongoTemplate,
			InstanceMonitorService instanceMonitorService, JedisUtil jedisUtil,
			Date now) {
        this.project = project;
        this.rds = rds;
        this.mongoTemplate = mongoTemplate;
        this.instanceMonitorService = instanceMonitorService;
        this.jedisUtil = jedisUtil;
        this.now = now;
	}

	@Override
	public void run() {
		log.info("开始采集实例【" + rds.getRdsName() + "】的磁盘使用率指标，id："+rds.getRdsId());
		JSONObject result = instanceMonitorService.getInstanceForVolUsed(rds.getDcId(), rds.getPrjId(), rds.getRdsId());
		log.info("实例详情查询："+result);
		if (null != result) {
			JSONObject json = new JSONObject();
			Double used = 0D;
			Double total = 0D;
			JSONObject volume = result.getJSONObject("volume");
			if(null != volume && !volume.isEmpty()){
				if(null != volume.getDouble("total")){
					total = volume.getDouble("total");
				}else{
					log.error("volume_used_ERROR:实例" + rds.getRdsName() + "磁盘总量total获取失败！rdsId:"+rds.getRdsId());
				}
				if(null != volume.getDouble("used")){
					used = volume.getDouble("used");
				}else{
					log.error("volume_used_ERROR:实例" + rds.getRdsName() + "磁盘当前使用量used获取失败！rdsId:"+rds.getRdsId());
				}
			}
			Double volused = 0.0D;
			if(total != 0){
				volused = (used/total)*100.0;
				volused = (Double)(Math.round(volused*10)/10.0);
			}
			json.put("timestamp", now);
			json.put("instance_id", rds.getRdsId());
			json.put("vm_id", rds.getVmId());
			json.put("project_id", project.getProjectId());
			json.put("user_id", project.getCustomerId());
			json.put("counter_unit", "%");
			json.put("counter_name", "volume_used");
			json.put("used", used);
			json.put("total", total);
			json.put("counter_volume", volused);
			json.put("real_time", new Date());
			
			Double last_used = 0D;
			try {
				mongoTemplate.insert(json, MongoCollectionName.MONITOR_VOLUME_USED_DETAIL);
				last_used = jedisUtil.getDouble(RedisKey.MONITOR_VOLUME_USED+rds.getVmId());
				BigDecimal dd = new BigDecimal(last_used);
                String lastvalue = dd.toPlainString();
                
                BigDecimal vu = new BigDecimal(volused);
                String usedvalue = vu.toPlainString();
				
				jedisUtil.set(RedisKey.MONITOR_VOLUME_USED+rds.getVmId(), usedvalue);
                jedisUtil.set(RedisKey.MONITOR_VOLUME_USED_LAST+rds.getVmId(), lastvalue);
			} catch (Exception e) {
				log.error("volume_used_ERROR:实例" + rds.getRdsName() + "磁盘使用率插入数据失败！",e);
			}
        }
	}

}

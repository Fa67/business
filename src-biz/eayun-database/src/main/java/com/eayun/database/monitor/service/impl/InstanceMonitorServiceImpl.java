package com.eayun.database.monitor.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.util.DateUtil;
import com.eayun.database.instance.model.CloudRDSInstance;
import com.eayun.database.monitor.service.InstanceMonitorService;
import com.eayun.eayunstack.service.impl.OpenstackBaseServiceImpl;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;

@SuppressWarnings("rawtypes")
@Service
public class InstanceMonitorServiceImpl extends OpenstackBaseServiceImpl implements InstanceMonitorService {

	private static final Logger log = LoggerFactory.getLogger(InstanceMonitorServiceImpl.class);
	
	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	
	/**
	 * 查询实例详情，获取磁盘使用率
	 * @Author: duanbinbin
	 * @param dcId
	 * @param prjId
	 * @param instanceId
	 * @return
	 *<li>Date: 2017年3月7日</li>
	 */
	@Override
	public JSONObject getInstanceForVolUsed(String dcId, String prjId,String instanceId) {
		log.info("查询实例详情-用于获取磁盘使用率");
		RestTokenBean restTokenBean = getRestTokenBean(dcId, prjId, OpenstackUriConstant.TROVE_SERVICE_URI);
		
		JSONObject result = restService.getById(restTokenBean, "/mgmt" + OpenstackUriConstant.RDS_INSTANCES_URI + "/", 
				OpenstackUriConstant.RDS_DATA_NAME, instanceId);
		if (null != result && !result.isEmpty()) {
            return result;
        }
        return null;
	}
	/**
	 * 汇总数据库实例的指标信息
	 * @Author: duanbinbin
	 * @param mongoTemplate
	 * @param meter
	 * @param projectId
	 * @param rds
	 * @param interval
	 *<li>Date: 2017年3月14日</li>
	 */
	@Override
	public void summaryMonitor(MongoTemplate mongoTemplate, String meter,
			String projectId, CloudRDSInstance rds, Date now,String interval) {
		if(interval.equals("1d")){
    		String todayStr = format.format(now);
            try {
                now = format.parse(todayStr);
            } catch (ParseException e) {
                log.error(e.toString(),e);
            }
    	}
        Date startTime = getStartTime(now, interval);
    	
    	Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("vm_id").is(rds.getVmId())),
                Aggregation.match(Criteria.where("timestamp").gte(startTime)),
                Aggregation.match(Criteria.where("timestamp").lt(now)),
                Aggregation.group().avg("counter_volume").as("average")
                );
        AggregationResults<JSONObject> totalresult = mongoTemplate.aggregate(agg,meter + ".detail", JSONObject.class);
        List<JSONObject> totallist = totalresult.getMappedResults();
        if(totallist.size() == 0){
        	return;
        }
        Double totalvolume = 0d;
        totalvolume = totallist.get(0).getDouble("average");
    	
        String unit = "";
        switch (meter) {
            case "disk.read.bytes.rate":
            case "disk.write.bytes.rate":
                unit = "MB/s";
                break;
            case "network.incoming.bytes.rate":
            case "network.outgoing.bytes.rate":
                unit = "Mb/s";
                break;
            case "cpu_util":
            case "memory.usage":
            case "volume.used":
                unit = "%";
                if(totalvolume > 100d){
            		totalvolume = 100d;
            	}
            	break;
        }
        now = DateUtil.dateRemoveSec(now);
        JSONObject json = new JSONObject();
        json.put("counter_name", meter);
        json.put("vm_id", rds.getVmId());
        json.put("instance_id", rds.getRdsId());
        json.put("project_id", projectId);
        json.put("timestamp", now);
        json.put("count_unit", unit);
        json.put("counter_volume", totalvolume);
        json.put("real_time", new Date());
        
        mongoTemplate.insert(json, meter + "." + interval);
	}
	
	private Date getStartTime(Date now, String interval) {
        Date time = new Date();
        switch (interval) {
            case "3min":
                time = DateUtil.addDay(now, new int[]{0,0,0,0,-3});
                break;
            case "5min":
            	time = DateUtil.addDay(now, new int[]{0,0,0,0,-5});
                break;
            case "10min":
            	time = DateUtil.addDay(now, new int[]{0,0,0,0,-10});
                break;
            case "1h":
            	time = DateUtil.addDay(now, new int[]{0,0,0,-1});
                break;
            case "1d":
            	time = DateUtil.addDay(now, new int[]{0,0,-1});
                break;
        }

        return time;
    }

}

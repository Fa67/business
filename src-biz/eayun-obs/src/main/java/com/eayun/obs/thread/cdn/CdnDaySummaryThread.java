package com.eayun.obs.thread.cdn;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.util.DateUtil;

public class CdnDaySummaryThread implements Runnable {
	
private static final Logger     log    = LoggerFactory.getLogger(CdnDaySummaryThread.class);
    
    private MongoTemplate           mongoTemplate;
    private Date           now;

	public CdnDaySummaryThread(MongoTemplate mongoTemplate, Date now) {
		this.mongoTemplate = mongoTemplate;
        this.now=now;
	}

	@Override
	public void run() {
		log.info("汇总客户每天CDN下载流量、动态请求数和HTTPS请求数");
		try {
			now = DateUtil.dateRemoveSec(now);
			Calendar c = Calendar.getInstance();  
			c.setTime(now); 
			int minute = c.get(Calendar.MINUTE);
			Date endTime = DateUtil.addDay(now, new int[]{0,0,0,0,-minute});
			Date startTime = DateUtil.addDay(endTime, new int[]{0,0,0,-24});
			
			Aggregation agg = Aggregation.newAggregation(
			        Aggregation.match(Criteria.where("timestamp").gt(startTime)),
			        Aggregation.match(Criteria.where("timestamp").lte(endTime)),
			        Aggregation.group("cus_id","cdnProvider").sum("flow_data").as("bytesTotal")
                            .sum("dreqs").as("dreqsTotal").sum("hreqs").as("hreqsTotal")
			        );
			AggregationResults<JSONObject> totalresult = mongoTemplate.aggregate(agg,"obs.cdn.1h", JSONObject.class);
			List<JSONObject> totallist = totalresult.getMappedResults();
			if(totallist.size() > 0){
				for(int i = 0; i < totallist.size(); i++){
					JSONObject obj = totallist.get(i);
					JSONObject insertObj = new JSONObject();
					insertObj.put("timestamp", startTime);
					insertObj.put("cus_id", obj.getString("cus_id"));
					insertObj.put("flow_data", obj.getLongValue("bytesTotal"));
					insertObj.put("dreqs", obj.getLongValue("dreqsTotal"));
					insertObj.put("hreqs", obj.getLongValue("hreqsTotal"));
					insertObj.put("counter_unit", "B");
					insertObj.put("cdnProvider", obj.getString("cdnProvider"));
					mongoTemplate.insert(insertObj, MongoCollectionName.OBS_CDN_1D);
				}
			}
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
		}
	}

}

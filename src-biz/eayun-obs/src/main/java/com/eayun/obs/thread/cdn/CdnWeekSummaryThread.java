package com.eayun.obs.thread.cdn;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.DateUtil;

public class CdnWeekSummaryThread implements Runnable {
	
	private static final Logger log = LoggerFactory.getLogger(CdnWeekSummaryThread.class);
	
	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	
	private MongoTemplate mongoTemplate;
	
	private JedisUtil jedisUtil;

	public CdnWeekSummaryThread(MongoTemplate mongoTemplate, JedisUtil jedisUtil) {
		this.mongoTemplate = mongoTemplate;
		this.jedisUtil = jedisUtil;
	}

	@Override
	public void run() {
		log.info("汇总上周CDN下载流量Top10");
		try {
			Date now = new Date();
			String todayStr = format.format(now);
			Date today = DateUtil.strToDate(todayStr);
			Date lastMonday = DateUtil.addDay(today, new int[] { 0, 0, -7 });
			
			Aggregation agg = Aggregation.newAggregation(
			        Aggregation.match(Criteria.where("timestamp").gt(lastMonday)),
			        Aggregation.match(Criteria.where("timestamp").lte(today)),
                    Aggregation.group("cus_id","cdnProvider").sum("flow_data").as("bytesTotal")
                            .sum("dreqs").as("dreqsTotal").sum("hreqs").as("hreqsTotal")
			        );
			AggregationResults<JSONObject> totalresult = mongoTemplate.aggregate(agg,"obs.cdn.1h", JSONObject.class);
			List<JSONObject> totallist = totalresult.getMappedResults();
			if(totallist.size() > 0){
				for(int i = 0;i < totallist.size();i++){
					JSONObject obj = totallist.get(i);
					JSONObject insertObj = new JSONObject();
					long flowData = obj.getLongValue("bytesTotal");
					insertObj.put("bucket_name ", insertObj.getString("bucket_name"));
					insertObj.put("cus_id ", insertObj.getString("cus_id"));
					insertObj.put("domain_id ", insertObj.getString("domain_id"));
                    insertObj.put("flow_data", flowData);
                    insertObj.put("dreqs", obj.getLongValue("dreqsTotal"));
                    insertObj.put("hreqs", obj.getLongValue("hreqsTotal"));
					insertObj.put("counter_unit ", "B");
					jedisUtil.addToSortedSet("obs:cdnSortByDownload", flowData,insertObj.toJSONString());
				}
			}
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
		}
	}

}

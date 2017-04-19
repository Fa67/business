package com.eayun.obs.thread;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.DateUtil;

public class ObsStorageDetailSummaryThread implements Runnable {

    private static final Logger     log    = LoggerFactory.getLogger(ObsStorageDetailSummaryThread.class);
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    private MongoTemplate mongoTemplate;
    private String cusId;
    private JedisUtil jedisUtil;
    
    
    public ObsStorageDetailSummaryThread(MongoTemplate mongoTemplate,String cusId,JedisUtil jedisUtil) {
    	this.mongoTemplate = mongoTemplate;
        this.cusId=cusId;
        this.jedisUtil=jedisUtil;
	}

	@Override
	public void run(){
		log.info("开始对容量信息进行当天汇总");
		try{
			 String userId=cusId;
			 Date now = new Date();
		     String todayStr = format.format(now);
		     Date today =DateUtil.strToDate(todayStr);
		     Date yesterday = DateUtil.addDay(today, new int[]{0,0,-1,1});
		     
		       
		     //算出容量总数
		     double storage=0;
		     long sum=0;
		     
		    Set<String> buckets=jedisUtil.keys(RedisKey.BUCKET_START +userId+":"+"*");
		     for(String buckect:buckets){
		    	 String[] args=buckect.split(":");
		    	 String bucketName=args[args.length-1];
		    	 String bucketStart=jedisUtil.get(RedisKey.BUCKET_START +userId+":"+bucketName);
		    	 String bucketEnd=jedisUtil.get(RedisKey.BUCKET_END+userId+":"+bucketName);
		    	 Date startBucket=DateUtil.stringToDate(bucketStart);
		    	 Date endBucket=DateUtil.stringToDate(bucketEnd);
		    	 
		    	 if(DateUtil.dateToStr(yesterday).equals(DateUtil.dateToStr(startBucket))||((endBucket.getTime()-today.getTime())<0)){
		    		 sum=0;
		    		 Criteria criatira = new Criteria();
				     criatira.andOperator(Criteria.where("owner").is(userId),Criteria.where("bucket").is(bucketName),Criteria.where("timestamp").gte(yesterday), Criteria.where("timestamp").lt(DateUtil.addDay(today, new int[]{0,0,0,1})));
				     List<JSONObject> jsonList = mongoTemplate.find(new Query(criatira),JSONObject.class,MongoCollectionName.OBS_STORAGE_1H);
				     for(int i=0;i<jsonList.size();i++){
					       JSONObject obj=jsonList.get(i);
					       JSONObject usage=obj.getJSONObject("usage");
					       if(null!=usage&&usage.size()>0){
					        	long storageUsed=usage.getLong("size_kb_actual");
					        	sum+=storageUsed;
					        }
					  }
				     storage+=(sum/24);
		    	 }else{
		    		 Aggregation agg = Aggregation.newAggregation(
				                Aggregation.match(Criteria.where("owner").is(userId)),
				                Aggregation.match(Criteria.where("bucket").is(bucketName)),
				                Aggregation.match(Criteria.where("timestamp").gte(yesterday)),
				                Aggregation.match(Criteria.where("timestamp").lt(DateUtil.addDay(today, new int[]{0,0,0,1}))),
				                Aggregation.group("bucket").avg("usage.size_kb_actual").as("total")
				                );
					AggregationResults<JSONObject> totalresult = mongoTemplate.aggregate(agg,MongoCollectionName.OBS_STORAGE_1H, JSONObject.class);
			        List<JSONObject> totallist = totalresult.getMappedResults();
			        for(int i=0;i<totallist.size();i++){
			        	JSONObject obj=totallist.get(i);
			        	storage+=obj.getDouble("total");
			        }
			    	 
			     }
		    	 
		    	 
		     }
		    
		     
		      JSONObject obsStorageUsed=new JSONObject();
		      obsStorageUsed.put("storageUsed", storage);
		      obsStorageUsed.put("timestamp", DateUtil.addDay(today, new int[]{0,0,-1}));
		      obsStorageUsed.put("owner", userId);
		      mongoTemplate.insert(obsStorageUsed, MongoCollectionName.OBS_STORAGE_24H);
	}catch(Exception e){
	    log.error(e.getMessage(),e);
	}
}

}
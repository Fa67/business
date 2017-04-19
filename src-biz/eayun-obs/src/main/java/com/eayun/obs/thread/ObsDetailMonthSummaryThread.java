package com.eayun.obs.thread;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.util.DateUtil;

public class ObsDetailMonthSummaryThread implements Runnable {

    private static final Logger     log    = LoggerFactory.getLogger(ObsDetailMonthSummaryThread.class);
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    private MongoTemplate mongoTemplate;
    private String cusId;
 
    public ObsDetailMonthSummaryThread(MongoTemplate mongoTemplate,String cusId) {
    	this.mongoTemplate = mongoTemplate;
        this.cusId=cusId;
	}

	@Override
	public void run(){
		log.info("开始对下载流量、请求次数历史信息进行汇总");
		try{
			 String userId=cusId;
			 Date now = new Date();
		        String todayStr = format.format(now);
		        Date today =DateUtil.strToDate(todayStr);
		        Date beforMonth = DateUtil.addDay(today, new int[]{0,-1});
		        
		        long countRequest=0;
		        long download=0;
		        long finalData=0;
		        Criteria criatira = new Criteria();
		        criatira.andOperator(Criteria.where("owner").is(userId),Criteria.where("timestamp").gte(beforMonth), Criteria.where("timestamp").lt(today));
		        List<JSONObject> jsonList = mongoTemplate.find(new Query(criatira),JSONObject.class,MongoCollectionName.OBS_USED_24H);
		        
		        Criteria backCriatira = new Criteria();
		        backCriatira.andOperator(Criteria.where("cus_id").is(cusId),Criteria.where("timestamp").gte(beforMonth), Criteria.where("timestamp").lt(today));
		        List<JSONObject> backJsonList = mongoTemplate.find(new Query(backCriatira),JSONObject.class,MongoCollectionName.CDN_BACKSOURCE_1H);
		        
		        for(int i=0;i<jsonList.size();i++){//下载流量原始数据
		        	JSONObject obj=jsonList.get(i);
	        		long bytesSent=obj.getLong("download");
	        		download+=bytesSent;
	        		long ops=obj.getLong("countRequest");
	        		countRequest+=ops;
	        		Date thisTime = obj.getDate("timestamp");
		        	thisTime = DateUtil.dateRemoveSec(thisTime);
		        	String bucketName = obj.getString("bucket");
		        	
		        	long backData = 0;
		        	if(null != backJsonList && !backJsonList.isEmpty()){//回源流量原始数据
		        		for(int j=0;j<backJsonList.size();j++){
		        			JSONObject backJson=backJsonList.get(j);
		        			Date backThisTime = backJson.getDate("timestamp");
		        			backThisTime = DateUtil.dateRemoveSec(backThisTime);
				        	String backBucketName = backJson.getString("bucket_name");
				        	if(thisTime.getTime()==backThisTime.getTime() && 
				        			bucketName.equals(backBucketName)){
				        		backData = backJson.getLongValue("backsource");
				        		break;
				        	}
		        		}
		        	}
		        	long diffData = (bytesSent > backData)?(bytesSent - backData):0;
		        	finalData+=diffData;
		        }
			
		        JSONObject obsUsed=new JSONObject();
		        obsUsed.put("countRequest", countRequest);
		        obsUsed.put("download", download);
		        obsUsed.put("timestamp", beforMonth);
		        obsUsed.put("owner", userId);
		        obsUsed.put("final_data", finalData);
		        mongoTemplate.insert(obsUsed, MongoCollectionName.OBS_USED_1MONTH);
		        
		        long backsource = 0;
		        String cdnProvider = "UpYun";
		        if(null != backJsonList && !backJsonList.isEmpty()){
		        	cdnProvider = backJsonList.get(0).getString("cdnProvider");
	        		for(int j=0;j<backJsonList.size();j++){
	        			JSONObject backJson=backJsonList.get(j);
	        			long sourceData = backJson.getLongValue("backsource");
	        			backsource += sourceData;
	        		}
	        	}
		        JSONObject backsourceJson=new JSONObject();
		        backsourceJson.put("backsource", backsource);
		        backsourceJson.put("timestamp", beforMonth);
		        backsourceJson.put("cus_id", userId);
		        backsourceJson.put("cdnProvider", cdnProvider);
		        backsourceJson.put("counter_unit", "B");
		        mongoTemplate.insert(backsourceJson, MongoCollectionName.CDN_BACKSOURCE_1MONTH);
		
	}catch(Exception e){
	    log.error(e.getMessage(),e);
	}
}

}
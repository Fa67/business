package com.eayun.obs.thread;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.util.DateUtil;

public class ObsDetailSummaryThread implements Runnable {

    private static final Logger     log    = LoggerFactory.getLogger(ObsDetailSummaryThread.class);
    private  SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    private MongoTemplate mongoTemplate;
    private String cusId;
 
    public ObsDetailSummaryThread(MongoTemplate mongoTemplate,String cusId) {
    	this.mongoTemplate = mongoTemplate;
        this.cusId=cusId;
	}

	@Override
	public void run(){
		log.info("开始对采集的下载流量、请求次数信息进行当天汇总");
		try{
			 String userId=cusId;
			 Date now = new Date();
		        String todayStr = format.format(now);
		        Date today =DateUtil.strToDate(todayStr);
		        Date yesterday = DateUtil.addDay(today, new int[]{0,0,-1,1});
		       
		        long countRequest=0;
		        long download=0;
		        long upload=0;
		        long getCount=0;
		        long putCount=0;
		        long deleteCount=0;
		        Criteria criatira = new Criteria();
		        criatira.andOperator(Criteria.where("owner").is(userId),Criteria.where("timestamp").gte(yesterday), Criteria.where("timestamp").lt(DateUtil.addDay(today, new int[]{0,0,0,1})));
		        List<JSONObject> jsonList = mongoTemplate.find(new Query(criatira),JSONObject.class,MongoCollectionName.OBS_USED_1H);
		        
		        Criteria backCriatira = new Criteria();
		        backCriatira.andOperator(Criteria.where("cus_id").is(cusId),Criteria.where("timestamp").gte(yesterday), Criteria.where("timestamp").lt(today));
		        List<JSONObject> backJsonList = mongoTemplate.find(new Query(backCriatira),JSONObject.class,MongoCollectionName.CDN_BACKSOURCE_1H);
		        
		        long finalData=0;
		        for(int i=0;i<jsonList.size();i++){
		        	JSONObject obj=jsonList.get(i);
		        	JSONArray categories = obj.getJSONArray("categories");
		        	Date thisTime = obj.getDate("timestamp");
		        	thisTime = DateUtil.dateRemoveSec(thisTime);
		        	String bucketName = obj.getString("bucket");
		        	
		        	long oneData = 0;
		        	for(int j=0;j<categories.size();j++){
		        		long bytesSent=categories.getJSONObject(j).getLong("bytes_sent");
		        		long bytesReceived=categories.getJSONObject(j).getLong("bytes_received");
		        		long ops=categories.getJSONObject(j).getLong("ops");
		        		download+=bytesSent;
		        		upload+=bytesReceived;
		        		countRequest+=ops;
		        		String action=categories.getJSONObject(j).getString("category");
		        		if(action.contains("get")){
		        			getCount+=ops;
		        		}else if(action.contains("put")){
		        			putCount+=ops;
		        		}else if(action.contains("delete")){
		        			deleteCount+=ops;
		        		}
		        		oneData+=bytesSent;
		        		
		        	}
		        	long oneBacksource=0;
		        	if(null!=backJsonList && !backJsonList.isEmpty()){
		        		for(int j=0;j<backJsonList.size();j++){
		        			JSONObject backJson=backJsonList.get(j);
		        			Date backThisTime = backJson.getDate("timestamp");
		        			backThisTime = DateUtil.dateRemoveSec(backThisTime);
				        	String backBucketName = backJson.getString("bucket_name");
				        	if(thisTime.getTime()==backThisTime.getTime()&&bucketName.equals(backBucketName)){
				        		oneBacksource = backJson.getLongValue("backsource");
				        		break;
				        	}
		        		}
		        	}
		        	long diff=(oneData-oneBacksource)>0?oneData-oneBacksource:0;
		        	finalData = finalData+diff;
		        }
		        
		        long backsource=0;
		        String cdnProvider = "UpYun";
		        if(null!=backJsonList && !backJsonList.isEmpty()){
		        	cdnProvider = backJsonList.get(0).getString("cdnProvider");
	        		for(int j=0;j<backJsonList.size();j++){
	        			JSONObject backJson=backJsonList.get(j);
	        			long backData = backJson.getLongValue("backsource");
	        			backsource+=backData;
	        		}
	        		JSONObject sourceJson=new JSONObject();
			        sourceJson.put("timestamp", DateUtil.addDay(today, new int[]{0,0,-1}));
			        sourceJson.put("cus_id", cusId);
			        sourceJson.put("counter_unit", "B");
			        sourceJson.put("backsource", backsource);
			        sourceJson.put("cdnProvider", cdnProvider);
			        mongoTemplate.insert(sourceJson, MongoCollectionName.CDN_BACKSOURCE_1D);
	        	}
		        
			
		        JSONObject obsUsed=new JSONObject();
		        obsUsed.put("countRequest", countRequest);
		        obsUsed.put("download", download);
		        obsUsed.put("upload", upload);
		        obsUsed.put("getCount", getCount);
		        obsUsed.put("putCount", putCount);
		        obsUsed.put("deleteCount", deleteCount);
		        obsUsed.put("timestamp", DateUtil.addDay(today, new int[]{0,0,-1}));
		        obsUsed.put("owner", userId);
		        obsUsed.put("final_data", finalData);
		        mongoTemplate.insert(obsUsed, MongoCollectionName.OBS_USED_24H);		
	}catch(Exception e){
	    log.error(e.getMessage(),e);
	}
}

}
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

public class ObsStorageDetailMonthSummaryThread implements Runnable {

    private static final Logger     log    = LoggerFactory.getLogger(ObsStorageDetailMonthSummaryThread.class);
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    private MongoTemplate mongoTemplate;
    private String cusId;
    
    
    public ObsStorageDetailMonthSummaryThread(MongoTemplate mongoTemplate,String cusId) {
    	this.mongoTemplate = mongoTemplate;
        this.cusId=cusId;
	}

	@Override
	public void run(){
		log.info("开始对存储容量信息进行历史账单汇总");
		try{
			 String userId=cusId;
			 Date now = new Date();
		     String todayStr = format.format(now);
		     Date today =DateUtil.strToDate(todayStr);
		     Date beforMonth = DateUtil.addDay(today, new int[]{0,-1});
		       
		     //算出容量总数
		     double storage=0;
		     Criteria criatira = new Criteria();
		     criatira.andOperator(Criteria.where("owner").is(userId),Criteria.where("timestamp").gte(beforMonth), Criteria.where("timestamp").lt(today));
		     List<JSONObject> jsonList = mongoTemplate.find(new Query(criatira),JSONObject.class,MongoCollectionName.OBS_STORAGE_24H);
		        
		     for(int i=0;i<jsonList.size();i++){
		       JSONObject obj=jsonList.get(i);
		        	double storageUsed=obj.getDouble("storageUsed");
		        	storage+=storageUsed;
		      }
		      JSONObject obsStorageUsed=new JSONObject();
		      obsStorageUsed.put("storageUsed", storage);
		      obsStorageUsed.put("timestamp", beforMonth);
		      obsStorageUsed.put("owner", userId);
		      mongoTemplate.insert(obsStorageUsed, MongoCollectionName.OBS_STORAGE_1MONTH);
	}catch(Exception e){
	    log.error(e.getMessage(),e);
	}
}

}
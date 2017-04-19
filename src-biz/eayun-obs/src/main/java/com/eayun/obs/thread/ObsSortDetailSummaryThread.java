package com.eayun.obs.thread;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import com.eayun.common.constant.RedisKey;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.DateUtil;

public class ObsSortDetailSummaryThread implements Runnable {

    private static final Logger     log    = LoggerFactory.getLogger(ObsSortDetailSummaryThread.class);
    private  SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    private MongoTemplate           mongoTemplate;
    private JedisUtil jedisUtil;
     
   
    public ObsSortDetailSummaryThread(MongoTemplate mongoTemplate,JedisUtil jedisUtil) {
    	 this.mongoTemplate = mongoTemplate;
         this.jedisUtil=jedisUtil;
	}

	@Override
	public void run() {
		log.info("开始汇总上周下载流量、请求次数Top10");
		try{
			 Date now = new Date();
		        String todayStr = format.format(now);
		        Date today =DateUtil.strToDate(todayStr);
		        Date Monday = DateUtil.addDay(today, new int[]{0,0,-7});

		        Criteria criatira = new Criteria();
		        criatira.andOperator(Criteria.where("timestamp").gte(Monday), Criteria.where("timestamp").lt(today));
		        List<JSONObject> jsonList = mongoTemplate.find(new Query(criatira),JSONObject.class,MongoCollectionName.OBS_USED_1H);
		        List<JSONObject> newJsonList=new ArrayList<JSONObject>();
		        
		        /**剔除回源流量后再比较*/
		        Criteria backCriatira = new Criteria();
	            backCriatira.andOperator(
						Criteria.where("timestamp").gte(Monday),
						Criteria.where("timestamp").lt(today));
	            List<JSONObject> backJsonList = mongoTemplate.find(new Query(backCriatira), 
	            		JSONObject.class, MongoCollectionName.CDN_BACKSOURCE_1H);
	            
		        for(int i=0;i<jsonList.size();i++){//下载流量原始数据，所有bucket的每小时下载流量数据
		        	JSONObject obj=jsonList.get(i);
		        	 long countRequest=0;
				     long download=0;
				     String owner=obj.getString("owner");
		        	 String bucketName=obj.getString("bucket");
		        	JSONArray categories = obj.getJSONArray("categories");
		        	
		        	Date thisTime = obj.getDate("timestamp");
		        	thisTime = DateUtil.dateRemoveSec(thisTime);
		        	for(int j=0;j<categories.size();j++){
		        		long bytesSent=categories.getJSONObject(j).getLong("bytes_sent");//原每小时下载流量
		        		download+=bytesSent;
		        		long ops=categories.getJSONObject(j).getLong("ops");
		        		countRequest+=ops;
		        	}
		        	
		        	long backData = 0;
		        	if(null != backJsonList && !backJsonList.isEmpty()){//回源流量原始数据，所有bucket的每小时回源流量数据
		        		for(int j=0;j<backJsonList.size();j++){
		        			JSONObject backJson = backJsonList.get(j);
		        			Date backThisTime = backJson.getDate("timestamp");
		        			backThisTime = DateUtil.dateRemoveSec(backThisTime);
				        	String backBucketName = backJson.getString("bucket_name");
				        	String cusId = backJson.getString("cus_id");
				        	if(thisTime.getTime()==backThisTime.getTime() && 
				        			bucketName.equals(backBucketName) && cusId.equals(owner)){
				        		//找到对应bucket该时段的回源流量数据
				        		backData = backJson.getLongValue("backsource");
				        		break;
				        	}
			        	}
		        	}
		        	//比较，正则正常记录，负则为0
		        	long diffData = (download > backData)?(download - backData):0;
		        	
		        	JSONObject bucket=new JSONObject();
		        	bucket.put("name", bucketName);
		        	bucket.put("owner", owner);
		        	bucket.put("countRequest", countRequest);
		        	bucket.put("download", diffData);//记录新数据
		        	newJsonList.add(bucket);
		        }
		        
		        List<JSONObject> result=new ArrayList<JSONObject>();
		        for(JSONObject object : newJsonList){
		        	String ownerName=object.getString("owner");
		        	String bucketName=object.getString("name");
		        	
		        	JSONObject obj = getJSONObject(result,ownerName,bucketName);
		        	if(obj == null){
		        		result.add(object);
		        	}else{
		        		long req=object.getLong("countRequest");
	        			long loadSize=object.getLong("download");
	        			
	        			long oldRreq=obj.getLong("countRequest");
	        			long oldLoadSize=obj.getLong("download");
	        			
	        			obj.put("countRequest", req+oldRreq);
	        			obj.put("download", loadSize+oldLoadSize);
		        	}
		        }
		        
		        jedisUtil.delete(RedisKey.OBS_SORT_BY_COUNT_REQUEST);
		        jedisUtil.delete(RedisKey.OBS_SORT_BY_DOWNLOAD);
		       for(JSONObject object : result){
		    	   long request=object.getLong("countRequest");
		    	   long load=object.getLong("download");
		           jedisUtil.addToSortedSet(RedisKey.OBS_SORT_BY_COUNT_REQUEST,request,object.toJSONString());
		           jedisUtil.addToSortedSet(RedisKey.OBS_SORT_BY_DOWNLOAD,load,object.toJSONString());
		        }

		        
	}catch(Exception e){ 
	    log.error(e.getMessage(),e);
	}
 }
	
	private JSONObject getJSONObject(List<JSONObject> list,String owner,String bucketName){
		for(JSONObject object : list){
			if(owner.equals(object.getString("owner")) && bucketName.equals(object.getString("name"))){
				return object;
			}
		}
		return null;
		
	}
	
}
package com.eayun.obs.thread.backsource;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.cdn.intf.CDN;
import com.eayun.cdn.util.CDNConstant;
import com.eayun.charge.bean.ChargeConstant;
import com.eayun.charge.model.ObsStatsBean;
import com.eayun.common.constant.EayunQueueConstant;
import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.template.EayunRabbitTemplate;
import com.eayun.common.util.DateUtil;
import com.eayun.obs.model.CdnBucket;
import com.eayun.obs.service.ObsCdnBucketService;

public class CdnBacksourceDetailGatherThread implements Runnable {
	
	private static final Logger     log    = LoggerFactory.getLogger(CdnBacksourceDetailGatherThread.class);
    
	private CDN           			cdn;
    private MongoTemplate           mongoTemplate;
    private ObsCdnBucketService     obsCdnBucketService;
    private EayunRabbitTemplate 	eayunRabbitTemplate;
    private Date           			now;
    private String           		cusId;
    

	public CdnBacksourceDetailGatherThread(CDN cdn,MongoTemplate mongoTemplate,ObsCdnBucketService obsCdnBucketService,
			EayunRabbitTemplate eayunRabbitTemplate, String cusId, Date now) {
		this.cdn = cdn;
		this.mongoTemplate = mongoTemplate;
        this.obsCdnBucketService=obsCdnBucketService;
        this.eayunRabbitTemplate=eayunRabbitTemplate;
        this.now=now;
        this.cusId=cusId;
	}

	@Override
	public void run() {
		log.info("采集客户:"+cusId+"的回源流量Thread");
		try {
			List<CdnBucket> cdnBucketList =  obsCdnBucketService.getListForBackByCusId(cusId);
			Date from = DateUtil.addDay(now, new int[]{0,0,0,-1,-10});
			Date to = DateUtil.addDay(now, new int[]{0,0,0,0,-10});
			if(!(null==cdnBucketList||cdnBucketList.isEmpty())){
				for(CdnBucket cdnBucket : cdnBucketList){
					String resultData = cdn.getBackSource(cdnBucket.getDomainId(), from, to);
					JSONObject json = JSONObject.parseObject(resultData);
					if(json.getBooleanValue("result")){
						insertMongo(cdnBucket,json, now);
					}else{
						log.error(json.getString("message"));
						int count = 0;
						while(!json.getBooleanValue("result") && CDNConstant.CDN_SYSERROR_CODE.equals(json.getString("error_code")) && count < CDNConstant.CDN_ERROR_COUNT){
							count++;
							String elseResult = cdn.getBackSource(cdnBucket.getDomainId(), from, to);
							json = JSONObject.parseObject(elseResult);
							if(json.getBooleanValue("result")){
								insertMongo(cdnBucket,json, now);
							}else{
								log.error(json.getString("message"));
							}
						}
					}
				}
			}

		} catch (Exception e) {
			log.error(e.getMessage(),e);
		} finally {
            ObsStatsBean obsStatsBean=new ObsStatsBean();
            obsStatsBean.setChargeFrom(DateUtil.addDay(now, new int[]{0,0,0,-1}));
            obsStatsBean.setChargeTo(now);
            obsStatsBean.setCusId(cusId);
            obsStatsBean.setStatsType(ChargeConstant.OBS_STATS_TYPE.CDN_BACK_ORIGIN);
            JSONObject json=new JSONObject();
            json=(JSONObject) JSONObject.toJSON(obsStatsBean);
            eayunRabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_OBS_GATHER_SUCCEED, json.toJSONString());
        }
    }
	
	private void insertMongo(CdnBucket cdnBucket,JSONObject resultJson,Date thisTime){
		JSONArray jsonArray =  resultJson.getJSONArray("data");
		long data = 0;
		for(int i = 0 ; i < jsonArray.size();i++){
			JSONObject obj = jsonArray.getJSONObject(i);
			long bytes = obj.getLongValue("bytes");
			data += bytes;
		}
		JSONObject object = new JSONObject();
		object.put("timestamp", thisTime);
		object.put("bucket_name", cdnBucket.getBucketName());
		object.put("cus_id", cdnBucket.getCusId());
		object.put("domain_id", cdnBucket.getDomainId());
		object.put("backsource", data);
		object.put("counter_unit", "B");
		object.put("cdnProvider", cdnBucket.getCdnProvider());
		object.put("real_time", new Date());
		mongoTemplate.insert(object, MongoCollectionName.CDN_BACKSOURCE_1H);
		
		JSONObject detail = new JSONObject();
		detail.put("timestamp", thisTime);
		detail.put("bucket_name", cdnBucket.getBucketName());
		detail.put("cus_id", cdnBucket.getCusId());
		detail.put("domain_id", cdnBucket.getDomainId());
		detail.put("details", jsonArray);
		detail.put("real_time", new Date());
		mongoTemplate.insert(detail, MongoCollectionName.CDN_BACKSOURCE_API_DETAILS);
	}

}

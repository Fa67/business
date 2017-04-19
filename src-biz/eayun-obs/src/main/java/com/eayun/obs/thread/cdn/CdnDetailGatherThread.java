package com.eayun.obs.thread.cdn;

import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.eayun.charge.bean.ChargeConstant;
import com.eayun.charge.model.ObsStatsBean;
import com.eayun.common.constant.EayunQueueConstant;
import com.eayun.common.template.EayunRabbitTemplate;
import com.eayun.obs.service.ObsCdnBucketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.cdn.intf.CDN;
import com.eayun.cdn.util.CDNConstant;
import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.util.DateUtil;
import com.eayun.obs.model.CdnBucket;

public class CdnDetailGatherThread implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(CdnDetailGatherThread.class);

    private MongoTemplate mongoTemplate;
    private Date now;
    private CDN cdn;
    private ObsCdnBucketService obsCdnBucketService;
    private EayunRabbitTemplate eayunRabbitTemplate;
    private String cusId;

    public CdnDetailGatherThread(MongoTemplate mongoTemplate, CDN cdn, CdnBucket cdnBucket, Date now) {
        this.mongoTemplate = mongoTemplate;
        this.now = now;
        this.cdn = cdn;
    }

    public CdnDetailGatherThread(CDN cdn, MongoTemplate mongoTemplate, ObsCdnBucketService obsCdnBucketService, EayunRabbitTemplate eayunRabbitTemplate, String cusId, Date now) {
        this.cdn = cdn;
        this.mongoTemplate = mongoTemplate;
        this.obsCdnBucketService = obsCdnBucketService;
        this.eayunRabbitTemplate = eayunRabbitTemplate;
        this.cusId = cusId;
        this.now = now;
    }

    @Override
    public void run() {
        // TODO: 2016/12/1  查询客户下所有的bucket，然后查询统计每个bucket的CDN下载流量、动态请求数、HTTPS请求数。
        // 对应几个每日、每周和每月的统计计划任务也要增加响应的字段-DONE
        log.info("开始采集客户["+cusId+"]下所有bucket的CDN下载流量、动态请求数和HTTPS请求数");
        try {
            List<CdnBucket> cdnBucketList =  obsCdnBucketService.getListForBackByCusId(cusId);
            Date from = DateUtil.addDay(now, new int[]{0,0,0,-1,-10});
            Date to = DateUtil.addDay(now, new int[]{0,0,0,0,-10});
            for(CdnBucket cdnBucket : cdnBucketList){
                log.info("采集bucket:" + cdnBucket.getBucketName() + "每小时CDN下载流量、动态请求数和HTTPS请求数");
                String domainId = cdnBucket.getDomainId();
                String resultData = cdn.getStatistics(domainId, from, to);
                JSONObject json = JSONObject.parseObject(resultData);
                if ("true".equals(json.getString("result"))) {
                    insertMongo(cdnBucket, json, to);
                } else {
                    log.error(json.getString("message"));
                    int count = 0;
                    while (null != json.getString("error_code") && json.getString("error_code").equals(CDNConstant.CDN_SYSERROR_CODE) && count < CDNConstant.CDN_ERROR_COUNT) {
                        count++;
                        String elseResult = cdn.getStatistics(domainId, from, to);
                        json = JSONObject.parseObject(elseResult);
                        String elseIsok = json.getString("result");
                        if ("true".equals(elseIsok)) {
                            insertMongo(cdnBucket, json, to);
                        } else {
                            log.error(json.getString("message"));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("采集客户["+cusId+"]下所有bucket的CDN下载流量、动态请求数和HTTPS请求数异常", e);
        } finally {
            ObsStatsBean obsStatsBean=new ObsStatsBean();
            obsStatsBean.setChargeFrom(DateUtil.addDay(now, new int[]{0,0,0,-1}));
            obsStatsBean.setChargeTo(now);
            obsStatsBean.setCusId(cusId);
            obsStatsBean.setStatsType(ChargeConstant.OBS_STATS_TYPE.CDN_DETAIL_GATHER_DONE);
            JSONObject json=new JSONObject();
            json=(JSONObject) JSONObject.toJSON(obsStatsBean);
            eayunRabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_OBS_GATHER_SUCCEED, json.toJSONString());
        }
    }

    private void insertMongo(CdnBucket cdnBucket, JSONObject json, Date to) {
        JSONArray jsonArray = json.getJSONArray("data");
        long bytesTotal = 0L;
        long dreqsTotal = 0L;
        long hreqsTotal = 0L;
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            long bytes = obj.getLongValue("bytes");
            long dreqs = obj.getLongValue("dreqs");
            long hreqs = obj.getLongValue("hreqs");
            bytesTotal += bytes;
            dreqsTotal += dreqs;
            hreqsTotal += hreqs;
        }
        JSONObject object = new JSONObject();
        object.put("timestamp", to);
        object.put("bucket_name", cdnBucket.getBucketName());
        object.put("cus_id", cdnBucket.getCusId());
        object.put("domain_id", cdnBucket.getDomainId());
        object.put("flow_data", bytesTotal);
        object.put("counter_unit", "B");
        object.put("dreqs",dreqsTotal);
        object.put("hreqs",hreqsTotal);
        object.put("cdnProvider", cdnBucket.getCdnProvider());
        object.put("real_time", new Date());
        mongoTemplate.insert(object, MongoCollectionName.OBS_CDN_1H);
    }

}

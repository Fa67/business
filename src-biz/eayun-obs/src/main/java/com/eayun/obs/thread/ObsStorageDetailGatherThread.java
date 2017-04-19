package com.eayun.obs.thread;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.accesskey.model.AccessKey;
import com.eayun.charge.bean.ChargeConstant;
import com.eayun.charge.model.ObsStatsBean;
import com.eayun.common.constant.EayunQueueConstant;
import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.template.EayunRabbitTemplate;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.HmacSHA1Util;
import com.eayun.common.util.ObsUtil;
import com.eayun.obs.base.service.ObsBaseService;
import com.eayun.obs.model.ObsAccessBean;
import com.eayun.obs.model.ObsResultBean;

public class ObsStorageDetailGatherThread implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ObsStorageDetailGatherThread.class);
    private MongoTemplate mongoTemplate;
    private ObsBaseService obsService;
    @SuppressWarnings("unused")
    private AccessKey aKey;
    private String cusId;
    private JedisUtil jedisUtil;
    private Date now;
    private EayunRabbitTemplate eayunRabbitTemplate;

    public ObsStorageDetailGatherThread(MongoTemplate mongoTemplate, ObsBaseService obsService, AccessKey ak, String cusId, JedisUtil jedisUtil, Date now,EayunRabbitTemplate eayunRabbitTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.obsService = obsService;
        this.aKey = ak;
        this.cusId = cusId;
        this.jedisUtil = jedisUtil;
        this.now = now;
        this.eayunRabbitTemplate=eayunRabbitTemplate;

    }

    @Override
    public void run() {
        log.info("开始采集存储容量的指标");
        try {

            String accessKey = ObsUtil.getAdminAccessKey();
            String secretKey = ObsUtil.getAdminSecretKey();
            String relativePath = "/admin/bucket";
            String userId = cusId;
            String host = ObsUtil.getEayunObsHost();
            String date = DateUtil.getRFC2822Date(new Date());
            //GET /admin/bucket?format=json&uid=lucy&stats&bucket=lucy HTTP/1.1
            String signature = ObsUtil.getSignature("GET", "", "", date, "", relativePath);
            String hmacSHA1 = HmacSHA1Util.getEncrypt(signature, secretKey);
            String header = ObsUtil.getRequestHeader();
            String url = header + host + relativePath + "?format=json&uid=" + userId + "&stats";
            ObsAccessBean obsBean = new ObsAccessBean();
            obsBean.setHost(host);
            obsBean.setUrl(url);
            obsBean.setHmacSHA1(hmacSHA1);
            obsBean.setAccessKey(accessKey);
            obsBean.setRFC2822Date(date);
            obsBean.setHttp("http://".equals(header));
            ObsResultBean resultBean = obsService.get(obsBean);
            String resDataString = resultBean.getResData();
            log.info(resDataString);
            JSONArray resJsonArry = null;
            if (null != resDataString && !"".equals(resDataString)) {
                resJsonArry = JSONArray.parseArray(resDataString);
                log.info(resJsonArry.toJSONString());
            }
            
            if (null != resJsonArry && resJsonArry.size() > 0) {
                for (int i = 0; i < resJsonArry.size(); i++) {
                    JSONObject bucket = resJsonArry.getJSONObject(i);
                    String bucketName = bucket.getString("bucket");
                    String start = jedisUtil.get(RedisKey.BUCKET_START  + userId + ":" + bucketName);
                    if (null == start || "null" == start) {
                        jedisUtil.set(RedisKey.BUCKET_START  + userId + ":" + bucketName, DateUtil.dateToString(now));
                        jedisUtil.set(RedisKey.BUCKET_END + userId + ":" + bucketName, DateUtil.dateToString(now));
                    } else {
                        jedisUtil.delete(RedisKey.BUCKET_END + userId + ":" + bucketName);
                        jedisUtil.set(RedisKey.BUCKET_END + userId + ":" + bucketName, DateUtil.dateToString(now));
                    }
                    JSONObject us = bucket.getJSONObject("usage");
                    JSONObject usage = new JSONObject();
                    if (null != us) {
                        JSONObject rgw = us.getJSONObject("rgw.main");
                        if (null != rgw && rgw.size() > 0) {
                            usage.put("size_kb", rgw.getInteger("size_kb"));
                            usage.put("size_kb_actual", rgw.getInteger("size_kb_actual"));
                            usage.put("num_objects", rgw.getInteger("num_objects"));
                        }
                    }
                    bucket.put("timestamp", now);
                    bucket.remove("usage");
                    bucket.put("usage", usage);
                    mongoTemplate.insert(bucket, MongoCollectionName.OBS_STORAGE_1H);
                }
            }

        } catch (Exception e) {
            log.error("计划任务采集存储容量异常", e);
        } finally {
            ObsStatsBean obsStatsBean=new ObsStatsBean();
            obsStatsBean.setChargeFrom(DateUtil.addDay(now, new int[]{0,0,0,-1}));
            obsStatsBean.setChargeTo(now);
            obsStatsBean.setCusId(cusId);
            obsStatsBean.setStatsType(ChargeConstant.OBS_STATS_TYPE.STORAGE);
            JSONObject json=(JSONObject) JSONObject.toJSON(obsStatsBean);
            eayunRabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_OBS_GATHER_SUCCEED, json.toJSONString());
        }
    }
}
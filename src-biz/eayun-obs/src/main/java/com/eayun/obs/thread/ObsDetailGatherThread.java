package com.eayun.obs.thread;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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
import com.eayun.common.template.EayunRabbitTemplate;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.HmacSHA1Util;
import com.eayun.common.util.ObsUtil;
import com.eayun.obs.base.service.ObsBaseService;
import com.eayun.obs.model.ObsAccessBean;
import com.eayun.obs.model.ObsResultBean;

public class ObsDetailGatherThread implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ObsDetailGatherThread.class);

    private MongoTemplate mongoTemplate;
    private ObsBaseService obsService;
    private EayunRabbitTemplate eayunRabbitTemplate;
    @SuppressWarnings("unused")
    private AccessKey aKey;
    private String cusId;
    private Date now;
    private static Calendar GMT_CAL = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    private SimpleDateFormat UTC_FORMAT = new SimpleDateFormat("yyyy-MM-dd'%20'HH:00:00");

    public ObsDetailGatherThread(MongoTemplate mongoTemplate, ObsBaseService obsService, AccessKey ak, String cusId, Date now, EayunRabbitTemplate eayunRabbitTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.obsService = obsService;
        this.aKey = ak;
        this.cusId = cusId;
        this.now = now;
        this.eayunRabbitTemplate = eayunRabbitTemplate;
    }

    @Override
    public void run() {
        log.info("开始采集下载流量、请求次数的指标");
        Date beginTime = DateUtil.addDay(now, new int[]{0, 0, 0, -1});
        try {

            String accessKey = ObsUtil.getAdminAccessKey();
            String secretKey = ObsUtil.getAdminSecretKey();

            String relativePath = "/admin/usage";
            String userId = cusId;

            //此处说明一下：由于底层bug,底层的时间要比我们当前系统时间晚16个小时（正确的应该是晚8个小时）
            //Date beginTime=DateUtil.addDay(now, new int[]{0,0,0,-9});
            //Date endTime=DateUtil.addDay(now, new int[]{0,0,0,-8});
            UTC_FORMAT.setCalendar(GMT_CAL);
            String bgTime = UTC_FORMAT.format(beginTime);
            String edTime = UTC_FORMAT.format(now);

            String host = ObsUtil.getEayunObsHost();
            String date = DateUtil.getRFC2822Date(new Date());
            String signature = ObsUtil.getSignature("GET", "", "", date, "", relativePath);
            String hmacSHA1 = HmacSHA1Util.getEncrypt(signature, secretKey);
            String header = ObsUtil.getRequestHeader();
            String url = header + host + relativePath + "?format=json&uid=" + userId + "&show-entries=true&show-summary=false&start=" + bgTime + "&end=" + edTime;
            //String url = "http://"+host+relativePath+"?format=json&uid="+userId+"&show-entries=true&show-summary=false&start=2016-02-24T23:59:13&end=2016-02-25T00:59:13";
            ObsAccessBean obsBean = new ObsAccessBean();
            obsBean.setHost(host);
            obsBean.setUrl(url);
            obsBean.setHmacSHA1(hmacSHA1);
            obsBean.setAccessKey(accessKey);
            obsBean.setRFC2822Date(date);
            obsBean.setHttp("http://".equals(header));
            ObsResultBean ResultBean = obsService.get(obsBean);
            String resDataString = ResultBean.getResData();
            JSONObject resJson = JSONObject.parseObject(resDataString);
            log.info(resJson.toJSONString());
            JSONArray entries = resJson.getJSONArray("entries");
            log.info("-------------------------------------------");
            if (null != entries) {
                for (int i = 0; i < entries.size(); i++) {
                    JSONObject entire = entries.getJSONObject(i);
                    String owner = entire.getString("owner");
                    JSONArray buckets = entire.getJSONArray("buckets");
                    for (int j = 0; j < buckets.size(); j++) {
                        JSONObject bucket = buckets.getJSONObject(j);
                        bucket.put("owner", owner);
                        bucket.put("timestamp", now);
                        mongoTemplate.insert(bucket, MongoCollectionName.OBS_USED_1H);
                    }
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            ObsStatsBean obsStatsBean = new ObsStatsBean();
            obsStatsBean.setChargeFrom(beginTime);
            obsStatsBean.setChargeTo(now);
            obsStatsBean.setCusId(cusId);
            obsStatsBean.setStatsType(ChargeConstant.OBS_STATS_TYPE.USED);
            JSONObject json = new JSONObject();
            json = (JSONObject) JSONObject.toJSON(obsStatsBean);
            eayunRabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_OBS_GATHER_SUCCEED, json.toJSONString());
        }
    }
}
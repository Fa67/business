package com.eayun.monitor.job;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.eayun.common.exception.AppException;
import com.eayun.common.job.BaseQuartzJobBean;
import com.eayun.common.redis.JedisUtil;
import com.eayun.mail.service.MailService;
import com.eayun.monitor.model.MonitorAlarmItem;
import com.eayun.monitor.service.AlarmService;
import com.eayun.monitor.service.MonitorAlarmService;
import com.eayun.monitor.thread.AlarmMailTemplate;
import com.eayun.monitor.thread.StatusCalculateThread;
import com.eayun.monitor.thread.StatusCalculateThreadPool;
import com.eayun.sms.service.SMSService;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class StatusCalculateJob extends BaseQuartzJobBean {
    private static final Logger log = LoggerFactory.getLogger(StatusCalculateJob.class);
    private AlarmService            alarmService;
    private MonitorAlarmService     monitorAlarmService;
    private MongoTemplate           mongoTemplate;
    private JedisUtil               jedisUtil;
    private SMSService              smsService;
    private MailService             mailService;
    @SuppressWarnings("unused")
    private Resource                dbConfig;
    private static boolean isDbConfigInited = false;
    @SuppressWarnings("unused")
    private Resource                htmlConfig;
    private static boolean isHtmlConfigInited = false;

    public void setDbConfig(Resource dbConfig) {
        this.dbConfig = dbConfig;
        if(!isDbConfigInited){
            isDbConfigInited = true;
            try {
                AlarmMailTemplate.dbInputStream = dbConfig.getInputStream();
            } catch (IOException e) {
                log.error(e.getMessage(),e);
            }
        }
    }

    public void setHtmlConfig(Resource htmlConfig) {
        this.htmlConfig = htmlConfig;
        if(!isHtmlConfigInited){
            isHtmlConfigInited = true;
            try {
                AlarmMailTemplate.htmlInputStream = htmlConfig.getInputStream();
            } catch (Exception e) {
                log.error(e.getMessage(),e);
            }
        }
    }



    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        ApplicationContext applicationContext = getApplicationContext(context);
        alarmService = (AlarmService) applicationContext.getBean(AlarmService.class);
        monitorAlarmService = (MonitorAlarmService) applicationContext
            .getBean(MonitorAlarmService.class);
        mongoTemplate = (MongoTemplate) applicationContext.getBean("mongoTemplate");
        jedisUtil = (JedisUtil) applicationContext.getBean("jedisUtil");
        smsService = (SMSService) applicationContext.getBean("smsService");
        mailService = (MailService) applicationContext.getBean("mailService");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date currentTime = null;
        try {
            currentTime = format.parse(format.format(new Date()));
        } catch (ParseException e) {
            throw new AppException(e.getMessage());
        }
        List<MonitorAlarmItem> itemList = new ArrayList<MonitorAlarmItem>();
        itemList = monitorAlarmService.getMonitorAlarmItemList();
        for (MonitorAlarmItem monitorAlarmItem : itemList) {
            StatusCalculateThread thread = new StatusCalculateThread(monitorAlarmItem,
                monitorAlarmService, alarmService, mongoTemplate, jedisUtil, currentTime,
                smsService, mailService);
            StatusCalculateThreadPool.pool.submit(thread);
        }
    }
}

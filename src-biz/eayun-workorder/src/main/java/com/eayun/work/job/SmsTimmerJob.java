package com.eayun.work.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.eayun.work.model.WorkOrderState;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.eayun.common.job.BaseQuartzJobBean;
import com.eayun.sms.service.SMSService;
import com.eayun.work.model.BaseNoSendSms;
import com.eayun.work.service.NoSendSmsService;
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class SmsTimmerJob extends BaseQuartzJobBean{
    private NoSendSmsService noSendSmsService;
    private SMSService smsService ;//短信发送
    
    private final Logger      log = LoggerFactory.getLogger(SmsTimmerJob.class);
    
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
    	ApplicationContext applicationContext = getApplicationContext(context);
    	noSendSmsService = applicationContext.getBean(NoSendSmsService.class);
    	smsService = (SMSService) applicationContext.getBean("smsService");
    	
        //获取为发送的短信
        StringBuffer hql = new StringBuffer();
        List<Date> values = new  ArrayList<Date>();
        hql.append("from BaseNoSendSms where smsStart < ? and smsEnd >?");
        values.add(new Date());
        values.add(new Date());
        List<BaseNoSendSms> baseNoSendList = noSendSmsService.find(hql.toString(), values);
        try {
            for (BaseNoSendSms baseNoSendSms : baseNoSendList) {
                String str = baseNoSendSms.getSmsPhone();
                List<String> list = new ArrayList<String>();
                if(str!=null && str.length()>0){
                  str=str.substring(1, str.length()-1);
                  String str1[] =str.split(",");
                  list= java.util.Arrays.asList(str1);
                }
                boolean bool=smsService.send(baseNoSendSms.getSmsContent(), list);
                if(bool){
                	noSendSmsService.delete(baseNoSendSms.getSmsId());
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }
}

package com.eayun.schedule.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.redis.JedisUtil;
import com.eayun.mail.service.MailService;
import com.eayun.monitor.bean.QuotaMsg;
import com.eayun.monitor.service.ContactService;
import com.eayun.schedule.service.QuotaNotifierService;
import com.eayun.sms.service.SMSService;

@Service
@Transactional
public class QuotaNotifierServiceImpl implements QuotaNotifierService {

    private static final Logger log                 = LoggerFactory
        .getLogger(QuotaNotifierServiceImpl.class);

    private static Timer        quotaNotifierTimer;

    private static final long   DELAY_MILLISECONDS  = 10000;

    private static final long   PERIOD_MILLISECONDS = 10000;

    private static final int    MESSAGES_PER_TASK = 100;

    @Autowired
    private JedisUtil           jedisUtil;
    @Autowired
    private SMSService          smsService;
    @Autowired
    private MailService         mailService;
    @Autowired
    private ContactService      contactService;


    
    private static Resource config;
    private static Resource quotaHtml;
    private static InputStream quotaHtmlInputStream;
    private static InputStream configInputStream;
    private static StringBuffer mailStringBuffer;
    private static Map<String,String> urlMap;

    public QuotaNotifierServiceImpl() {
        if (quotaNotifierTimer == null) {
            quotaNotifierTimer = new Timer();
            quotaNotifierTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    //在TimerTask中执行在配额消息队列拉取消息完成发送邮件短信通知的功能
                    log.info("启动配额耗尽通知TimerTask");
                    for (int i=0; i < MESSAGES_PER_TASK; i++) {
                        try {
                            String msgStr = jedisUtil.pop(RedisKey.QUOTA_MSGQUEUE);
                            if (msgStr == null) {
                                return;
                            }
                            JSONObject msgJson = JSON.parseObject(msgStr);
                            if (msgJson == null) {
                                return;
                            }
                            QuotaMsg quotaMsg = new QuotaMsg();
                            quotaMsg.setBiz(msgJson.getString("biz"));
                            quotaMsg.setCustomerId(msgJson.getString("customer"));
                            quotaMsg.setProjectId(msgJson.getString("project"));

                            notifyAdminOfQuotaExhausted(quotaMsg);

                        } catch (Exception e) {
                            log.error("配额耗尽通知TimerTask执行异常", e);
                        }
                    }

                }

            }, DELAY_MILLISECONDS, PERIOD_MILLISECONDS);
        }
    }

    protected void notifyAdminOfQuotaExhausted(QuotaMsg quotaMsg) throws Exception {
        //1. 根据客户ID获取客户下的超级管理员的邮箱、手机号
        //2. 根据项目ID获取项目名称
        //3. 组织内容发送邮件和短信
        //4. Tips quotaMsg中的业务类型用于将来扩展使用，可根据不同业务进行不同操作
        Map<String, String> adminContactInfo = contactService.getAdminContact(quotaMsg.getCustomerId(),quotaMsg.getProjectId());

        String email = adminContactInfo.get("email");
        String phone = adminContactInfo.get("phone");
        String projectName = adminContactInfo.get("projectName");

        String smsContent = "尊敬的客户您好，您的项目[" + projectName + "]下报警短信使用量已达配额，为避免您错过报警提醒，请及时登录控制台处理。";

        List<String> mobiles = new ArrayList<String>();
        mobiles.add(phone);
        smsService.send(smsContent, mobiles);

        String title = "报警短信配额提醒";
        List<String> links = new ArrayList<String>();
        links.add(email);
        String mailContent = getMailContent(projectName);
        mailService.send(title, mailContent, links);
    }

    private String getMailContent(String projectName) throws IOException {
        initResource();
        
        String mailContent = mailStringBuffer.toString();
        mailContent=mailContent.replace("{projectName}", projectName);
        return mailContent;
    }

    /**
     * 初始化资源
     * @throws IOException
     */
    private void initResource() throws IOException {
        if(config==null){
            config = new ClassPathResource("db.properties");
            configInputStream = config.getInputStream();
            if(configInputStream==null){
                throw new NullPointerException();
            }
            urlMap = initUrlMap();
        }
        if(quotaHtml==null){
            quotaHtml = new ClassPathResource("quotanotice.html");
            quotaHtmlInputStream = quotaHtml.getInputStream();
            if(quotaHtmlInputStream==null){
                throw new NullPointerException();
            }
        }
        if(mailStringBuffer==null){
            mailStringBuffer = new StringBuffer();
            BufferedReader br = new BufferedReader(new InputStreamReader(quotaHtmlInputStream,"utf-8"));  
            String line = ""; 
            String mail="";
            while ((line = br.readLine()) != null) {  
                mail += line; 
            }
            mail=mail.replace("{imgUrl}", urlMap.get("imgUrl"));
            mail=mail.replace("{ecscUrl}", urlMap.get("ecscUrl"));
            mailStringBuffer.append(mail);
            br.close(); 
        }
    }

    private Map<String, String> initUrlMap() {
        Properties p  =   new  Properties();
        Map<String,String> map = new HashMap<String,String>();
        try {
            p.load(configInputStream);
            map.put("imgUrl",  p.getProperty("imgUrl"));
            map.put("ecscUrl", p.getProperty("ecscUrl"));
            map.put("ecmcUrl", p.getProperty("ecmcUrl"));
        } catch (IOException e1) {
            log.error(e1.getMessage(),e1);
        }
        return map;
    }
}

package com.eayun.monitor.job;

import com.eayun.common.job.BaseQuartzJobBean;
import com.eayun.common.util.BeanUtils;
import com.eayun.customer.serivce.CustomerService;
import com.eayun.datacenter.ecmcservice.EcmcDataCenterService;
import com.eayun.mail.service.MailService;
import com.eayun.monitor.ecmcservice.EcmcAlarmService;
import com.eayun.monitor.ecmcservice.EcmcApiAlarmService;
import com.eayun.monitor.ecmcservice.EcmcMonitorAlarmService;
import com.eayun.monitor.model.*;
import com.eayun.monitor.thread.ApiAlarmMailTemplate;
import com.eayun.sms.service.SMSService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * API资源监控以及报警管理计划任务，负责API调用产生的实时数据统计记录以及报警信息等的提醒操作
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class EcmcApiStatusMonitorWithAlarmRuleCheckJob extends BaseQuartzJobBean {

    private EcmcApiAlarmService ecmcApiAlarmService ;
    private EcmcAlarmService ecmcAlarmService ;
    private SMSService smsService ;
    private MailService mailService ;
    private EcmcMonitorAlarmService ecmcMonitorAlarmService ;
    private EcmcDataCenterService ecmcDataCenterService ;
    private CustomerService customerService ;

    private static Logger logger = LoggerFactory.getLogger(EcmcApiStatusMonitorWithAlarmRuleCheckJob.class) ;

    @SuppressWarnings("unused")
    private Resource dbConfig;
    private static boolean isDbConfigInited = false;
    @SuppressWarnings("unused")
    private Resource htmlConfig;
    private static boolean isHtmlConfigInited = false;
    public void setDbConfig(Resource dbConfig) {
        this.dbConfig = dbConfig;
        if(!isDbConfigInited){
            isDbConfigInited = true;
            try {
                ApiAlarmMailTemplate.dbInputStream = dbConfig.getInputStream();
            } catch (IOException e) {
                logger.error(e.getMessage(),e);
            }
        }
    }
    public void setHtmlConfig(Resource htmlConfig) {
        this.htmlConfig = htmlConfig;
        if(!isHtmlConfigInited){
            isHtmlConfigInited = true;
            try {
                ApiAlarmMailTemplate.htmlInputStream = htmlConfig.getInputStream();
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
            }
        }
    }
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        //当前时间
        logger.info("API资源监测以及报警管理计划任务开始执行");
        Date now = new Date();
        ApplicationContext applicationContext = getApplicationContext(context);
        this.ecmcApiAlarmService = applicationContext.getBean(EcmcApiAlarmService.class);
        this.smsService =              applicationContext.getBean(SMSService.class);
        this.mailService =             applicationContext.getBean(MailService.class);
        this.ecmcMonitorAlarmService = applicationContext.getBean(EcmcMonitorAlarmService.class);
        this.ecmcDataCenterService =   applicationContext.getBean(EcmcDataCenterService.class);
        this.customerService =         applicationContext.getBean(CustomerService.class) ;
        this.ecmcAlarmService =        applicationContext.getBean(EcmcAlarmService.class) ;
        Map<String,List<BaseEcmcAlarmMessage>> alarmMessages = null ;

        //整理当前时刻（分钟）内的历史访问数据
        // TODO: 2017/3/22 该方法暂时没有发现问题
        if (this.ecmcApiAlarmService.createRealTimeData(now)){
            // TODO: 2017/3/22 该方法暂时没有发现问题
            Set<String> allDatasWeiduMessages = this.ecmcApiAlarmService.createRedisDataForCheckIsSatisfyWarningCondition(now);
            if (allDatasWeiduMessages != null){
                // TODO: 2017/3/22 此方法做了一点小的修改，需要在测试环境中再验证一下
                alarmMessages = this.ecmcApiAlarmService.checkIsSatisfyWarning(now, allDatasWeiduMessages);


                if (alarmMessages != null) {
                    //得出正确的报警信息并且进行保存
                    ecmcMonitorAlarmService.saveAlarmMessages(alarmMessages);
                    for (Map.Entry<String, List<BaseEcmcAlarmMessage>> entity : alarmMessages.entrySet()) {
                        //报警规则对应的报警信息
                        List<BaseEcmcAlarmMessage> ruleWarningMessages = entity.getValue() ;
                        List<EcmcAlarmContact> contacts = ecmcAlarmService.getEcmcConsByRuleId(entity.getKey());
                        //通过邮件进行提醒
                        sendAlarmMessageByMail(ruleWarningMessages, contacts);
                        //通过短信进行提醒
                        sendAlarmMessageBySMS(contacts);
                    }
                }


            }
        }
        logger.info("API资源监测以及报警管理计划任务执行完毕");
    }

    /**
     * 查询报警联系人邮件信息
     * @param contacts  联系人集合
     * @return
     */
    private List<String> queryEmailInformation(List<EcmcAlarmContact> contacts){
        List<String> addresses = new ArrayList<>() ;
        for (EcmcAlarmContact contact : contacts){
            String contactMethod = contact.getContactMethod() ;
            if ((contactMethod!=null) && (!"".equals(contactMethod))){
                if (contactMethod.contains("邮件")){
                    addresses.add(contact.getContactEmail());
                }
            }
        }
        return addresses ;
    }

    /**
     * 查询报警联系人电话号码
     * @param contacts  联系人集合
     * @return
     */
    private List<String> queryPhoneInformation(List<EcmcAlarmContact> contacts){
        List<String> addresses = new ArrayList<>() ;
        for (EcmcAlarmContact contact : contacts){
            String contactMethod = contact.getContactMethod() ;
            if ((contactMethod!=null) && (!"".equals(contactMethod))){
                if (contactMethod.contains("短信")){
                    addresses.add(contact.getContactPhone());
                }
            }
        }
        return addresses ;
    }

    /**
     * 通过邮件发送报警信息提醒
     * @param messages
     * @param contacts
     */
    private void sendAlarmMessageByMail(List<BaseEcmcAlarmMessage> messages, List<EcmcAlarmContact> contacts){
        String mailSendTitle = "易云报警提醒";
        String strContent = createMailContent();
        List<EcmcAlarmMessage> alarmMessages = processAlarmMessage(messages);
        StringBuilder tbodyContent = new StringBuilder();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ;
        for (EcmcAlarmMessage ecmcAlarmMessage : alarmMessages){
            tbodyContent.append("<tr class=\"help-block\">");
            tbodyContent.append("<td style='text-align: center;'>").append(ecmcAlarmMessage.getCusName()).append("</td>");
            tbodyContent.append("<td style='text-align: center;'>").append(ecmcAlarmMessage.getIp()).append("</td>");
            tbodyContent.append("<td style='text-align: center;'>").append(ecmcAlarmMessage.getDcName()).append("</td>");
            tbodyContent.append("<td style='text-align: center;'>").append(ecmcAlarmMessage.getDetail()).append("</td>");
            tbodyContent.append("<td style='text-align: center;'>").append(format.format(ecmcAlarmMessage.getTime())).append("</td>");
            tbodyContent.append("</tr>") ;
        }
        String sendContent = strContent.replace("{tbodyContent}", tbodyContent.toString()) ;
        logger.info("邮件发送内容为 : " + sendContent);
        List<String> sendPeoples = queryEmailInformation(contacts) ;
        try {
            if (sendPeoples.size() != 0) {
                mailService.send(mailSendTitle, sendContent, sendPeoples);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 通过短信发送报警提醒信息
     * @param contacts
     */
    private void sendAlarmMessageBySMS(List<EcmcAlarmContact> contacts){
        String strContent = "易云公有云API被调用时发生报警消息，请登录运维中心处理。" ;
        try {
            smsService.send(strContent, queryPhoneInformation(contacts));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 得到邮件发送模板内容
     * @return
     */
    private String createMailContent() {
        ApiAlarmMailTemplate template = ApiAlarmMailTemplate.getInstance();
        String strContent=String.valueOf(template.getMailHtml());
        return strContent;
    }

    /**
     * 解析报警对象集合信息，将ID转换为对应的中文名称
     * @param origin
     * @return
     */
    private List<EcmcAlarmMessage> processAlarmMessage(List<BaseEcmcAlarmMessage> origin){
        Map<String,String> regions = new HashMap<>();
        Map<String,String> cuses = new HashMap<>();

        List<EcmcAlarmMessage> ecmcAlarmMessages = new ArrayList<>() ;
        for (BaseEcmcAlarmMessage baseEcmcAlarmMessage : origin){
            EcmcAlarmMessage ecmcAlarmMessage = new EcmcAlarmMessage() ;
            try {
                BeanUtils.copyProperties(ecmcAlarmMessage,baseEcmcAlarmMessage);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            regions.put("-","-");
            cuses.put(  "-","-");
            String regionId = ecmcAlarmMessage.getDcId();
            if (regions.get(regionId) == null && regionId != null){
                regions.put(regionId,ecmcDataCenterService.getdatacenterName(regionId));
            }
            ecmcAlarmMessage.setDcName(regions.get(regionId));

            String cusId = ecmcAlarmMessage.getCusId() ;
            if (cuses.get(cusId) == null && cusId != null){
                cuses.put(cusId,customerService.findCustomerById(cusId).getCusOrg());
            }
            ecmcAlarmMessage.setCusName(cuses.get(cusId));

            ecmcAlarmMessages.add(ecmcAlarmMessage);
        }
        return ecmcAlarmMessages;
    }
}
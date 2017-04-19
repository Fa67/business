package com.eayun.work.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.util.StringUtil;
import com.eayun.customer.model.Customer;
import com.eayun.customer.model.User;
import com.eayun.customer.serivce.CustomerService;
import com.eayun.customer.serivce.UserService;
import com.eayun.ecmcuser.model.EcmcSysUser;
import com.eayun.ecmcuser.service.EcmcSysUserService;
import com.eayun.mail.service.MailService;
import com.eayun.sms.service.SMSService;
import com.eayun.work.dao.EcmcUserDao;
import com.eayun.work.dao.NoSendSmsDao;
import com.eayun.work.model.BaseNoSendSms;
import com.eayun.work.model.WorkOpinion;
import com.eayun.work.model.WorkUtils;
import com.eayun.work.model.Workorder;
import com.eayun.work.service.WorkMailService;
@Service
@Transactional
public class WorkMailServiceImpl implements WorkMailService {
	@Autowired
    private UserService userService;//用户
    @Autowired
    private MailService mailService;//邮件发送
    @Autowired
    private SMSService smsService ;//邮件发送
    @Autowired
    private EcmcUserDao ecmcUserDao;//ecmc用户
    @Autowired
    private EcmcSysUserService ecmcUserService;//ecmc用户service
    @Autowired
    private NoSendSmsDao noSendSmsDao;//未发送的短信
    @Autowired
    private CustomerService cusService;//未发送的短信
    /**
     * 时间格式化 yyyy-MM-dd HH:mm:ss
     * @param date
     * @return
     */
    private String dateFmt(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strDate = formatter.format(date);
        return strDate;
    }
    @SuppressWarnings("deprecation")
    @Override
    public void addWorkSendMail(Workorder workorder, String userId) throws Exception {
    	if(StringUtil.isEmpty(workorder.getWorkCusName())){
    		Customer cus = cusService.findCustomerById(workorder.getApplyCustomer());
            if(cus!=null){
                workorder.setWorkCusName(StringUtil.isEmpty(cus.getCusId())?"":cus.getCusOrg());
            }else{
                workorder.setWorkCusName(workorder.getApplyCustomer());
            }
    	}
        List<String> mailList= new ArrayList<String>();
        List<String> smsList= new ArrayList<String>();
        
        String smsContent ="尊敬的客户：欢迎注册易云公有云服务，我们将尽快联系您，如有任何疑问请致电400-606-6396。";
        if(!workorder.getWorkType().equals("0007001003002")){//非注册类工单
            mailList.add(workorder.getWorkEmail());//创建者邮箱
            String strContent=String.valueOf(ReadMailHtml.getEcscMailHtml());
            strContent=strContent.replace("{userName}", workorder.getWorkApplyUserName());
            strContent=strContent.replace("{workNum}", workorder.getWorkNum());
            strContent=strContent.replace("{workCreDate}", dateFmt(workorder.getWorkCreTime()));
            strContent=strContent.replace("{workTitle}", workorder.getWorkTitle());
            strContent=strContent.replace("{workOrderTypeName}", workorder.getWorkTypeName());
            strContent=strContent.replace("{workFalg}", "待受理");
            strContent=strContent.replace("{workScene}", "提交工单");
            mailService.send("新增工单", strContent, mailList); 
            
            smsContent = "尊敬的客户：您的工单已提交成功，正在等待运维工程师受理，请登录管理控制台查看及管理。";
        }
        Date date = new Date();
        int hours=date.getHours();
        if(workorder.getWorkPhone()!=null){
            smsList.add(workorder.getWorkPhone());
            if(!StringUtil.isEmpty(workorder.getWorkPhoneTime()) && ((workorder.getWorkPhoneTime().equals("0") && 18>hours && hours>=9) || workorder.getWorkPhoneTime().equals("1"))){//接受短信
                smsService.send(smsContent, smsList);
            }else if(!StringUtil.isEmpty(workorder.getWorkPhoneTime()) && workorder.getWorkPhoneTime().equals("2")){//从不接受短信

            }else{
                //计算出下次发送时间段，存入数据库中
                BaseNoSendSms baseNoSendSms = new BaseNoSendSms();
                baseNoSendSms.setSmsContent(smsContent);
                baseNoSendSms.setSmsPhone(smsList.toString());
                Calendar calendar = new GregorianCalendar();
                calendar.setTime(date);
                if(hours>=18){
                	calendar.add(Calendar.DATE,1);
                }
                date =calendar.getTime(); 
                date.setMinutes(0);
                date.setSeconds(0);
                date.setHours(9);//设置成9点
                Date startDate = new Date(date.getTime());
                baseNoSendSms.setSmsStart(startDate);
                date.setHours(18);//设置成18点
                Date endDate= new Date(date.getTime());
                baseNoSendSms.setSmsEnd(endDate);
                noSendSmsDao.save(baseNoSendSms);
            }
        }
        
        //----获取ecmc中的管理员用户
        mailList.clear();
        smsList.clear();
        String permission ="java:acceptAllOrder";
        List<EcmcSysUser> list = ecmcUserService.findUserByPermission(null,permission);
        if(WorkUtils.REGISTERTYPE.equals(workorder.getWorkType()) ||WorkUtils.QUOTATYPE.equals(workorder.getWorkType())){
            permission="java:acceptSpecialOrder";
        }else{
            permission="java:acceptMormalOrder";
        }
        List<EcmcSysUser> ecmcList = ecmcUserService.findUserByPermission(null,permission);
        list.addAll(ecmcList);
        for (EcmcSysUser ecmcUser : list) {
            if(StringUtil.isEmail(ecmcUser.getMail())){
                mailList.add(ecmcUser.getMail());
            }
        }
        workorder.setWorkHeadUserName("用户");
        if(WorkUtils.REGISTERTYPE.equals(workorder.getWorkType())){
        	mailService.send("新增客户注册工单", this.content(workorder), mailList);
        }else{
        	mailService.send("新增工单", this.content(workorder), mailList);
        }
        
    }
    @Override
    public List<EcmcSysUser> getEcmcUserList(String permission){
    	List<EcmcSysUser> ecmcUserList=ecmcUserService.findUserByPermission(null, permission);
        return ecmcUserList;
    }
    //获取责任人信息
    @Override
    public EcmcSysUser findEcmcUserByUserId(String userId){
    	EcmcSysUser baseEcmcUser = ecmcUserService.findUserById(userId);
        return baseEcmcUser;
    }
    @Override
    public void updateWorkorderForFalg(Workorder newWorkorder, Workorder oldWorkorder, String userId) throws Exception {
        
        //----获取ecmc中的用户
        List<String> list  = new ArrayList<String>();
        if(!StringUtil.isEmpty(oldWorkorder.getWorkHeadUser())){//工单责任人不为空
            EcmcSysUser workHeadUser = this.findEcmcUserByUserId(oldWorkorder.getWorkHeadUser());
            list.add(workHeadUser.getMail());
        }else{
            List<EcmcSysUser> ecmcuserList = this.getEcmcUserList("java:acceptAllOrder");
            for (EcmcSysUser ecmcUser : ecmcuserList) {
                if(StringUtil.isEmail(ecmcUser.getMail())){
                    list.add(ecmcUser.getMail());
                }
            }
        }
        mailService.send("关闭工单", this.content(newWorkorder), list);
    }
   

    @Override
    public void updWorkForFcSendMail(Workorder workorder,String userId) throws Exception {
        List<EcmcSysUser> ecmcUserList = this.getEcmcUserList("java:acceptAllOrder");
        List<String> list  = new ArrayList<String>();
        for (EcmcSysUser ecmcUser : ecmcUserList) {
            if(StringUtil.isEmail(ecmcUser.getMail())){
                list.add(ecmcUser.getMail());
            }
        }
        if(!StringUtil.isEmpty(workorder.getWorkHeadUser())){
            EcmcSysUser ecmcUser = ecmcUserService.findUserById(workorder.getWorkHeadUser());
            if(ecmcUser!=null){
                list.add(ecmcUser.getMail());
            }
        }
        if(StringUtil.isEmpty(workorder.getWorkCusName())){
    		Customer cus = cusService.findCustomerById(workorder.getApplyCustomer());
            if(cus!=null){
                workorder.setWorkCusName(StringUtil.isEmpty(cus.getCusId())?workorder.getApplyCustomer():cus.getCusOrg());
            }else{
                workorder.setWorkCusName(workorder.getApplyCustomer());
            }
    	}

    	if(StringUtil.isEmpty(workorder.getWorkApplyUserName())){
            User user = userService.findUserById(userId);
    		workorder.setWorkApplyUserName(user.getUserAccount());
    		workorder.setWorkCreUserName(user.getUserAccount());
    	}
        String ecmcStrContent = String.valueOf(ReadMailHtml.getEcmcMailHtml());
        StringBuffer content = new StringBuffer();
        content.append("<span class='blue'>");
        content.append(workorder.getWorkTitle());
        content.append("</span>");
        content.append(" 于");
        content.append(dateFmt(new Date()));
        content.append(" 被客户");
        content.append(workorder.getWorkCusName());
        content.append(" 一键投诉");
        ecmcStrContent=ecmcStrContent.replace("{work}", content);
		ecmcStrContent=ecmcStrContent.replace("{userName}", workorder.getWorkHeadUserName()!=null?workorder.getWorkHeadUserName():"管理员");
    	ecmcStrContent=ecmcStrContent.replace("{workNum}", workorder.getWorkNum());
    	ecmcStrContent=ecmcStrContent.replace("{workCreTime}", dateFmt(workorder.getWorkCreTime()));
    	ecmcStrContent=ecmcStrContent.replace("{workLevel}", workorder.getWorkLevelName());
    	ecmcStrContent=ecmcStrContent.replace("{workType}", workorder.getWorkTypeName());
    	ecmcStrContent=ecmcStrContent.replace("{applyCustomer}", workorder.getWorkCusName());
    	ecmcStrContent=ecmcStrContent.replace("{workFalg}", WorkUtils.getEcmcFlagMap().get(workorder.getWorkFalg()));
    	ecmcStrContent=ecmcStrContent.replace("{workContent}", StringUtil.isEmpty(workorder.getWorkContent()) ? "" : workorder.getWorkContent());
    	ecmcStrContent=ecmcStrContent.replace("{opinionContent}", "该工单已被客户投诉。");
        mailService.send("投诉工单", ecmcStrContent.toString(), list);
    }

    @Override
    public void addWorkOpinionSendMail(WorkOpinion workOpinion, Workorder workorder, String userId) throws Exception {
    	if(StringUtil.isEmpty(workorder.getWorkCusName())){
    		Customer cus = cusService.findCustomerById(workorder.getApplyCustomer());
            if(cus!=null){
                workorder.setWorkCusName(StringUtil.isEmpty(cus.getCusId())?workorder.getApplyCustomer():cus.getCusOrg());
            }else{
                workorder.setWorkCusName(workorder.getApplyCustomer());
            }
    	}
    	if(!StringUtil.isEmpty(workorder.getWorkHeadUser()) && !StringUtil.isEmpty(workOpinion.getOpinionContent())){//工单责任人和回复内容不为空
        	EcmcSysUser workHeadUser = findEcmcUserByUserId(workorder.getWorkHeadUser());
            //----获取ecmc中的用户
            List<String> MailList  = new ArrayList<String>();
            MailList.add(workHeadUser.getMail());
            
            String ecmcStrContent = String.valueOf(ReadMailHtml.getEcmcMailHtml());
            StringBuffer content = new StringBuffer();
            content.append("<span class='blue'>");
            content.append(workorder.getWorkTitle());
            content.append("</span>");
            content.append(" 于");
            content.append(dateFmt(workOpinion.getOpinionTime()));
            content.append(" 被客户");
            content.append(workorder.getWorkCusName());
            content.append(" 回复");
            ecmcStrContent=ecmcStrContent.replace("{work}", content);
    		ecmcStrContent=ecmcStrContent.replace("{userName}", workorder.getWorkHeadUserName() !=null ?workorder.getWorkHeadUserName():"管理员");
        	ecmcStrContent=ecmcStrContent.replace("{workNum}", workorder.getWorkNum());
        	ecmcStrContent=ecmcStrContent.replace("{workCreTime}", dateFmt(workorder.getWorkCreTime()));
        	ecmcStrContent=ecmcStrContent.replace("{workLevel}", workorder.getWorkLevelName());
        	ecmcStrContent=ecmcStrContent.replace("{workType}", workorder.getWorkTypeName());
        	ecmcStrContent=ecmcStrContent.replace("{applyCustomer}", workorder.getWorkCusName());
        	ecmcStrContent=ecmcStrContent.replace("{workFalg}",WorkUtils.getEcmcFlagMap().get(workorder.getWorkFalg()));
        	ecmcStrContent=ecmcStrContent.replace("{workContent}", StringUtil.isEmpty(workorder.getWorkContent())?"":workorder.getWorkContent());
        	ecmcStrContent=ecmcStrContent.replace("{opinionContent}", workOpinion.getOpinionContent());
            mailService.send("回复责任人", ecmcStrContent.toString(), MailList);
        }
        
    }
    @SuppressWarnings("deprecation")
    @Override
    public void updWorkEcscFalgSendMail(Workorder workorder, String userId) throws Exception {
        List<String> mailList = new ArrayList<>();
        User user = userService.findUserById(userId);
        String strContent=ReadMailHtml.getEcscMailHtml().toString();
        strContent=strContent.replace("{userName}", user.getUserAccount());
        strContent=strContent.replace("{workNum}", workorder.getWorkNum());
        strContent=strContent.replace("{workCreDate}", dateFmt(new Date()));
        strContent=strContent.replace("{workTitle}", workorder.getWorkTitle());
        strContent=strContent.replace("{workOrderTypeName}", workorder.getWorkTypeName());
        strContent=strContent.replace("{workFalg}", "待评价");
        strContent=strContent.replace("{workScene}", "待评价工单");
        mailList.add(workorder.getWorkEmail());
        mailService.send("待评价工单", strContent, mailList);
        String smsContent="尊敬的客户：您有1条待评价工单，请登录管理控制台及时处理。";
        Date date = new Date();
        int hours=date.getHours();
        List<String> smsList = new ArrayList<String>();
        smsList.add(workorder.getWorkPhone());
        if(workorder.getWorkPhoneTime().equals("1") || (workorder.getWorkPhoneTime().equals("0") && 18>hours && hours>=9)){//接受短信
            smsService.send(smsContent, smsList);
        }else if(workorder.getWorkPhoneTime() != null && workorder.getWorkPhoneTime().equals("2")){//从不接受短信
        	
        }else{
            //计算出下次发送时间段，存入数据库中
            BaseNoSendSms baseNoSendSms = new BaseNoSendSms();
            baseNoSendSms.setSmsContent(smsContent);
            baseNoSendSms.setSmsPhone(smsList.toString());
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            if(hours>=18){
            	calendar.add(Calendar.DATE,1);
            }
            date =calendar.getTime(); 
            date.setHours(9);//设置成9点
            date.setMinutes(0);
            date.setSeconds(0);
            Date strDate = new Date(date.getTime());
            baseNoSendSms.setSmsStart(strDate);
            date.setHours(18);//设置成18点
            Date endDate = new Date(date.getTime());
            baseNoSendSms.setSmsEnd(endDate);
            noSendSmsDao.save(baseNoSendSms);
        }
    }
    private String content(Workorder workorder) throws Exception{
    	if(StringUtil.isEmpty(workorder.getWorkCusName())){
    		Customer cus = cusService.findCustomerById(workorder.getApplyCustomer());
            if(cus!=null){
                workorder.setWorkCusName(StringUtil.isEmpty(cus.getCusId())?"":cus.getCusOrg());
            }else{
                workorder.setWorkCusName(workorder.getApplyCustomer());
            }
    	}
    	String ecmcStrContent = String.valueOf(ReadMailHtml.getEcmcMailHtml());
    	StringBuffer contentStr = new StringBuffer();
        contentStr.append("工单：<span class='blue'>");
        contentStr.append(workorder.getWorkTitle());
        contentStr.append("</span> 于");
    	if(workorder.getWorkEcscFalg().equals("0") || workorder.getWorkEcscFalg().equals("1")){
    		contentStr.append(dateFmt(workorder.getWorkCreTime()));
    		contentStr.append(" 已提交成功");
    	}else if(workorder.getWorkEcscFalg().equals("7")){
    		contentStr.append(dateFmt(new Date()));
    		contentStr.append(" 已被 客户");
    		contentStr.append(workorder.getWorkCusName());
    		contentStr.append(" 关闭");
    		ecmcStrContent=ecmcStrContent.replace("{work}", contentStr);
    	}else if(workorder.getWorkEcscFalg().equals("6")){
    		contentStr.append(dateFmt(new Date()));
    		contentStr.append(" 已被 客户");
    		contentStr.append(workorder.getWorkCusName());
    		contentStr.append(" 删除");
    	}
        ecmcStrContent=ecmcStrContent.replace("{work}", contentStr);
    	ecmcStrContent=ecmcStrContent.replace("{userName}", !StringUtil.isEmpty(workorder.getWorkHeadUserName())?workorder.getWorkHeadUserName():"用户");
    	ecmcStrContent=ecmcStrContent.replace("{workNum}", workorder.getWorkNum());
    	ecmcStrContent=ecmcStrContent.replace("{workCreTime}", dateFmt(workorder.getWorkCreTime()));
    	ecmcStrContent=ecmcStrContent.replace("{workLevel}", workorder.getWorkLevelName());
    	ecmcStrContent=ecmcStrContent.replace("{workType}", workorder.getWorkTypeName());
    	ecmcStrContent=ecmcStrContent.replace("{applyCustomer}", StringUtil.isEmpty(workorder.getWorkCusName())?"":workorder.getWorkCusName());
    	ecmcStrContent=ecmcStrContent.replace("{workFalg}", WorkUtils.getEcmcFlagMap().get(workorder.getWorkFalg()));
    	ecmcStrContent=ecmcStrContent.replace("{workContent}", StringUtil.isEmpty(workorder.getWorkContent())?"":workorder.getWorkContent());
    	if(workorder.getWorkEcscFalg().equals("7")){
    		ecmcStrContent=ecmcStrContent.replace("{opinionContent}", "该工单已被客户关闭");
    	}else{
    		ecmcStrContent=ecmcStrContent.replace("{opinionContent}", StringUtil.isEmpty(workorder.getWorkContent())?"":workorder.getWorkContent());
    	}

    	return ecmcStrContent.toString();
    }
}

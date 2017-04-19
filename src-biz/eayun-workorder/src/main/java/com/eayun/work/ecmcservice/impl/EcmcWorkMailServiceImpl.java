package com.eayun.work.ecmcservice.impl;

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
import com.eayun.ecmcuser.model.BaseEcmcSysUser;
import com.eayun.ecmcuser.model.EcmcSysUser;
import com.eayun.ecmcuser.service.EcmcSysUserService;
import com.eayun.mail.service.MailService;
import com.eayun.sms.service.SMSService;
import com.eayun.work.dao.NoSendSmsDao;
import com.eayun.work.ecmcservice.EcmcWorkMailService;
import com.eayun.work.model.BaseNoSendSms;
import com.eayun.work.model.BaseWorkOpinion;
import com.eayun.work.model.WorkOpinion;
import com.eayun.work.model.WorkUtils;
import com.eayun.work.model.Workorder;
@Service
@Transactional
public class EcmcWorkMailServiceImpl implements EcmcWorkMailService{
	@Autowired
	private EcmcSysUserService ecmcUserService;
	@Autowired
	private MailService mailService;
	@Autowired
	private SMSService smsService;
	@Autowired
    private NoSendSmsDao noSendSmsDao;//未发送的短信
	
	
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
    /**
	 * 新建，受理，处理，解决，完结，取消时发送邮件短信
	 * @param workorder
	 * @param workOpinion
	 * @throws Exception
	 */
    @Override
    public void sendMailAndSms(Workorder workorder,WorkOpinion workOpinion) throws Exception{
    	if(!StringUtil.isEmpty(workOpinion.getReplyUser()) && workOpinion.getReplyUser().equals(workOpinion.getCreUser())){//回复人等于创建人
    		return ;
    	}
    	
    	boolean bool=true;
    	//点击受理或者响应的时候不给客户发送短信和邮件
    	if(StringUtil.isEmpty(workorder.getSendMesFlag()) && "1".equals(workorder.getWorkFalg()) && "0,1".contains(workorder.getWorkEcscFalg())){
    		bool=false;
    	}
    	//判断申请人是否为空
		if("2".equals(workorder.getWorkCreRole()) && bool && "1,2,3,4".contains(workorder.getWorkEcscFalg()) && "0,3".contains(workorder.getSendMesFlag())){
    		//给申请人，也就是用户发送邮件和短信
    		this.sendEcscSmsAndMail(workorder, workOpinion);
    	}
		this.sendEcmcSmsAndMail(workorder, workOpinion);
    }
    /*
     * ecsc发送邮件和短信
     */
    @SuppressWarnings("deprecation")
    private void sendEcscSmsAndMail(Workorder workorder,WorkOpinion workOpinion) throws Exception{
    	//邮件集合
    	List<String> mailList = new ArrayList<String>();
    	//1.获取邮件内容
		String strContent=String.valueOf(EcmcReadMailHtml.getEcscMailHtml());
		strContent=strContent.replace("{userName}", StringUtil.isEmpty(workorder.getWorkApplyUserName())?"":workorder.getWorkApplyUserName());
		strContent=strContent.replace("{workTitle}", workorder.getWorkTitle());
		strContent=strContent.replace("{workCreDate}", dateFmt(workorder.getWorkEcscFalg().equals("0")?workorder.getWorkCreTime():new Date()));
        strContent=strContent.replace("{workNum}", workorder.getWorkNum());
        strContent=strContent.replace("{workOrderTypeName}", workorder.getWorkTypeName());
        strContent=strContent.replace("{workFalg}", workorder.getWorkEcscFalgName());
        //发送邮件
        String title=WorkUtils.getEcscTitleMap().get(workorder.getWorkEcscFalg());
        mailList.add(workorder.getWorkEmail());
        if(!workorder.getWorkEcscFalg().equals("1")){
        	mailService.send(title, strContent, mailList); 
        }
        //发送短信
        String smsContent = "";
        if(workorder.getWorkFalg().equals("0")){
        	smsContent = "尊敬的客户：您的工单已提交成功，正在等待运维工程师受理，请登录管理控制台查看及管理。";
        }else{
    		smsContent ="尊敬的客户：您有1条"+workorder.getWorkEcscFalgName()+"工单，请登录管理控制台及时处理。";
        	
        }
        List<String> smsList = new ArrayList<String>();
        smsList.add(workorder.getWorkPhone());
        int hours=new Date().getHours();
        Date date = new Date();
        //接受短信-------------客户
        if(WorkUtils.REGISTERTYPE.equals(workorder.getWorkType()) && !StringUtil.isEmpty(workorder.getWorkState()) && !StringUtil.isEmpty(workorder.getSendMesFlag())&& "3".equals(workorder.getSendMesFlag())){//注册类
	    	if(workorder.getWorkState().equals("1")){//通过
	    		smsService.send("尊敬的客户：您的申请已通过审核，请等待开通账号。", smsList);
	    	}else if(workorder.getWorkState().equals("2")){//未通过
	    		smsService.send("尊敬的客户：您的申请未通过审核，如有问题请致电400-606-6396", smsList);
	    	}
		}else if("0".equals(workorder.getWorkFalg()) || ("1,2,3,4".contains(workorder.getWorkEcscFalg()) && "3".equals(workorder.getSendMesFlag()))){
        	//任何时候都收取--------选择9：00-18：00。当前时间在时间断内
        	 if("1".equals(workorder.getWorkPhoneTime()) || ("0".equals(workorder.getWorkPhoneTime()) && 18>hours && hours>=9)){
        		 smsService.send(smsContent, smsList);
        	 }else if(workorder.getWorkPhoneTime().equals("0") && (18<=hours || hours<9)){//选择9：00-18：00。当前时间不在时间段之内
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
                 Date startDate = new Date(date.getTime());
                 baseNoSendSms.setSmsStart(startDate);
                 date.setHours(18);//设置成18点
                 Date endDate= new Date(date.getTime());
                 baseNoSendSms.setSmsEnd(endDate);
                 noSendSmsDao.save(baseNoSendSms);
        	 }
        }
    }
    /*
     * ecmc只发送邮件
     */
    private void sendEcmcSmsAndMail(Workorder workorder,WorkOpinion workOpinion) throws Exception{
    	List<String> mailList = new ArrayList<String>();
    	String title=WorkUtils.getEcmcTitleMap().get(workorder.getWorkFalg());
    	if("0".equals(workorder.getWorkFalg()) ||("4".equals(workorder.getWorkFalg()) && StringUtil.isEmpty(workorder.getWorkHeadUser()))){//创建或者取消
			String permission ="java:acceptAllOrder";
			List<EcmcSysUser> list = ecmcUserService.findUserByPermission(null,permission);
			if(WorkUtils.REGISTERTYPE.equals(workorder.getWorkType()) ||WorkUtils.QUOTATYPE.equals(workorder.getWorkType())){
				permission="java:acceptSpecialOrder";
			}else{
				permission="java:acceptMormalOrder";
			}
			List<EcmcSysUser> ecmcList = ecmcUserService.findUserByPermission(null,permission);
			list.addAll(ecmcList);
			list.add(ecmcUserService.findUserById(workorder.getWorkCreUser()));
        	for (EcmcSysUser ecmcSysUser : list) {
        		mailList.add(ecmcSysUser.getMail());
    		}
        	//邮件集合
        	workorder.setWorkCreUserName("用户");//群发的时候，模板是一样的。
        	String content = this.content(workorder,false,workOpinion);
         	mailService.send(title, content, mailList);
    	}else{
    		if(("1".equals(workorder.getWorkFalg()) && "0".equals(workorder.getWorkEcscFalg()))){//点击受理
				mailList.clear();
                //给责任人也发送一次
                BaseEcmcSysUser headUser = ecmcUserService.findUserById(workorder.getWorkHeadUser());
                if(headUser!=null && StringUtil.isEmail(headUser.getMail())){
                    mailList.add(headUser.getMail());
                }
                //邮件集合
				workorder.setWorkCreUserName(workorder.getWorkHeadUserName());//给责任人发送
                String content = this.content(workorder,false,workOpinion);
            	mailService.send(title, content, mailList);
            }
    		//接受人
    		if(!"2".equals(workorder.getWorkCreRole())){//控制台
				mailList.clear();
    			BaseEcmcSysUser headUser = ecmcUserService.findUserById(workOpinion.getReplyUser());
    			//工单创建者删除后不再需要给创建者发送邮件
    			if(headUser == null){
    				return ;
    			}
    			workorder.setWorkCreUserName(headUser.getName());//为后面content方法中，发送邮件的抬头赋值
    			mailList.add(headUser.getMail());
    			//邮件集合
    			String content = this.content(workorder,true,workOpinion);
            	mailService.send(title, content, mailList);
    		}
    	}
    	
    }
    private String content(Workorder workorder,boolean oneself,WorkOpinion workOpinion)throws Exception{
    	String ecmcStrContent = String.valueOf(EcmcReadMailHtml.getEcmcMailHtml());
    	StringBuffer contentStr = new StringBuffer();
		contentStr.append("工单：<span class='blue'>");
		contentStr.append(workorder.getWorkTitle());
		contentStr.append("</span> 已于");
    	if("0".equals(workorder.getWorkFalg())){
    		contentStr.append(dateFmt(workorder.getWorkCreTime()));
    		contentStr.append("已提交成功");
    	}else if("1".equals(workorder.getWorkFalg()) && !StringUtil.isEmpty(workOpinion.getOpinionState()) && !"0,1".contains(workOpinion.getOpinionState())){
			if(oneself && "0".equals(workorder.getFlowRespondFalg())){
        		contentStr.append(dateFmt(workorder.getFlowBeginTime()));
        		contentStr.append(" 已受理成功");
        	}else if(!oneself && "0".equals(workorder.getFlowRespondFalg())){
        		contentStr.append(dateFmt(workorder.getFlowBeginTime()));
        		contentStr.append(" 已被 运维工程师");
        		contentStr.append(workorder.getWorkHeadUserName());
        		contentStr.append(" 受理成功");
	    	}else if("1".equals(workorder.getFlowRespondFalg())){
	    		contentStr.append(dateFmt(workorder.getFlowRespondTime()));
	    		contentStr.append(" 已被 运维工程师");
	    		contentStr.append(workorder.getWorkHeadUserName());
	    		contentStr.append(" 响应");
	    	}
    	}else{
			contentStr.append(dateFmt(workorder.getWorkCreTime()));
			if("4".equals(workorder.getWorkFalg())){
				contentStr.append("已取消成功");
			}else{
				contentStr.append("提交成功");
			}
    	}
		ecmcStrContent=ecmcStrContent.replace("{work}", contentStr);
		ecmcStrContent=ecmcStrContent.replace("{userName}", StringUtil.isEmpty(workorder.getWorkCreUserName())?"客户":workorder.getWorkCreUserName());
    	ecmcStrContent=ecmcStrContent.replace("{workNum}", workorder.getWorkNum());
    	ecmcStrContent=ecmcStrContent.replace("{workCreTime}", dateFmt(workorder.getWorkCreTime()));
    	ecmcStrContent=ecmcStrContent.replace("{workLevel}", workorder.getWorkLevelName());
    	ecmcStrContent=ecmcStrContent.replace("{workType}", workorder.getWorkTypeName());
    	ecmcStrContent=ecmcStrContent.replace("{applyCustomer}", StringUtil.isEmpty(workorder.getWorkCusName())?"":workorder.getWorkCusName());
    	ecmcStrContent=ecmcStrContent.replace("{workFalg}", workorder.getWorkFalgName());
    	ecmcStrContent=ecmcStrContent.replace("{workContent}", StringUtil.isEmpty(workorder.getWorkContent())?"":workorder.getWorkContent());
    	ecmcStrContent=ecmcStrContent.replace("{opinionContent}", workOpinion.getOpinionContent());
    	return ecmcStrContent.toString();
    }
	/**
	 * 编辑工单时发送的邮件(负责人、创建人)
	 * @param workorder
	 * @param oldWorkorder
	 * @param ecmcUser
	 * @throws Exception
	 */
    @Override
	public void sendEmailMessageForEdit(Workorder workorder, Workorder oldWorkorder, BaseWorkOpinion baseWorkOpinion, EcmcSysUser ecmcUser) throws Exception{
		List<String> list = new ArrayList<String>();
		if(!StringUtil.isEmpty(workorder.getWorkHeadUser())){
			//获取责任人信息
			BaseEcmcSysUser workHeadUser = ecmcUserService.findUserById(workorder.getWorkHeadUser());
			if(!StringUtil.isEmpty(workHeadUser.getMail())){
				list.add(workHeadUser.getMail());
			}
		}
		//获取创建人信息
		BaseEcmcSysUser workCreUser = ecmcUserService.findUserById(workorder.getWorkCreUser());
		if(workCreUser != null){
			if(!StringUtil.isEmpty(workCreUser.getMail())){
				list.add(workCreUser.getMail());
			}
		}
		//邮件集合
		boolean bool=false;
    	String ecmcStrContent = String.valueOf(EcmcReadMailHtml.getEcmcMailHtml());
    	StringBuffer content=new StringBuffer();
		content.append("工单：");
    	content.append("<span class='blue'>"+workorder.getWorkTitle()+"</span>");
		content.append("的");
    	if(!workorder.getWorkTitle().equals(oldWorkorder.getWorkTitle())){//标题变化
    		content.append("工单标题");
		}else if(!workorder.getWorkContent().equals(oldWorkorder.getWorkContent())){//描述变化
			content.append("工单内容");
    	}else if(!workorder.getWorkLevel().equals(oldWorkorder.getWorkLevel())){//级别变化
			content.append("工单级别");
			bool=true;
		}
		content.append(" 于");
		content.append(dateFmt(new Date()));
		content.append(" 已被");
		//content.append(ecmcUser.getRoles().get(0).getName());
		content.append(ecmcUser.getName());
		content.append(" 重新编辑");
		
    	ecmcStrContent=ecmcStrContent.replace("{work}", content);
    	ecmcStrContent=ecmcStrContent.replace("{userName}", !StringUtil.isEmpty(workorder.getWorkHeadUser())?"用户":workorder.getWorkCreUserName());
    	ecmcStrContent=ecmcStrContent.replace("{workNum}", workorder.getWorkNum());
    	ecmcStrContent=ecmcStrContent.replace("{workCreTime}", dateFmt(workorder.getWorkCreTime()));
    	if(bool){
			ecmcStrContent=ecmcStrContent.replace("{workLevel}", "<span class='red'>"+workorder.getWorkLevelName()+"</span>");
		}else{
			ecmcStrContent=ecmcStrContent.replace("{workLevel}", workorder.getWorkLevelName());
		}
    	ecmcStrContent=ecmcStrContent.replace("{workType}", workorder.getWorkTypeName());
    	ecmcStrContent=ecmcStrContent.replace("{applyCustomer}", StringUtil.isEmpty(workorder.getWorkCusName())?"":workorder.getWorkCusName());
    	ecmcStrContent=ecmcStrContent.replace("{workFalg}", workorder.getWorkFalgName());
    	ecmcStrContent=ecmcStrContent.replace("{workContent}", oldWorkorder.getWorkContent());
    	ecmcStrContent=ecmcStrContent.replace("{opinionContent}", baseWorkOpinion.getOpinionContent());
		//发送邮件
		mailService.send("编辑工单", ecmcStrContent, list);
	}
}

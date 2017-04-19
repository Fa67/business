package com.eayun.ecmcwork.job;

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

import com.eayun.common.job.BaseQuartzJobBean;
import com.eayun.common.util.StringUtil;
import com.eayun.ecmcuser.model.EcmcSysUser;
import com.eayun.mail.service.MailService;
import com.eayun.work.ecmcservice.EcmcWorkorderService;
import com.eayun.work.ecmcservice.impl.EcmcReadMailHtml;
import com.eayun.work.model.Workorder;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class EcmcWorkorderJob extends BaseQuartzJobBean{
	 private final Logger      log = LoggerFactory.getLogger(EcmcWorkorderJob.class);
	 
	private MailService mailService ;//短信发送
	private EcmcWorkorderService ecmcWorkService;//工单
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		ApplicationContext applicationContext = getApplicationContext(context);
		ecmcWorkService = applicationContext.getBean(EcmcWorkorderService.class);
		mailService = (MailService) applicationContext.getBean("mailService");
		List<Workorder> workList=ecmcWorkService.getWorkorderListByFlag();//获取所有状态为未处理和处理中工单
		if(workList==null || workList.size()==0){
			log.info("不满足超过5分钟未受理或者响应发送条件。");
			return;
		}
		List<EcmcSysUser> adminList = ecmcWorkService.getEcmcAdmin();//管理员
		List<String> emailList= new ArrayList<>();
		int diff= 0;//时间差
		for (Workorder workorder : workList) {//等于2不发送跳出
			if("2".equals(workorder.getSendMesFlag())){
				log.info("不满足超过5分钟未受理或者响应发送条件。");
				continue;
			}
			if(workorder.getWorkFalg().equals("0")){//未处理状态
				Date date = new Date();//现在时间
				diff = (int) ((date.getTime()-workorder.getWorkCreTime().getTime())/(1000*60));//现在时间距创建工单时间长度
				if(diff>=5 && workorder.getSendMesFlag().equals("0")){//超过5分钟未受理，并且为发送短信的。
					log.info("超过5分钟未受理邮件开始");
					if(emailList.isEmpty()){
						for (EcmcSysUser ecmcSysUser : adminList) {
							if(StringUtil.isEmail(ecmcSysUser.getMail())){
								emailList.add(ecmcSysUser.getMail());
							}
						}
					}
					
					try {
						mailService.send("无人受理", this.content(workorder, true), emailList);
						ecmcWorkService.editWorkSendMessage(workorder.getWorkId(), "1");
					} catch (Exception e) {
						log.error("未受理超时邮件发送失败",e.getMessage());
					}
				}
			}else{//处理中状态
				Date date = new Date();//现在时间
				diff = (int) ((date.getTime()-workorder.getFlowBeginTime().getTime())/(1000*60));//开始受理未响应，距现在是时间差
				if(diff>5 && workorder.getSendMesFlag().equals("1") && workorder.getFlowRespondFalg().equals("0")){//超过5分钟未响应并且未发送短信
					log.info("超过5分钟未响应发送邮件开始");
					if(emailList.isEmpty()){
						for (EcmcSysUser ecmcSysUser : adminList) {
							if(StringUtil.isEmail(ecmcSysUser.getMail())){
								emailList.add(ecmcSysUser.getMail());
							}
						}
					}
					
					try {
						mailService.send("无人响应", this.content(workorder, false), emailList);
						ecmcWorkService.editWorkSendMessage(workorder.getWorkId(), "2");
					} catch (Exception e) {
						log.error("未响应超时邮件发送失败",e.getMessage());
					}
				}
			}
		}
	}
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
	private String content(Workorder workorder,boolean oneself) throws Exception{
    	String ecmcStrContent = String.valueOf(EcmcReadMailHtml.getEcmcMailHtml());
    	StringBuffer contentStr = new StringBuffer();
		if(oneself){
			contentStr.append("工单：");
			contentStr.append("<span class='blue'>");
    		contentStr.append(workorder.getWorkTitle());
    		contentStr.append("</span>");
    		contentStr.append("于");
    		contentStr.append(dateFmt(workorder.getWorkCreTime()));
    		contentStr.append("提交成功，");
    		contentStr.append("目前未被受理");
			ecmcStrContent=ecmcStrContent.replace("{work}", contentStr);
    	}else{
			contentStr.append("工单：");
			contentStr.append("<span class='blue'>");
    		contentStr.append(workorder.getWorkTitle());
    		contentStr.append("</span>");
    		contentStr.append("于");
    		contentStr.append(dateFmt(workorder.getFlowBeginTime()));
    		contentStr.append("已被 运维工程师");
    		contentStr.append(workorder.getWorkHeadUserName());
    		contentStr.append("成功受理，");
    		contentStr.append("目前未被响应");
			ecmcStrContent=ecmcStrContent.replace("{work}", contentStr);
    	}
		ecmcStrContent=ecmcStrContent.replace("{userName}", "管理员");
		ecmcStrContent=ecmcStrContent.replace("{workTitle}", workorder.getWorkTitle());
		ecmcStrContent=ecmcStrContent.replace("{workNum}", workorder.getWorkNum());
		ecmcStrContent=ecmcStrContent.replace("{workCreTime}", dateFmt(workorder.getWorkCreTime()));
		ecmcStrContent=ecmcStrContent.replace("{workLevel}", workorder.getWorkLevelName());
		ecmcStrContent=ecmcStrContent.replace("{workType}", workorder.getWorkTypeName());
		ecmcStrContent=ecmcStrContent.replace("{applyCustomer}", StringUtil.isEmpty(workorder.getWorkCusName())?"":workorder.getWorkCusName());
		ecmcStrContent=ecmcStrContent.replace("{workFalg}", StringUtil.isEmpty(workorder.getWorkFalgName())?"":workorder.getWorkFalgName());
		ecmcStrContent=ecmcStrContent.replace("{workContent}",StringUtil.isEmpty(workorder.getWorkContent())?"":workorder.getWorkContent());
		ecmcStrContent=ecmcStrContent.replace("{opinionContent}", contentStr);
    	return ecmcStrContent.toString();
    }
}

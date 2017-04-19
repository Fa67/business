package com.eayun.notice.operatedatasendmail.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import com.eayun.common.job.BaseQuartzJobBean;
import com.eayun.notice.operatedatasendmail.service.OperateDataSendMailService;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class OperateDataSendMailJob extends  BaseQuartzJobBean{
	 private static final Logger log = LoggerFactory.getLogger(OperateDataSendMailJob.class);
	 private OperateDataSendMailService operateDataSendMailService;

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		ApplicationContext applicationContext = getApplicationContext(context);
		operateDataSendMailService = applicationContext.getBean(OperateDataSendMailService.class);
		try{
			operateDataSendMailService.getOperateDataSendMail();
		}catch(Exception e){
			  log.error(e.getMessage(),e);
		}
		
		
	}

}

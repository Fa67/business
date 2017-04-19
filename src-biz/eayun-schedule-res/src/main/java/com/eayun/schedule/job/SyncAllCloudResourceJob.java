package com.eayun.schedule.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.context.ApplicationContext;

import com.eayun.common.job.BaseQuartzJobBean;
import com.eayun.schedule.service.SyncDataCenterService;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class SyncAllCloudResourceJob extends BaseQuartzJobBean {
	private SyncDataCenterService syncDataCenterservice;

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		ApplicationContext applicationContext = getApplicationContext(context);
		syncDataCenterservice = applicationContext.getBean(SyncDataCenterService.class);
		syncDataCenterservice.sync();
	}

}

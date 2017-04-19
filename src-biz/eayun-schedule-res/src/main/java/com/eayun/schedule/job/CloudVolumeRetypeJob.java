package com.eayun.schedule.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.context.ApplicationContext;

import com.eayun.common.job.BaseQuartzJobBean;
import com.eayun.schedule.service.SyncVolumeRetypeService;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class CloudVolumeRetypeJob extends BaseQuartzJobBean {
	private SyncVolumeRetypeService syncVolumeRetypeService;

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		ApplicationContext applicationContext = getApplicationContext(context);
		syncVolumeRetypeService = applicationContext.getBean(SyncVolumeRetypeService.class);
		syncVolumeRetypeService.sync();
	}

}

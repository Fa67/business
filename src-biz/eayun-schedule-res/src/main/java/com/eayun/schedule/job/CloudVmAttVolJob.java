package com.eayun.schedule.job;

import java.util.concurrent.ThreadPoolExecutor;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.context.ApplicationContext;

import com.eayun.common.constant.RedisKey;
import com.eayun.common.job.BaseQuartzJobBean;
import com.eayun.schedule.pool.SyncResourceStatutPool;
import com.eayun.schedule.service.CloudVmAttVolService;
import com.eayun.schedule.thread.status.CloudVmAttVolStatusThread;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class CloudVmAttVolJob extends BaseQuartzJobBean{
	private CloudVmAttVolService cloudVmAttVolService;
	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		ThreadPoolExecutor pool = SyncResourceStatutPool.pool;
		long size = SyncResourceStatutPool.maxSize-pool.getActiveCount();
		ApplicationContext applicationContext = getApplicationContext(context);
		cloudVmAttVolService = applicationContext.getBean(CloudVmAttVolService.class);
		
		long quenceSize = cloudVmAttVolService.size(RedisKey.volAttVmKey);
		if(size>quenceSize){
			size = quenceSize;
		}
		for(int i= 0; i<size ;i++){
			cloudVmAttVolService = applicationContext.getBean(CloudVmAttVolService.class);
			CloudVmAttVolStatusThread vmThread = new CloudVmAttVolStatusThread(cloudVmAttVolService);
			pool.submit(vmThread);
		}
	}
}

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
import com.eayun.schedule.service.CloudVmService;
import com.eayun.schedule.thread.status.CloudVmStatusThread;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class CloudVmJob extends BaseQuartzJobBean{
	
	private	CloudVmService cloudVmService;
	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		ThreadPoolExecutor pool = SyncResourceStatutPool.pool;
		int maxPoolSize = 100;
		long size = maxPoolSize-pool.getActiveCount();
		ApplicationContext applicationContext = getApplicationContext(context);
		cloudVmService = applicationContext.getBean(CloudVmService.class);
		long quenceSize = cloudVmService.size(RedisKey.vmKey);
		if(size>quenceSize){
			size = quenceSize;
		}
		for(int i= 0; i<size ;i++){
			cloudVmService = applicationContext.getBean(CloudVmService.class);
			CloudVmStatusThread vmThread = new CloudVmStatusThread(cloudVmService);
			pool.submit(vmThread);
		}
		
		
	}
	
	
}

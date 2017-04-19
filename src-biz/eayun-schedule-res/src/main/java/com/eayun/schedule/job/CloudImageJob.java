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
import com.eayun.schedule.service.CloudImageService;
import com.eayun.schedule.thread.status.CloudImageStatusThread;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class CloudImageJob  extends BaseQuartzJobBean{
	private CloudImageService cloudImageService;
	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		ThreadPoolExecutor pool = SyncResourceStatutPool.pool;
		int maxPoolSize = SyncResourceStatutPool.maxSize;
		ApplicationContext applicationContext = getApplicationContext(context);
		cloudImageService = applicationContext.getBean(CloudImageService.class);
		long size = maxPoolSize-pool.getActiveCount();
		long quenceSize = cloudImageService.size(RedisKey.imageKey);
		if(size>quenceSize){
			size = quenceSize;
		}
		for(int i= 0; i<size ;i++){
			cloudImageService = applicationContext.getBean(CloudImageService.class);
			CloudImageStatusThread vmThread = new CloudImageStatusThread(cloudImageService);
			pool.submit(vmThread);
		}
	}
}

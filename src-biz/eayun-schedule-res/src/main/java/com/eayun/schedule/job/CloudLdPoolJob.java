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
import com.eayun.schedule.service.CloudLdPoolService;
import com.eayun.schedule.thread.status.CloudLdPoolStatusThread;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class CloudLdPoolJob  extends BaseQuartzJobBean{
	private CloudLdPoolService cloudLdPoolService ;
	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		ThreadPoolExecutor pool = SyncResourceStatutPool.pool;
		ApplicationContext applicationContext = getApplicationContext(context);
		cloudLdPoolService = applicationContext.getBean(CloudLdPoolService.class);
		int maxPoolSize = 100;
		long size = maxPoolSize-pool.getActiveCount();
		long quenceSize = cloudLdPoolService.size(RedisKey.ldPoolKey);
		if(size>quenceSize){
			size = quenceSize;
		}
		for(int i= 0; i<size ;i++){
			cloudLdPoolService = applicationContext.getBean(CloudLdPoolService.class);
			CloudLdPoolStatusThread vmThread = new CloudLdPoolStatusThread(cloudLdPoolService);
			pool.submit(vmThread);
		}
	}

}

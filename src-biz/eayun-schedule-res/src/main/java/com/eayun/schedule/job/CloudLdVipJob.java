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
import com.eayun.schedule.service.CloudLdVipService;
import com.eayun.schedule.thread.status.CloudLdVipStatusThread;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class CloudLdVipJob  extends BaseQuartzJobBean{
	private CloudLdVipService cloudLdVipService;
	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		ThreadPoolExecutor pool = SyncResourceStatutPool.pool;
		ApplicationContext applicationContext = getApplicationContext(context);
		cloudLdVipService = applicationContext.getBean(CloudLdVipService.class);
		int maxPoolSize = 100;
		long size = maxPoolSize-pool.getActiveCount();
		long quenceSize = cloudLdVipService.size(RedisKey.ldVipKey);
		if(size>quenceSize){
			size = quenceSize;
		}
		for(int i= 0; i<size ;i++){
			cloudLdVipService = applicationContext.getBean(CloudLdVipService.class);
			CloudLdVipStatusThread vmThread = new CloudLdVipStatusThread(cloudLdVipService);
			pool.submit(vmThread);
		}
	}

}

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
import com.eayun.schedule.service.CloudVolumeSnapService;
import com.eayun.schedule.thread.status.CloudVolumeSnapStatusThread;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class CloudVolumeSnapJob  extends BaseQuartzJobBean{
	private CloudVolumeSnapService cloudVolumeSnapService;
	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		ThreadPoolExecutor pool = SyncResourceStatutPool.pool;
		int maxPoolSize = 100;
		long size = maxPoolSize-pool.getActiveCount();
		ApplicationContext applicationContext = getApplicationContext(context);
		cloudVolumeSnapService = applicationContext.getBean(CloudVolumeSnapService.class);
		long quenceSize = cloudVolumeSnapService.size(RedisKey.volSphKey);
		if(size>quenceSize){
			size = quenceSize;
		}
		for(int i= 0; i<size ;i++){
			cloudVolumeSnapService = applicationContext.getBean(CloudVolumeSnapService.class);
			CloudVolumeSnapStatusThread vmThread = new CloudVolumeSnapStatusThread(cloudVolumeSnapService);
			pool.submit(vmThread);
		}
	}
}

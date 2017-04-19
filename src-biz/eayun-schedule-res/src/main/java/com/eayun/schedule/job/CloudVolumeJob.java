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
import com.eayun.schedule.service.CloudVolumeService;
import com.eayun.schedule.thread.status.CloudVolumeStatusThread;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class CloudVolumeJob extends BaseQuartzJobBean{
	private CloudVolumeService cloudVolumeService;
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		ThreadPoolExecutor pool = SyncResourceStatutPool.pool;
		int maxPoolSize = 100;
		long size = maxPoolSize-pool.getActiveCount();
		ApplicationContext applicationContext = getApplicationContext(context);
		cloudVolumeService = applicationContext.getBean(CloudVolumeService.class);
		long quenceSize = cloudVolumeService.size(RedisKey.volKey);
		if(size>quenceSize){
			size = quenceSize;
		}
		for(int i= 0; i<size ;i++){
			cloudVolumeService = applicationContext.getBean(CloudVolumeService.class);
			CloudVolumeStatusThread vmThread = new CloudVolumeStatusThread(cloudVolumeService);
			pool.submit(vmThread);
		}
	}

}

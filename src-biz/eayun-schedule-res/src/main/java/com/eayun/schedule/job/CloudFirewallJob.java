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
import com.eayun.schedule.service.CloudFirewallService;
import com.eayun.schedule.thread.status.CloudFirewallStatusThread;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class CloudFirewallJob  extends BaseQuartzJobBean{
	private CloudFirewallService cloudFireWallService;
	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		ThreadPoolExecutor pool = SyncResourceStatutPool.pool;
		int maxPoolSize = SyncResourceStatutPool.maxSize;
		long size = maxPoolSize-pool.getActiveCount();
		ApplicationContext applicationContext = getApplicationContext(context);
		cloudFireWallService = applicationContext.getBean(CloudFirewallService.class);
		long quenceSize = cloudFireWallService.size(RedisKey.fwKey);
		if(size>quenceSize){
			size = quenceSize;
		}
		for(int i= 0; i<size ;i++){
			cloudFireWallService = applicationContext.getBean(CloudFirewallService.class);
			CloudFirewallStatusThread vmThread = new CloudFirewallStatusThread(cloudFireWallService);
			pool.submit(vmThread);
		}
	}

}

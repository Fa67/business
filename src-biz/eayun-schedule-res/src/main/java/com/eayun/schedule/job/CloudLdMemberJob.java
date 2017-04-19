package com.eayun.schedule.job;

import java.util.concurrent.ThreadPoolExecutor;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.context.ApplicationContext;

import com.eayun.common.constant.RedisKey;
import com.eayun.common.job.BaseQuartzJobBean;
import com.eayun.schedule.ScheduleResourceStartup;
import com.eayun.schedule.pool.SyncResourceStatutPool;
import com.eayun.schedule.service.CloudLdMemberService;
import com.eayun.schedule.thread.status.CloudLdMemberStatusThread;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class CloudLdMemberJob  extends BaseQuartzJobBean{
	private CloudLdMemberService cloudLdMemberService;
	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		ThreadPoolExecutor pool = SyncResourceStatutPool.pool;
		ApplicationContext applicationContext = getApplicationContext(context);
		cloudLdMemberService = applicationContext.getBean(CloudLdMemberService.class);
		int maxPoolSize = SyncResourceStatutPool.maxSize;
		long size = maxPoolSize-pool.getActiveCount();
		long quenceSize = cloudLdMemberService.size(RedisKey.ldMemberKey);
		if(size>quenceSize){
			size = quenceSize;
		}
		for(int i= 0; i<size ;i++){
			cloudLdMemberService = ScheduleResourceStartup.context.getBean(CloudLdMemberService.class);
			CloudLdMemberStatusThread vmThread = new CloudLdMemberStatusThread(cloudLdMemberService);
			pool.submit(vmThread);
		}
	}
}

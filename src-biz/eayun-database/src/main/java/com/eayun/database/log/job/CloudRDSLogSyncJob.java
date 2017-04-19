package com.eayun.database.log.job;

import java.util.List;
import java.util.concurrent.ExecutorService;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.context.ApplicationContext;

import com.eayun.common.job.BaseQuartzJobBean;
import com.eayun.database.instance.model.CloudRDSInstance;
import com.eayun.database.log.pool.CloudRDSLogSyncThreadPool;
import com.eayun.database.log.service.RDSLogService;
import com.eayun.database.log.thread.CloudRDSLogSyncThread;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class CloudRDSLogSyncJob extends BaseQuartzJobBean{
	private RDSLogService rdsLogService;
	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		ExecutorService pool = CloudRDSLogSyncThreadPool.pool;
		ApplicationContext applicationContext = getApplicationContext(context);
		rdsLogService = applicationContext.getBean(RDSLogService.class);
		List<CloudRDSInstance> list = rdsLogService.queryRdsInstanceForPublish(true);
		if(null != list && list.size()>0){
			for(CloudRDSInstance rdsInstance : list){
				Runnable thread = new CloudRDSLogSyncThread(rdsLogService,rdsInstance);
				pool.submit(thread);
			}
		}
	}
}

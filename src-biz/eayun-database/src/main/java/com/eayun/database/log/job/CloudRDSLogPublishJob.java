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
import com.eayun.database.log.pool.CloudRDSLogPublishThreadPool;
import com.eayun.database.log.service.RDSLogService;
import com.eayun.database.log.thread.CloudRDSLogPublishThread;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class CloudRDSLogPublishJob extends BaseQuartzJobBean{
	private RDSLogService rdsLogService;
	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		ExecutorService pool = CloudRDSLogPublishThreadPool.pool;
		ApplicationContext applicationContext = getApplicationContext(context);
		rdsLogService = applicationContext.getBean(RDSLogService.class);
		
		List<CloudRDSInstance> list = rdsLogService.queryRdsInstanceForPublish(false);
		for(CloudRDSInstance instance : list){
			Runnable thread = new CloudRDSLogPublishThread(rdsLogService,instance);
			pool.submit(thread);
		}
	}
}

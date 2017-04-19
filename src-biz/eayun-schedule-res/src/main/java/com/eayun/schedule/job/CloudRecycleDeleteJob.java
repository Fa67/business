package com.eayun.schedule.job;

import java.util.concurrent.ThreadPoolExecutor;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.eayun.common.job.BaseQuartzJobBean;
import com.eayun.schedule.pool.CloudRecycleDeletePool;
import com.eayun.schedule.service.CloudRecycleDeleteService;
import com.eayun.schedule.thread.recycle.CloudRecycleResourceThread;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class CloudRecycleDeleteJob extends BaseQuartzJobBean{
	private static final Logger log = LoggerFactory.getLogger(CloudRecycleDeleteJob.class);
	@Autowired
	private CloudRecycleDeleteService cloudRecycleDeleteService;
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		log.info("执行回收站的自动删除任务");
		ThreadPoolExecutor pool = CloudRecycleDeletePool.pool;
		ApplicationContext applicationContext = getApplicationContext(context);
		cloudRecycleDeleteService = applicationContext.getBean(CloudRecycleDeleteService.class);
		
		pool.submit(new CloudRecycleResourceThread(cloudRecycleDeleteService));
	}

}

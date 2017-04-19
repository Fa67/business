package com.eayun.obs.job.cdn;

import java.util.Date;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.eayun.common.job.BaseQuartzJobBean;
import com.eayun.obs.thread.cdn.CdnMonthSummaryPool;
import com.eayun.obs.thread.cdn.CdnMonthSummaryThread;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class CdnMonthSummaryJob extends BaseQuartzJobBean {
	
	private final Logger log = LoggerFactory.getLogger(CdnMonthSummaryJob.class);
	
	private MongoTemplate mongoTemplate;

	@Override
	protected void executeInternal(JobExecutionContext context)
			throws JobExecutionException {
		log.info("开始汇总客户上个月的CDN下载流量");
		ApplicationContext applicationContext = getApplicationContext(context);
		mongoTemplate = (MongoTemplate) applicationContext.getBean("mongoTemplate");
		
		Date now=new Date();
		CdnMonthSummaryThread thread = new CdnMonthSummaryThread(mongoTemplate,now);
		CdnMonthSummaryPool.pool.submit(thread);
	}

}

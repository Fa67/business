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
import com.eayun.obs.thread.cdn.CdnDaySummaryPool;
import com.eayun.obs.thread.cdn.CdnDaySummaryThread;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class CdnDaySummaryJob extends BaseQuartzJobBean {
	
private final Logger log = LoggerFactory.getLogger(CdnDaySummaryJob.class);
	
	private MongoTemplate mongoTemplate;
	
	@Override
	protected void executeInternal(JobExecutionContext context)
			throws JobExecutionException {
		log.info("开始汇总客户每天CDN下载流量");
		ApplicationContext applicationContext = getApplicationContext(context);
		mongoTemplate = (MongoTemplate) applicationContext.getBean("mongoTemplate");
		
		Date now=new Date();
		CdnDaySummaryThread thread = new CdnDaySummaryThread(mongoTemplate,now);
		CdnDaySummaryPool.pool.submit(thread);

	}

}

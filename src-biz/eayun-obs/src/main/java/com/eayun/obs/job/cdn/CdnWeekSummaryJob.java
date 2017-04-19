package com.eayun.obs.job.cdn;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.eayun.common.job.BaseQuartzJobBean;
import com.eayun.common.redis.JedisUtil;
import com.eayun.obs.thread.cdn.CdnWeekSummaryPool;
import com.eayun.obs.thread.cdn.CdnWeekSummaryThread;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class CdnWeekSummaryJob extends BaseQuartzJobBean {

	private final Logger log = LoggerFactory.getLogger(CdnWeekSummaryJob.class);
	
	private MongoTemplate mongoTemplate;
	
	private JedisUtil jedisUtil;
	
	@Override
	protected void executeInternal(JobExecutionContext context)
			throws JobExecutionException {
		log.info("开始汇总上周CDN下载流量Top10");
		ApplicationContext applicationContext = getApplicationContext(context);
		mongoTemplate = (MongoTemplate) applicationContext.getBean("mongoTemplate");
		jedisUtil = (JedisUtil) applicationContext.getBean("jedisUtil");
		
		CdnWeekSummaryThread thread = new CdnWeekSummaryThread(mongoTemplate,jedisUtil);
		CdnWeekSummaryPool.pool.submit(thread);
	}

}

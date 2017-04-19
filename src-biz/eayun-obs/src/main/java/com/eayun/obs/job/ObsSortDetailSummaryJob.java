package com.eayun.obs.job;

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
import com.eayun.obs.thread.ObsSortDetailSummaryPool;
import com.eayun.obs.thread.ObsSortDetailSummaryThread;

/**
 * 对象存储Top10采集计划任务
 * 
 * @Filename: obsDetailGatherJob.java
 * @Description:
 * @Version: 1.0
 * @Author: chengxiaodong
 * @Email: xiaodong.cheng@eayun.com
 * @History:<br> <li>Date: 2016年1月20日</li>
 *<li>Version: 1.0</li>
 *<li>Content:create</li>
 * 
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class ObsSortDetailSummaryJob extends BaseQuartzJobBean {

	private final Logger log = LoggerFactory.getLogger(ObsSortDetailSummaryJob.class);
	private MongoTemplate mongoTemplate;
	private JedisUtil         jedisUtil;


	@Override
	protected void executeInternal(JobExecutionContext context)
			throws JobExecutionException {
		log.info("进入下载流量、请求次数上周top10Job");
		ApplicationContext applicationContext = getApplicationContext(context);
		mongoTemplate = (MongoTemplate) applicationContext.getBean("mongoTemplate");
		jedisUtil = (JedisUtil) applicationContext.getBean("jedisUtil");
		
		
		ObsSortDetailSummaryThread thread = new ObsSortDetailSummaryThread(mongoTemplate,jedisUtil);
		ObsSortDetailSummaryPool.pool.submit(thread);
	

	}

}

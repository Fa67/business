package com.eayun.news.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.eayun.common.job.BaseQuartzJobBean;
import com.eayun.news.service.NewsDeleteService;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class NewsDeleteJob  extends BaseQuartzJobBean {
    private static final Logger log = LoggerFactory.getLogger(NewsDeleteJob.class);
	private NewsDeleteService newsDeleteService;

	
	@Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
	    ApplicationContext applicationContext = getApplicationContext(context);
	    newsDeleteService = applicationContext.getBean(NewsDeleteService.class);
		try {
			newsDeleteService.deleteNews();
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
		}
	}
}

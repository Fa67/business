package com.eayun.virtualization.job;


import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.eayun.common.job.BaseQuartzJobBean;
import com.eayun.virtualization.service.ResourceDisposeService;
/**
 * 资源到期处理(未超过保留时长)JOB
 * @author xiangyu.cao@eayun.com
 *
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class ResourceExpirationJob extends BaseQuartzJobBean{
	 private final Logger      log = LoggerFactory.getLogger(ResourceExpirationJob.class);

	   private ResourceDisposeService resourceExpirationService;


	    @Override
	    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
	        log.info("开始进行云资源到期处理");
	        ApplicationContext applicationContext = getApplicationContext(context);
	        resourceExpirationService = (ResourceDisposeService) applicationContext.getBean(ResourceDisposeService.class);
	        try {
				resourceExpirationService.resourceExpiration();
			} catch (Exception e) {
				log.error(e.getMessage(),e);
			}
	    }
	}

package com.eayun.log.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.eayun.common.job.BaseQuartzJobBean;
import com.eayun.log.service.DeleteLogService;
/**
 * 定时删除日志
 *                       
 * @Filename: SysLogJob.java
 * @Description: 
 * @Version: 1.0
 * @Author:wang ning
 * @Email: ning.wang@eayun.com
 * @History:<br>
 *<li>Date: 2015年9月14日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class SysLogJob extends BaseQuartzJobBean{
	private static final Logger     log    = LoggerFactory.getLogger(SysLogJob.class);
	 
	private DeleteLogService  deleteLogService;

//	public void setDeleteLogService(DeleteLogService deleteLogService) {
//		this.deleteLogService = deleteLogService;
//	}

	@Override
	protected void executeInternal(JobExecutionContext context)
			throws JobExecutionException {
	        log.info("刪除日志");
	        ApplicationContext applicationContext = getApplicationContext(context);
	        deleteLogService = applicationContext.getBean(DeleteLogService.class);
	        deleteLogService.deleteLog();
	}
	
}

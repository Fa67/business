package com.eayun.obs.job;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.eayun.accesskey.model.AccessKey;
import com.eayun.accesskey.service.AccessKeyService;
import com.eayun.common.job.BaseQuartzJobBean;
import com.eayun.common.template.EayunRabbitTemplate;
import com.eayun.obs.base.service.ObsBaseService;
import com.eayun.obs.service.ObsGetAllUsersService;
import com.eayun.obs.thread.ObsDetailGatherPool;
import com.eayun.obs.thread.ObsDetailGatherThread;

/**
 * 对象存储用量信息采集计划任务
 * 
 * @Filename: obsDetailGatherJob.java
 * @Description:
 * @Version: 1.0
 * @Author: chengxiaodong
 * @Email: xiaodong.cheng@eayun.com
 * @History:<br> <li>Date: 2016年1月12日</li>
 *<li>Version: 1.0</li>
 *<li>Content:create</li>
 * 
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class ObsDetailGatherJob extends BaseQuartzJobBean {

	private final Logger log = LoggerFactory.getLogger(ObsDetailGatherJob.class);
	private MongoTemplate mongoTemplate;
	private ObsGetAllUsersService obsGetAllUsersService;
	private ObsBaseService obsBaseService;
	private AccessKeyService accessKeyService;
	private EayunRabbitTemplate eayunRabbitTemplate;


	@Override
	protected void executeInternal(JobExecutionContext context)
			throws JobExecutionException {
		log.info("进入对象存储下载流量、请求次数Job");
		ApplicationContext applicationContext = getApplicationContext(context);
		obsGetAllUsersService = (ObsGetAllUsersService) applicationContext.getBean(ObsGetAllUsersService.class);
		obsBaseService = (ObsBaseService) applicationContext.getBean(ObsBaseService.class);
		accessKeyService = (AccessKeyService) applicationContext.getBean(AccessKeyService.class);
		mongoTemplate = (MongoTemplate) applicationContext.getBean("mongoTemplate");
		eayunRabbitTemplate = (EayunRabbitTemplate) applicationContext.getBean(EayunRabbitTemplate.class);
		Date now=new Date();
		Calendar calendar=Calendar.getInstance();
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		now=calendar.getTime();
		
		// 得到全部客户
		List<String> obsUsers = null;
		try {
			obsUsers = obsGetAllUsersService.getObsAllUsers();
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		if(obsUsers != null){
		      for (String cusId : obsUsers) {
		            AccessKey accessKey = null;
		            try {
		                accessKey = accessKeyService.getDefaultAK(cusId);
		            } catch (Exception e) {
		                log.error(e.getMessage(),e);
		            }
		            if (null == accessKey) {
		                continue;
		            }
		            ObsDetailGatherThread thread = new ObsDetailGatherThread(mongoTemplate, obsBaseService, accessKey, cusId,now,eayunRabbitTemplate);
		            ObsDetailGatherPool.pool.submit(thread);
		     }
		}
	}

}

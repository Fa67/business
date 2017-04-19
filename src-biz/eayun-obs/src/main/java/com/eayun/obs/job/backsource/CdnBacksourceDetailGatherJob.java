package com.eayun.obs.job.backsource;

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

import com.eayun.cdn.impl.UpYunCDN;
import com.eayun.cdn.intf.CDN;
import com.eayun.common.job.BaseQuartzJobBean;
import com.eayun.common.template.EayunRabbitTemplate;
import com.eayun.obs.service.ObsCdnBucketService;
import com.eayun.obs.service.ObsGetAllUsersService;
import com.eayun.obs.thread.backsource.CdnBacksourceDetailGatherPool;
import com.eayun.obs.thread.backsource.CdnBacksourceDetailGatherThread;

/**
 * 采集bucket通过CDN下载的回源流量
 *                       
 * @Filename: ObsBacksourceDetailGatherJob.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2016年10月20日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class CdnBacksourceDetailGatherJob extends BaseQuartzJobBean {

	private final Logger log = LoggerFactory.getLogger(CdnBacksourceDetailGatherJob.class);
	
	private MongoTemplate mongoTemplate;
	
	private ObsGetAllUsersService obsGetAllUsersService;
	
	private ObsCdnBucketService obsCdnBucketService;
	
	private CDN cdn;
	
	private EayunRabbitTemplate eayunRabbitTemplate;
	
	@Override
	protected void executeInternal(JobExecutionContext context)
			throws JobExecutionException {
		log.info("统计回源流量Job");
		ApplicationContext applicationContext = getApplicationContext(context);
		cdn = applicationContext.getBean(UpYunCDN.class);
		mongoTemplate = (MongoTemplate) applicationContext.getBean("mongoTemplate");
		obsCdnBucketService = (ObsCdnBucketService) applicationContext.getBean(ObsCdnBucketService.class);
		obsGetAllUsersService = (ObsGetAllUsersService) applicationContext.getBean(ObsGetAllUsersService.class);
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
		    	  CdnBacksourceDetailGatherThread thread = new CdnBacksourceDetailGatherThread(cdn,mongoTemplate,obsCdnBucketService ,eayunRabbitTemplate, cusId,now);
		    	  CdnBacksourceDetailGatherPool.pool.submit(thread);
		     }
		}
	}

}

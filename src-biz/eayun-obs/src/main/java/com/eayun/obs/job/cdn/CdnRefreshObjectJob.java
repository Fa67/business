package com.eayun.obs.job.cdn;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.eayun.cdn.impl.UpYunCDN;
import com.eayun.cdn.intf.CDN;
import com.eayun.common.job.BaseQuartzJobBean;
import com.eayun.common.redis.JedisUtil;
import com.eayun.obs.service.ObsCdnBucketService;
import com.eayun.obs.thread.cdn.CdnRefreshObjectPool;
import com.eayun.obs.thread.cdn.CdnRefreshObjectThread;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class CdnRefreshObjectJob extends BaseQuartzJobBean {
	
	private final Logger log = LoggerFactory.getLogger(CdnRefreshObjectJob.class);

	private JedisUtil jedisUtil;
	
	private ObsCdnBucketService obsCdnBucketService;
	
	private CDN cdn;
	
	@Override
	protected void executeInternal(JobExecutionContext context)
			throws JobExecutionException {
		log.info("开始处理底层操作object消息");
		ApplicationContext applicationContext = getApplicationContext(context);
		jedisUtil = (JedisUtil) applicationContext.getBean("jedisUtil");
		obsCdnBucketService = (ObsCdnBucketService) applicationContext.getBean(ObsCdnBucketService.class);
		cdn = applicationContext.getBean(UpYunCDN.class);
		
		long size = jedisUtil.sizeOfList("CDN_REFRESH:SYNCOBJECT");
		for(int i = 0;i < size;i++){
			CdnRefreshObjectThread thread = new CdnRefreshObjectThread(jedisUtil,cdn,obsCdnBucketService);
			CdnRefreshObjectPool.pool.submit(thread);
		}
		
	}

}

package com.eayun.obs.job.cdn;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.eayun.cdn.impl.ALiDNS;
import com.eayun.cdn.impl.UpYunCDN;
import com.eayun.cdn.intf.CDN;
import com.eayun.common.job.BaseQuartzJobBean;
import com.eayun.common.redis.JedisUtil;
import com.eayun.obs.service.ObsCdnBucketService;
import com.eayun.obs.thread.cdn.CdnRefreshBucketPool;
import com.eayun.obs.thread.cdn.CdnRefreshBucketThread;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class CdnRefreshBucketJob extends BaseQuartzJobBean {

	private final Logger log = LoggerFactory.getLogger(CdnRefreshBucketJob.class);

	private JedisUtil jedisUtil;
	
	private ObsCdnBucketService obsCdnBucketService;
	
	private CDN cdn;
	
	private ALiDNS dns;
	
	@Override
	protected void executeInternal(JobExecutionContext context)
			throws JobExecutionException {
		log.info("开始处理底层操作bucket消息");
		ApplicationContext applicationContext = getApplicationContext(context);
		jedisUtil = (JedisUtil) applicationContext.getBean("jedisUtil");
		obsCdnBucketService = (ObsCdnBucketService) applicationContext.getBean(ObsCdnBucketService.class);
		cdn = applicationContext.getBean(UpYunCDN.class);
		dns = applicationContext.getBean(ALiDNS.class);
		
		long size = jedisUtil.sizeOfList("CDN_REFRESH:SYNCBUCKET");
		for(int i = 0;i < size;i++){
			CdnRefreshBucketThread thread = new CdnRefreshBucketThread(jedisUtil,cdn,dns,obsCdnBucketService);
			CdnRefreshBucketPool.pool.submit(thread);
		}
	}

}

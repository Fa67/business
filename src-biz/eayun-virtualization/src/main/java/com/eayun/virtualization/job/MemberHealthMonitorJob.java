package com.eayun.virtualization.job;

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

import com.eayun.common.job.BaseQuartzJobBean;
import com.eayun.common.redis.JedisUtil;
import com.eayun.eayunstack.service.OpenstackMemberService;
import com.eayun.monitor.service.LdPoolAlarmMonitorService;
import com.eayun.virtualization.ecmcservice.EcmcLBPoolService;
import com.eayun.virtualization.model.CloudLdPool;
import com.eayun.virtualization.service.HealthMonitorService;
import com.eayun.virtualization.service.MemberService;
import com.eayun.virtualization.thread.MemberHealthMonitorPool;
import com.eayun.virtualization.thread.MemberHealthMonitorThread;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class MemberHealthMonitorJob  extends BaseQuartzJobBean{
	private final Logger log = LoggerFactory
			.getLogger(MemberHealthMonitorJob.class);
	private EcmcLBPoolService ecmcLBPoolService;
	private MemberService memberService;
	private HealthMonitorService healthMonitorService;
	private OpenstackMemberService openstackMemberService;
	private JedisUtil jedisUtil;
	private LdPoolAlarmMonitorService ldPoolAlarmMonitorService;
	private MongoTemplate mongoTemplate;
	@Override
	protected void executeInternal(JobExecutionContext context)
			throws JobExecutionException {
		log.info("开始进行成员状态计划任务");
		ApplicationContext applicationContext = getApplicationContext(context);
		ecmcLBPoolService = applicationContext.getBean(EcmcLBPoolService.class);
		memberService = applicationContext.getBean(MemberService.class);
		healthMonitorService = applicationContext.getBean(HealthMonitorService.class);
		openstackMemberService = applicationContext.getBean(OpenstackMemberService.class);
		jedisUtil = applicationContext.getBean(JedisUtil.class);
		ldPoolAlarmMonitorService = applicationContext.getBean(LdPoolAlarmMonitorService.class);
		mongoTemplate = applicationContext.getBean(MongoTemplate.class);
		try {
		List<CloudLdPool> list=	ecmcLBPoolService.getAllPoolList();
		log.info("开始对所有负载均衡下成员的状态进行同步,本次同步共有"+list.size()+"个负载均衡");
		Date jobStartTime=new Date();
		for (CloudLdPool cloudLdPool : list) {
			log.info("开始对负载均衡"+cloudLdPool.getPoolId()+"下成员的进行改变");
			MemberHealthMonitorThread thread=new MemberHealthMonitorThread(cloudLdPool, memberService, healthMonitorService, openstackMemberService, jedisUtil, ldPoolAlarmMonitorService, mongoTemplate,jobStartTime);
			MemberHealthMonitorPool.pool.submit(thread);
		}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
	}
}

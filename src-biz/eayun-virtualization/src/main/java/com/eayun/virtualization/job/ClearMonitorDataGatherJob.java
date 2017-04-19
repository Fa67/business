package com.eayun.virtualization.job;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.job.BaseQuartzJobBean;
import com.eayun.common.redis.JedisUtil;
import com.eayun.virtualization.thread.ClearMonitorDataGatherPool;
import com.eayun.virtualization.thread.ClearMonitorDataGatherThread;

/**
 * 每天凌晨1点30分清除已删除的资源的监控指标数据
 * @Filename: ClearMonitorDataGatherJob.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2017年3月16日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class ClearMonitorDataGatherJob extends BaseQuartzJobBean {
	
	private final Logger      log = LoggerFactory.getLogger(ClearMonitorDataGatherJob.class);

    private MongoTemplate     mongoTemplate;
    private JedisUtil         jedisUtil;
	@Override
	protected void executeInternal(JobExecutionContext context)
			throws JobExecutionException {
		log.info("开始清除已删除资源的监控数据...");
		ApplicationContext applicationContext = getApplicationContext(context);
		mongoTemplate = (MongoTemplate) applicationContext.getBean("mongoTemplate");
        jedisUtil = (JedisUtil) applicationContext.getBean("jedisUtil");
        try {
        	Set<String> idSet = jedisUtil.getSet(RedisKey.MONITOR_ITEM_DELETE);
        	if(idSet.isEmpty()){
        		log.info("无需要清除监控数据的资源...");
				return;
        	}
        	for(String strJson : idSet){
        		JSONObject json = JSONObject.parseObject(strJson);
        		ClearMonitorDataGatherThread thread = new ClearMonitorDataGatherThread(mongoTemplate, jedisUtil,json);
    			ClearMonitorDataGatherPool.pool.submit(thread);
        	}
		} catch (Exception e) {
			log.error("取出清除监控指标数据队列错误",e);
		}

	}

}

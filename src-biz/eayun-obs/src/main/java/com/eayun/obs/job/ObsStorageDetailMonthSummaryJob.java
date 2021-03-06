package com.eayun.obs.job;

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
import com.eayun.obs.service.ObsGetAllUsersService;
import com.eayun.obs.thread.ObsStorageDetailMonthSummaryPool;
import com.eayun.obs.thread.ObsStorageDetailMonthSummaryThread;

/**
 * 对象存储用量信息汇总计划任务
 *                       
 * @Filename: obsDetailGatherJob.java
 * @Description: 
 * @Version: 1.0
 * @Author: chengxiaodong
 * @Email: xiaodong.cheng@eayun.com
 * @History:<br>
 *<li>Date: 2016年1月14日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class ObsStorageDetailMonthSummaryJob extends BaseQuartzJobBean {
	
    private final Logger      log = LoggerFactory.getLogger(ObsStorageDetailMonthSummaryJob.class);
    private MongoTemplate     mongoTemplate;
    private ObsGetAllUsersService obsGetAllUsersService;
    private AccessKeyService accessKeyService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("进入存储容量历史账单汇总Job");
        ApplicationContext applicationContext = getApplicationContext(context);
        obsGetAllUsersService=(ObsGetAllUsersService)applicationContext.getBean(ObsGetAllUsersService.class);
        accessKeyService=(AccessKeyService)applicationContext.getBean(AccessKeyService.class);
        mongoTemplate = (MongoTemplate)applicationContext.getBean("mongoTemplate");
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
		            if(null==accessKey){
		                continue;
		            }
		            ObsStorageDetailMonthSummaryThread thread = new ObsStorageDetailMonthSummaryThread(mongoTemplate,cusId);
		            ObsStorageDetailMonthSummaryPool.pool.submit(thread);
		        }
		}
    }


}

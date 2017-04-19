package com.eayun.database.monitor.job;

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
import com.eayun.common.util.DateUtil;
import com.eayun.database.instance.model.CloudRDSInstance;
import com.eayun.database.instance.service.CloudRDSInstanceService;
import com.eayun.database.monitor.service.InstanceMonitorService;
import com.eayun.database.monitor.thread.InstanceVolumeDetailGatherPool;
import com.eayun.database.monitor.thread.InstanceVolumeDetailGatherThread;
import com.eayun.project.service.ProjectService;
import com.eayun.virtualization.model.CloudProject;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class InstanceVolumeDetailGatherJob extends BaseQuartzJobBean {

	private final Logger      log = LoggerFactory.getLogger(InstanceVolumeDetailGatherJob.class);

    private MongoTemplate     		mongoTemplate;
    private CloudRDSInstanceService cloudRDSInstanceService;
    private ProjectService    		projectService;
    private InstanceMonitorService  instanceMonitorService;
    private JedisUtil         		jedisUtil;
    
	@Override
	protected void executeInternal(JobExecutionContext context)
			throws JobExecutionException {
		log.info("开始采集数据库实例磁盘使用率指标");
        ApplicationContext applicationContext = getApplicationContext(context);
        projectService =  applicationContext.getBean(ProjectService.class);
        instanceMonitorService =  applicationContext.getBean(InstanceMonitorService.class);
        cloudRDSInstanceService = applicationContext.getBean(CloudRDSInstanceService.class);
        mongoTemplate = (MongoTemplate) applicationContext.getBean("mongoTemplate");
        jedisUtil = (JedisUtil) applicationContext.getBean("jedisUtil");
        Date now = new Date();
        now = DateUtil.dateRemoveSec(now);
        //得到全部未删除的数据库实例
        List<CloudProject> projectList = projectService.getAllProjects();
        for (CloudProject project : projectList) {
        	List<CloudRDSInstance> rdsList = cloudRDSInstanceService.getRDSListByPrjId(project.getProjectId());
        	for(CloudRDSInstance rds : rdsList){
        		InstanceVolumeDetailGatherThread thread = new InstanceVolumeDetailGatherThread
        				(project, rds,mongoTemplate, instanceMonitorService, jedisUtil,now);
            	InstanceVolumeDetailGatherPool.pool.submit(thread);
        	}
        	
        }
	}

}

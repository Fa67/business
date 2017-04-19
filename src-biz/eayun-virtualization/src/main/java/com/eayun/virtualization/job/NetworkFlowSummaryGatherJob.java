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
import com.eayun.common.util.DateUtil;
import com.eayun.datacenter.model.DcDataCenter;
import com.eayun.datacenter.service.DataCenterService;
import com.eayun.project.service.ProjectService;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.thread.NetworkFlowSummaryGatherPool;
import com.eayun.virtualization.thread.NetworkFlowSummaryGatherThread;

/**
 *                       
 * @Filename: NetworkFlowSummaryGatherJob.java
 * @Description: 
 * @Version: 1.0
 * @Author: chenhao
 * @Email: hao.chen@eayun.com
 * @History:<br>
 *<li>Date: 2015年12月8日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class NetworkFlowSummaryGatherJob extends BaseQuartzJobBean {

    private final Logger      log = LoggerFactory.getLogger(NetworkFlowSummaryGatherJob.class);

    private MongoTemplate     mongoTemplate;

    private ProjectService    projectService;

    private DataCenterService dataCenterService;
    
    /**
     * 计划任务：每天0点5分执行汇总昨天的流量
     * @param context
     * @throws JobExecutionException
     */
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("开始汇总流量数据");
        
        ApplicationContext applicationContext = getApplicationContext(context);
        dataCenterService = applicationContext.getBean(DataCenterService.class);
        projectService = applicationContext.getBean(ProjectService.class);
        mongoTemplate = (MongoTemplate) applicationContext.getBean("mongoTemplate");
        
        List<DcDataCenter> datacenterList = dataCenterService.getAllList();
        Date now = new Date();
        now = DateUtil.dateRemoveSec(now);
        for (DcDataCenter dataCenter : datacenterList) {
            List<CloudProject> projectList = projectService.getProjectListByDataCenter(dataCenter
                .getId());
            for (CloudProject project : projectList) {
                NetworkFlowSummaryGatherThread thread = new NetworkFlowSummaryGatherThread(mongoTemplate,project,now);
                NetworkFlowSummaryGatherPool.pool.submit(thread);
            }
        }
    }

}

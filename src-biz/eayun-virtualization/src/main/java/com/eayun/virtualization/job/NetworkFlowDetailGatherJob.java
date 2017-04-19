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
import com.eayun.eayunstack.service.OpenstackMeterService;
import com.eayun.project.service.ProjectService;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.thread.NetworkFlowDetailGatherPool;
import com.eayun.virtualization.thread.NetworkFlowDetailGatherThread;

/**
 *                       
 * @Filename: NetworkFlowDetailGatherJob.java
 * @Description: 
 * @Version: 1.0
 * @Author: chenhao
 * @Email: hao.chen@eayun.com
 * @History:<br>
 *<li>Date: 2015年12月7日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class NetworkFlowDetailGatherJob extends BaseQuartzJobBean {

    private final Logger      log = LoggerFactory.getLogger(NetworkFlowDetailGatherJob.class);

    private MongoTemplate     mongoTemplate;

    private ProjectService    projectService;

    private DataCenterService dataCenterService;
    
    private OpenstackMeterService openstackMeterService;
    
    
    /**
     * 计划任务：每分钟执行一次，但根据底层采集数据的时间点，只有底层采集到数据时才插入mongo数据
     * @param context
     */
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("开始采集流量数据");
        ApplicationContext applicationContext = getApplicationContext(context);
        dataCenterService = applicationContext.getBean(DataCenterService.class);
        projectService = applicationContext.getBean(ProjectService.class);
        openstackMeterService = (OpenstackMeterService) applicationContext.getBean("openstackMeterService");
        mongoTemplate = (MongoTemplate) applicationContext.getBean("mongoTemplate");
        Date now = new Date();
        now = DateUtil.dateRemoveSec(now);
        
        List<DcDataCenter> datacenterList = dataCenterService.getAllList();

        for (DcDataCenter dataCenter : datacenterList) {
            List<CloudProject> projectList = projectService.getProjectListByDataCenter(dataCenter
                .getId());
            for (CloudProject project : projectList) {
                    NetworkFlowDetailGatherThread thread = new NetworkFlowDetailGatherThread(
                        dataCenter, project , mongoTemplate,openstackMeterService , now);
                    NetworkFlowDetailGatherPool.pool.submit(thread);
            }
        }
    }

}

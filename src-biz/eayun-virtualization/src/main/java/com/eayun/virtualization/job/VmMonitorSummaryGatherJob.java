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
import com.eayun.virtualization.model.CloudVm;
import com.eayun.virtualization.service.VmMonitorService;
import com.eayun.virtualization.service.VmService;
import com.eayun.virtualization.thread.VmMonitorSummaryGatherPool;
import com.eayun.virtualization.thread.VmMonitorSummaryGatherThread;

/**
 * 虚拟机监控指标数据采集定时汇总计划任务
 *                       
 * @Filename: VMMonitorDetailGatherJob.java
 * @Description: 
 * @Version: 1.0
 * @Author: chenhao
 * @Email: hao.chen@eayun.com
 * @History:<br>
 *<li>Date: 2015年12月15日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class VmMonitorSummaryGatherJob extends BaseQuartzJobBean {

    private final Logger      log = LoggerFactory.getLogger(VmMonitorSummaryGatherJob.class);

    private MongoTemplate     mongoTemplate;
    private VmService         vmService;
    private ProjectService    projectService;
    private DataCenterService dataCenterService;
    private VmMonitorService  vmMonitorService;
    private String            interval;

    public void setInterval(String interval) {
        this.interval = interval;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("开始汇总主机指标");
        
        ApplicationContext applicationContext = getApplicationContext(context);
        dataCenterService =  applicationContext.getBean(DataCenterService.class);
        projectService =  applicationContext.getBean(ProjectService.class);
        vmMonitorService =  applicationContext.getBean(VmMonitorService.class);
        vmService =  applicationContext.getBean(VmService.class);
        mongoTemplate = (MongoTemplate) applicationContext.getBean("mongoTemplate");
        Date now = new Date();
        // 得到全部未删除的主机
        List<DcDataCenter> datacenterList = dataCenterService.getAllList();
        for (DcDataCenter dataCenter : datacenterList) {
            List<CloudProject> projectList = projectService.getProjectListByDataCenter(dataCenter
                .getId());
            for (CloudProject project : projectList) {
                List<CloudVm> vmList = vmService
                    .getUnDeletedVmListByProject(project.getProjectId());
                for (CloudVm vm : vmList) {
                    VmMonitorSummaryGatherThread thread = new VmMonitorSummaryGatherThread(project,
                        vm, mongoTemplate, vmMonitorService, interval,now);
                    VmMonitorSummaryGatherPool.pool.submit(thread);
                }
            }
        }
    }
}

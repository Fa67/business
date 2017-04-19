package com.eayun.work.job;


import com.eayun.common.job.BaseQuartzJobBean;
import com.eayun.work.dao.WorkOpinionDao;
import com.eayun.work.model.WorkOrderState;
import com.eayun.work.model.Workorder;
import com.eayun.work.service.WorkorderService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.List;

/**
 * 工单自动确认以及自动评价任务JOB
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class WorkorderAutoConvertStatusJob extends BaseQuartzJobBean {

    //操作工单
    private WorkorderService workorderService ;
    private Logger logger = LoggerFactory.getLogger(WorkorderAutoConvertStatusJob.class) ;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        logger.info(" ---------- 订单自动确认以及评价任务开始 ---------- ");
        ApplicationContext applicationContext = getApplicationContext(context) ;
        this.workorderService = applicationContext.getBean(WorkorderService.class) ;
        // TODO: 2017/3/20 待确认订单处理
        List<Workorder> pendingWorkorder = workorderService.getWorkordersByState(WorkOrderState.PENDING) ;
        logger.info("当前待确认的工单有： " + pendingWorkorder);
        for (Workorder workorder : pendingWorkorder){
            workorder.setWorkFalg("3");
            workorder.setWorkEcscFalg("4");
            workorder.setIsAutoconfirm("1");
            try {
                workorderService.addWorkOpinion(workorder, "");
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        // TODO: 2017/3/20 待评价订单处理
        List<Workorder> evaluatingWorkorder = workorderService.getWorkordersByState(WorkOrderState.EVALUATING) ;
        logger.info("当前待评价的工单有： " + evaluatingWorkorder);
        for (Workorder workorder : evaluatingWorkorder){
            workorder.setWorkFalg("3");
            workorder.setWorkEcscFalg("5");
            workorder.setWorkHighly("1");
            workorder.setIsAutoevaluation("1");
            try {
                workorderService.addWorkOpinion(workorder, "");
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        logger.info(" ---------- 订单自动确认以及评价任务完成 ---------- ");
    }
}
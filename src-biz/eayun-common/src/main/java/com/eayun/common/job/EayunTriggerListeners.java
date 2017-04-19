package com.eayun.common.job;

import java.util.Date;

import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.MongoCollectionName;



public class EayunTriggerListeners implements TriggerListener {
    
    private static final Logger log = LoggerFactory.getLogger(EayunTriggerListeners.class);
    private MongoTemplate mongoTemplate; 
    private Date startDate;
    private static final String TRIGGER_RESULT_COMPLETE_SUCCESS = "1";
    private static final String TRIGGER_RESULT_COMPLETE_ERROR = "0";
    private static final String TRIGGER_RESULT_MISFIRED = "2";
    
    @Override
    public String getName() {
        return "EayunTriggerListeners";
    }
    
    public ApplicationContext getApplicationContext(final JobExecutionContext jobexecutioncontext) {
        try {
            return (ApplicationContext) jobexecutioncontext.getScheduler().getContext()
                .get("applicationContextKey");
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        log.info("触发器："+trigger.getKey().getName()+" 触发……");
        startDate = new Date();
        ApplicationContext appContext = getApplicationContext(context);
        mongoTemplate = appContext.getBean(MongoTemplate.class);

    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        return false;
    }

    @Override
    public void triggerMisfired(Trigger trigger) {
        log.warn("触发器："+trigger.getKey().getName()+" 哑火！");
    	String triggerName = trigger.getKey().getName();
        String jobName = trigger.getJobKey().getName();
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("triggerName", triggerName);
        jsonObj.put("jobName", jobName);
        jsonObj.put("jobStartTime", startDate);
        jsonObj.put("takeTime", 0);
        jsonObj.put("errorMsg", null);
        // 执行类型，0：自动执行；1：手动执行
        jsonObj.put("excType", triggerName.startsWith("MT_") ? "1" : "0");
        jsonObj.put("triggerCode", TRIGGER_RESULT_MISFIRED);
		// 添加任务执行日志
        try {
            mongoTemplate.insert(jsonObj, MongoCollectionName.LOG_SCHEDULE);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context,
                                CompletedExecutionInstruction triggerInstructionCode) {
        log.info("触发器："+trigger.getKey().getName()+" 完成！triggerInstructionCode："+triggerInstructionCode);
        String triggerName = trigger.getKey().getName();
        String jobName = trigger.getJobKey().getName();
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("triggerName", triggerName);
        jsonObj.put("jobName", jobName);
        jsonObj.put("jobStartTime", startDate);
        jsonObj.put("takeTime", context.getJobRunTime());
        jsonObj.put("errorMsg", triggerInstructionCode == CompletedExecutionInstruction.NOOP ? "" : triggerInstructionCode);
        // 执行类型，0：自动执行；1：手动执行
        jsonObj.put("excType", context.getTrigger().getKey().getName().startsWith("MT_") ? "1" : "0");
        jsonObj.put("triggerCode", triggerInstructionCode == CompletedExecutionInstruction.NOOP ? TRIGGER_RESULT_COMPLETE_SUCCESS : TRIGGER_RESULT_COMPLETE_ERROR);
		// 添加任务执行日志
        try {
            mongoTemplate.insert(jsonObj, MongoCollectionName.LOG_SCHEDULE);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
		
    }
}

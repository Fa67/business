package com.eayun.common.job;

import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

public abstract class BaseQuartzJobBean extends QuartzJobBean {

    public ApplicationContext getApplicationContext(final JobExecutionContext jobexecutioncontext) {
        try {
            return (ApplicationContext) jobexecutioncontext.getScheduler().getContext()
                .get("applicationContextKey");
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }
}

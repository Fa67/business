package com.eayun.schedule.job;

import com.eayun.common.job.BaseQuartzJobBean;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by Administrator on 2016/11/24.
 */
public class TempJobBean extends BaseQuartzJobBean {
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        System.out.println(" --------- ");
    }
}

package com.eayun.dashboard.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.annotation.Autowired;

import com.eayun.common.job.BaseQuartzJobBean;
import com.eayun.dashboard.ecmcservice.EcmcOverviewService;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class OverviewIncomeChartJob  extends BaseQuartzJobBean{
	
	@Autowired
	private EcmcOverviewService ecmcOverviewService;

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		ecmcOverviewService.gatherOverviewIncomeChart();
	}
	

}

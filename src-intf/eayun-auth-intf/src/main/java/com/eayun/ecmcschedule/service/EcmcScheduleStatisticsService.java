package com.eayun.ecmcschedule.service;

import java.util.Date;
import java.util.Map;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.ecmcschedule.model.BaseEcmcScheduleStatistics;

public interface EcmcScheduleStatisticsService {
	
	public void add(BaseEcmcScheduleStatistics baseEcmcScheduleStatistics);
	
	public Page getByTriggerName(String taskId, String startTime, String endTime, QueryMap queryMap);
	
	public BaseEcmcScheduleStatistics getTriggerNameAndDate(String triggerName, Date statisticsDate);
	
	public Map<String,Object> getChartData(String taskId, String startTime, String endTime);

}

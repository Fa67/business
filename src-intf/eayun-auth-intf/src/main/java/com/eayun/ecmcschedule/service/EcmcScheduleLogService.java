package com.eayun.ecmcschedule.service;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;

public interface EcmcScheduleLogService {
	
	public Page getLogList(String triggerName, String jobName, String startTime, String endTime, String queryStr, String isSuccess, QueryMap queryMap);

}

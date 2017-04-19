package com.eayun.ecmcschedule.service;

import java.util.List;

import com.eayun.ecmcschedule.model.BaseEcmcScheduleInfo;
import com.eayun.ecmcschedule.model.EcmcScheduleInfo;

public interface EcmcScheduleInfoService {
	
	public void add(EcmcScheduleInfo scheduleInfo) throws Exception;
	
	public List<EcmcScheduleInfo> getTaskList(String queryStr, String state);
	
	public BaseEcmcScheduleInfo getByTriggerName(String triggerName);

	public List<String> getAllTaskId();

	public void update(EcmcScheduleInfo scheduleInfo, String oldBeanName) throws Exception ;
	
	public void deleteTask(String taskId, String beanName) throws Exception;

	public EcmcScheduleInfo getTask(String taskId, String beanName) throws Exception;
}
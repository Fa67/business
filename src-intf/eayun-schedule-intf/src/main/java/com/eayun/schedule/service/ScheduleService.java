package com.eayun.schedule.service;

import org.quartz.SchedulerException;

import com.eayun.ecmcschedule.model.EcmcScheduleInfo;

public interface ScheduleService {
	
	public void addTask(EcmcScheduleInfo scheduleInfo) throws Exception;
	
	public void pauseTask(String taskId) throws Exception;
	
	public void resumeTask(String taskId) throws Exception;
	
	public void deleteTask(String taskId, String beanName) throws SchedulerException;
	
	public void modifyTask(EcmcScheduleInfo scheduleInfo, String oldBeanName) throws Exception;
	
	public EcmcScheduleInfo getTask(String taskId, String beanName) throws Exception;
	
	public void triggerTask(String taskId, String beanName) throws Exception;
	
	public boolean checkBeanAndMethod(String beanName, String methodName);

}

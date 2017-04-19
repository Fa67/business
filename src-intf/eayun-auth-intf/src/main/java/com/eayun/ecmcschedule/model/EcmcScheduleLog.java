package com.eayun.ecmcschedule.model;

import java.util.Date;

public class EcmcScheduleLog {
	
	//任务名称
	private String taskName;
	
	private String id;
	
	private String beanName;
	
	private String methodName;
	
	private String description;
	
	private String triggerName;
	
	private String jobName;
	
	private Date jobStartTime;
	
	private String excType;
	
	private String triggerCode;
	
	private String errorMsg;
	
	private long takeTime;

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBeanName() {
		return beanName;
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTriggerName() {
		return triggerName;
	}

	public void setTriggerName(String triggerName) {
		this.triggerName = triggerName;
	}

	public String getExcType() {
		return excType;
	}

	public void setExcType(String excType) {
		this.excType = excType;
	}

	public String getTriggerCode() {
		return triggerCode;
	}

	public void setTriggerCode(String triggerCode) {
		this.triggerCode = triggerCode;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public long getTakeTime() {
		return takeTime;
	}

	public void setTakeTime(long takeTime) {
		this.takeTime = takeTime;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public Date getJobStartTime() {
		return jobStartTime;
	}

	public void setJobStartTime(Date jobStartTime) {
		this.jobStartTime = jobStartTime;
	}
	
	

}

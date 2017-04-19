package com.eayun.ecmcschedule.model;

import java.util.Date;

import org.quartz.JobDataMap;

public class EcmcScheduleInfo extends BaseEcmcScheduleInfo {

	/**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 8340953009969991082L;
    // 任务ID
	private String taskId;
	// Bean名称
	private String beanName;
	// 方法名
	private String methodName;
	// 任务状态
	private String taskState;
	// 时间表达式
	private String cronExpression;
	// 上次执行时间
	private Date preExcTime;
	// 下次执行时间
	private Date nextExcTime;
	// 任务描述
	private String taskDesc;
	//创建人名称
	private String createUserName;
	//参数集合
	private JobDataMap dataMap;
	
	//触发器名称
	private String triggerName;
	//任务名称
	private String jobName;
	
	
	public JobDataMap getDataMap() {
		return dataMap;
	}
	public void setDataMap(JobDataMap dataMap) {
		this.dataMap = dataMap;
	}
	public String getCreateUserName() {
		return createUserName;
	}
	public void setCreateUserName(String createUserName) {
		this.createUserName = createUserName;
	}
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
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
	public String getTaskState() {
		return taskState;
	}
	public void setTaskState(String taskState) {
		this.taskState = taskState;
	}
	public String getCronExpression() {
		return cronExpression;
	}
	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}
	public Date getPreExcTime() {
		return preExcTime;
	}
	public void setPreExcTime(Date preExcTime) {
		this.preExcTime = preExcTime;
	}
	public Date getNextExcTime() {
		return nextExcTime;
	}
	public void setNextExcTime(Date nextExcTime) {
		this.nextExcTime = nextExcTime;
	}
	public String getTaskDesc() {
		return taskDesc;
	}
	public void setTaskDesc(String taskDesc) {
		this.taskDesc = taskDesc;
	}
	public String getTriggerName() {
		return triggerName;
	}
	public void setTriggerName(String triggerName) {
		this.triggerName = triggerName;
	}
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	
	

}

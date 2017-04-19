package com.eayun.schedule.model;

import java.util.Date;

public class ScheduleJob {
	
	//任务ID
	private String taskId;
	//Bean名称
	private String beanName;
	//方法名
	private String methodName;
	//任务状态
	private String taskState;
	//时间表达式
	private String cronExpression;
	//上次执行时间
	private Date preExcTime;
	//下次执行时间
	private Date nextExcTime;
	//创建时间
	private Date createTime;
	//任务描述
	private String taskDesc;
	
	
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
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public String getTaskDesc() {
		return taskDesc;
	}
	public void setTaskDesc(String taskDesc) {
		this.taskDesc = taskDesc;
	}
	
	

}

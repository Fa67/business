package com.eayun.schedule.model;

public class Schedule {
	
	private String jobName;
	private String triggerName;
	private String triggerExpression;
	private String jobGroup;
	private String triggerGroup;
	private String triggerDescription;
	
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	public String getTriggerName() {
		return triggerName;
	}
	public void setTriggerName(String triggerName) {
		this.triggerName = triggerName;
	}
	public String getTriggerExpression() {
		return triggerExpression;
	}
	public void setTriggerExpression(String triggerExpression) {
		this.triggerExpression = triggerExpression;
	}
	public String getJobGroup() {
		return jobGroup;
	}
	public void setJobGroup(String jobGroup) {
		this.jobGroup = jobGroup;
	}
	public String getTriggerGroup() {
		return triggerGroup;
	}
	public void setTriggerGroup(String triggerGroup) {
		this.triggerGroup = triggerGroup;
	}
	public String getTriggerDescription() {
		return triggerDescription;
	}
	public void setTriggerDescription(String triggerDescription) {
		this.triggerDescription = triggerDescription;
	}
	

}

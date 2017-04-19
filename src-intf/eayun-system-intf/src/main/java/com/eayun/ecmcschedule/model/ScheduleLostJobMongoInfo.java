package com.eayun.ecmcschedule.model;

import java.util.Date;

/**
 * 存储在Mongodb数据库中的漏跑任务信息的实体类
 */
public class ScheduleLostJobMongoInfo {
	
	//计划任务名称
	private String  jobName ;
	//最新一次检测时间
	private Date    jobDate ;
	//计划任务时间表达式
	private String  cron ;
	//计划任务间隔时间
	private Long    cycleTime ;
	//计划任务第一次漏跑时间点
	private String  firstTime ;
	//计划任务最新一次漏跑时间点
	private String  endTime ;
	//漏跑总次数
	private Integer number ;
	
	public ScheduleLostJobMongoInfo(){
		
	}
	
	public ScheduleLostJobMongoInfo(String jobName, Date jobDate, String cron, Long cycleTime, String firstTime, String endTime, Integer number) {
		super();
		this.jobName = jobName;
		this.jobDate = jobDate;
		this.cron = cron ;
		this.cycleTime = cycleTime ;
		this.firstTime = firstTime ;
		this.endTime = endTime ;
		this.number = number ;
	}
	
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	public Date getJobDate() {
		return jobDate;
	}
	public void setJobDate(Date jobDate) {
		this.jobDate = jobDate;
	}
	public String getCron() {
		return cron;
	}
	public void setCron(String cron) {
		this.cron = cron;
	}
	public Long getCycleTime() {
		return cycleTime;
	}
	public void setCycleTime(Long cycleTime) {
		this.cycleTime = cycleTime;
	}
	public String getFirstTime() {
		return firstTime;
	}
	public void setFirstTime(String firstTime) {
		this.firstTime = firstTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public Integer getNumber() {
		return number;
	}
	public void setNumber(Integer number) {
		this.number = number;
	}
	
}
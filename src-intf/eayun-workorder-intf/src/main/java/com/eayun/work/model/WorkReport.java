package com.eayun.work.model;

import java.util.Date;
/**
 * 运维之星帮助类
 * @author 陈鹏飞
 *
 */
public class WorkReport {
	private String workLevel;//工单级别
	private String workLevelName;//工单级别名称
	private String workType;//工单类别名称
	private String workTypeName;//工单类别名称
	private String workHeadUser;//责任人名称
	private String workHeadUserName;//责任人名称
	
	private int num=0;//工单条数
	private int doneNum=0;//工单按时完成条数
	private int notDoneNum=0;//工单未按时完成条数
	
	private Date beginTime;//开始时间
	private Date respondTime;//响应时间
	private Date endtime;//结束时间
	public String getWorkType() {
		return workType;
	}
	public void setWorkType(String workType) {
		this.workType = workType;
	}
	public String getWorkTypeName() {
		return workTypeName;
	}
	public void setWorkTypeName(String workTypeName) {
		this.workTypeName = workTypeName;
	}
	public String getWorkHeadUser() {
		return workHeadUser;
	}
	public void setWorkHeadUser(String workHeadUser) {
		this.workHeadUser = workHeadUser;
	}
	public String getWorkHeadUserName() {
		return workHeadUserName;
	}
	public void setWorkHeadUserName(String workHeadUserName) {
		this.workHeadUserName = workHeadUserName;
	}
	public int getNum() {
		return num;
	}
	public void setNum(int num) {
		this.num = num;
	}
	public int getDoneNum() {
		return doneNum;
	}
	public void setDoneNum(int doneNum) {
		this.doneNum = doneNum;
	}
	public int getNotDoneNum() {
		return notDoneNum;
	}
	public void setNotDoneNum(int notDoneNum) {
		this.notDoneNum = notDoneNum;
	}
	public Date getBeginTime() {
		return beginTime;
	}
	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}
	public Date getRespondTime() {
		return respondTime;
	}
	public void setRespondTime(Date respondTime) {
		this.respondTime = respondTime;
	}
	public Date getEndtime() {
		return endtime;
	}
	public void setEndtime(Date endtime) {
		this.endtime = endtime;
	}
	public String getWorkLevel() {
		return workLevel;
	}
	public void setWorkLevel(String workLevel) {
		this.workLevel = workLevel;
	}
	public String getWorkLevelName() {
		return workLevelName;
	}
	public void setWorkLevelName(String workLevelName) {
		this.workLevelName = workLevelName;
	}
	
	
	
}

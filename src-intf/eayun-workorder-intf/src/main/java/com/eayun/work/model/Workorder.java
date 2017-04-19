package com.eayun.work.model;

import java.util.Date;

/**
 * 
 * @author 陈鹏飞
 *
 */
public class Workorder extends BaseWorkorder {

	private static final long serialVersionUID = -5895454228082927262L;
	
	private String prjId;//工单类别名称
	private String workTypeName;//工单类别名称
	private String workLevelName;//工单级别名称
	private String workCreRoleName;//创建角色名称
	private String workPhoneTimeTum;//短信提醒时间段
	private String workFalgName;//状态名称
	private String workEcscFalgName;//状态名称
	private String flowRespondFalg;//是否响应
	private String opinionId;//回复id
	
	private String workCusName;//申请客户
	private Date flowBeginTime;//开始时间
	private Date flowRespondTime;//响应时间
	private Date endtime;//结束时间
	private Date updateTime;//修改时间
	private Date newDate;
	/**
	 * 用于区分反馈，确认和评价工单。
	 * '2'--反馈工单
	 * '3'--确认工单
	 * '4'--评价工单
	 **/
	private String logType;
	
	public Date getNewDate() {
		return newDate;
	}
	public void setNewDate(Date newDate) {
		this.newDate = newDate;
	}
	private int num=0;//工单条数
	
	public String getWorkTypeName() {
		return workTypeName;
	}
	public void setWorkTypeName(String workTypeName) {
		this.workTypeName = workTypeName;
	}
	public String getWorkLevelName() {
		return workLevelName;
	}
	public void setWorkLevelName(String workLevelName) {
		this.workLevelName = workLevelName;
	}
	public String getWorkCreRoleName() {
		return workCreRoleName;
	}
	public void setWorkCreRoleName(String workCreRoleName) {
		this.workCreRoleName = workCreRoleName;
	}
	public String getWorkPhoneTimeTum() {
		return workPhoneTimeTum;
	}
	public void setWorkPhoneTimeTum(String workPhoneTimeTum) {
		this.workPhoneTimeTum = workPhoneTimeTum;
	}
	public String getWorkFalgName() {
		return workFalgName;
	}
	public void setWorkFalgName(String workFalgName) {
		this.workFalgName = workFalgName;
	}
    public String getFlowRespondFalg() {
        return flowRespondFalg;
    }
    public void setFlowRespondFalg(String flowRespondFalg) {
        this.flowRespondFalg = flowRespondFalg;
    }
    public String getWorkEcscFalgName() {
        return workEcscFalgName;
    }
    public void setWorkEcscFalgName(String workEcscFalgName) {
        this.workEcscFalgName = workEcscFalgName;
    }
	public String getOpinionId() {
		return opinionId;
	}
	public void setOpinionId(String opinionId) {
		this.opinionId = opinionId;
	}
	public String getPrjId() {
		return prjId;
	}
	public void setPrjId(String prjId) {
		this.prjId = prjId;
	}
	public int getNum() {
		return num;
	}
	public void setNum(int num) {
		this.num = num;
	}
	public Date getFlowBeginTime() {
		return flowBeginTime;
	}
	public void setFlowBeginTime(Date flowBeginTime) {
		this.flowBeginTime = flowBeginTime;
	}
	public Date getFlowRespondTime() {
		return flowRespondTime;
	}
	public void setFlowRespondTime(Date flowRespondTime) {
		this.flowRespondTime = flowRespondTime;
	}
	public Date getEndtime() {
		return endtime;
	}
	public void setEndtime(Date endtime) {
		this.endtime = endtime;
	}
	public String getWorkCusName() {
		return workCusName;
	}
	public void setWorkCusName(String workCusName) {
		this.workCusName = workCusName;
	}
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	public String getLogType() {
		return logType;
	}
	public void setLogType(String logType) {
		this.logType = logType;
	}
	
}

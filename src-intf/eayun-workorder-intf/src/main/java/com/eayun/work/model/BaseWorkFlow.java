package com.eayun.work.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
/**
 * 
 * @author 陈鹏飞
 *
 */
@Entity
@Table(name = "workorder_assess")
public class BaseWorkFlow implements Serializable{

	private static final long serialVersionUID = -6927339515733799873L;
	@Id
	@Column(name = "id", unique = true, nullable = false, length = 32)
	private String flowId;	//ID
	@Column(name = "work_id", length = 32)
	private String workId;	//工单id
	@Column(name = "userid", length = 32)
	private String userIdHead;	//工单负责人
	@Column(name = "state", length = 32)
	private String flowFlag="0";	//工单--流程状态
	@Column(name = "isrespstate", length = 1)
	private String flowRespondFalg="0";	//响应状态0:未响应1:以响应
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "begintime")
	private Date flowBeginTime;	//开始时间
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "responsetime")
	private Date flowRespondTime;	//响应时间
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "endtime")
	private Date endtime;	//结束时间
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "flow_cre_time")
	private Date flowCreTime;	//新增字段--流程创建时间
    @Column(name = "flow_state", length = 1)
    private String flowState="0";    //新增字段---操作 0:未转办1:已转办 2:打回 
	
	public String getFlowId() {
		return flowId;
	}
	public void setFlowId(String flowId) {
		this.flowId = flowId;
	}
	public String getWorkId() {
		return workId;
	}
	public void setWorkId(String workId) {
		this.workId = workId;
	}
	public String getUserIdHead() {
		return userIdHead;
	}
	public void setUserIdHead(String userIdHead) {
		this.userIdHead = userIdHead;
	}
	public String getFlowFlag() {
		return flowFlag;
	}
	public void setFlowFlag(String flowFlag) {
		this.flowFlag = flowFlag;
	}
	public String getFlowState() {
		return flowState;
	}
	public void setFlowState(String flowState) {
		this.flowState = flowState;
	}
	public String getFlowRespondFalg() {
		return flowRespondFalg;
	}
	public void setFlowRespondFalg(String flowRespondFalg) {
		this.flowRespondFalg = flowRespondFalg;
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
	public Date getFlowCreTime() {
		return flowCreTime;
	}
	public void setFlowCreTime(Date flowCreTime) {
		this.flowCreTime = flowCreTime;
	}
    public Date getEndtime() {
        return endtime;
    }
    public void setEndtime(Date endtime) {
        this.endtime = endtime;
    }
	
}

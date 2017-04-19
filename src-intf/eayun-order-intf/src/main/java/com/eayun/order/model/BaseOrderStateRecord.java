package com.eayun.order.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;

/**
 *                       
 * @Filename: BaseOrderStateRecord.java
 * @Description: 订单状态变更映射表
 * @Version: 1.0
 * @Author: bo.zeng
 * @Email: bo.zeng@eayun.com
 * @History:<br>
 *<li>Date: 2016年7月27日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Entity
@Table(name = "order_state_record")
public class BaseOrderStateRecord implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 *Comment for <code>recordId</code>
	 *主键UUID
	 */
	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name = "record_id", unique = true, nullable = false, length = 32)
	private String recordId;
	
	/**
	 *Comment for <code>orderNo</code>
	 *订单编号
	 */
	@Column(name = "order_no", length = 18, updatable=false, nullable=false)
	private String orderNo;
	
	/**
	 *Comment for <code>originState</code>
	 *原始状态（1-待支付；2-资源创建中；3-资源创建失败-已取消；4-已完成；5-已取消）
	 */
	@Column(name = "origin_state", length = 1, updatable=false)
	private String originState;
	
	/**
	 *Comment for <code>toState</code>
	 *变更状态（1-待支付；2-资源创建中；3-资源创建失败-已取消；4-已完成；5-已取消）
	 */
	@Column(name = "to_state", length = 1, updatable=false, nullable=false)
	private String toState;
	
	/**
	 *Comment for <code>changeTime</code>
	 *变更时间
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "change_time", updatable=false, nullable=false)
	private Date changeTime;
	
	public BaseOrderStateRecord(){
		
	}
	
	public BaseOrderStateRecord(String orderNo, String originState, String toState){
		this.orderNo = orderNo;
		this.originState = originState;
		this.toState = toState;
		this.changeTime = new Date();
	}

	public String getRecordId() {
		return recordId;
	}

	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getOriginState() {
		return originState;
	}

	public void setOriginState(String originState) {
		this.originState = originState;
	}

	public String getToState() {
		return toState;
	}

	public void setToState(String toState) {
		this.toState = toState;
	}

	public Date getChangeTime() {
		return changeTime;
	}

	public void setChangeTime(Date changeTime) {
		this.changeTime = changeTime;
	}
	
}

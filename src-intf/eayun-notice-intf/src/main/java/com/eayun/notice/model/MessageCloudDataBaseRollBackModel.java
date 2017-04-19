package com.eayun.notice.model;

import java.util.Date;

public class MessageCloudDataBaseRollBackModel {
	
	private String orderNo;//订单编号
	private String orderName;//订单名称
	private Date orderCancelTime;//订单取消时间
	private String cloudDataBaseName;//云数据库名称
	private String CloudDataId;//云数据库ID
	private String resourceTypeName; // 资源类型
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	public String getOrderName() {
		return orderName;
	}
	public void setOrderName(String orderName) {
		this.orderName = orderName;
	}
	public Date getOrderCancelTime() {
		return orderCancelTime;
	}
	public void setOrderCancelTime(Date orderCancelTime) {
		this.orderCancelTime = orderCancelTime;
	}
	public String getCloudDataBaseName() {
		return cloudDataBaseName;
	}
	public void setCloudDataBaseName(String cloudDataBaseName) {
		this.cloudDataBaseName = cloudDataBaseName;
	}
	public String getCloudDataId() {
		return CloudDataId;
	}
	public void setCloudDataId(String cloudDataId) {
		CloudDataId = cloudDataId;
	}
	public String getResourceTypeName() {
		return resourceTypeName;
	}
	public void setResourceTypeName(String resourceTypeName) {
		this.resourceTypeName = resourceTypeName;
	}

}

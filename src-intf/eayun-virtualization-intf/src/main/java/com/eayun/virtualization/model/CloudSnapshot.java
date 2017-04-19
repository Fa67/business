package com.eayun.virtualization.model;

public class CloudSnapshot extends BaseCloudSnapshot {
	private static final long serialVersionUID = 1L;
	private String volName;
	private String prjName;
	private String dcName;
	private String createTimeForDis;//前端显示的时间格式
	private String isDeleting;//是否正在删除
	private int count ;
	private String statusForDis;
	private int volNum;//基于当前备份创建的云硬盘数量
	private String cusId;//客户Id
	private String cusOrg;//客户组织
	
	private String orderNo;//订单编号
	
	
	
	
	
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	public int getVolNum() {
		return volNum;
	}
	public void setVolNum(int volNum) {
		this.volNum = volNum;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public String getVolName() {
		return volName;
	}
	public void setVolName(String volName) {
		this.volName = volName;
	}
	public String getPrjName() {
		return prjName;
	}
	public void setPrjName(String prjName) {
		this.prjName = prjName;
	}
	public String getCreateTimeForDis() {
		return createTimeForDis;
	}
	public void setCreateTimeForDis(String createTimeForDis) {
		this.createTimeForDis = createTimeForDis;
	}
	public String getIsDeleting() {
		return isDeleting;
	}
	public void setIsDeleting(String isDeleting) {
		this.isDeleting = isDeleting;
	}
	public String getStatusForDis() {
		return statusForDis;
	}
	public void setStatusForDis(String statusForDis) {
		this.statusForDis = statusForDis;
	}
	public String getDcName() {
		return dcName;
	}
	public void setDcName(String dcName) {
		this.dcName = dcName;
	}
	public String getCusId() {
		return cusId;
	}
	public void setCusId(String cusId) {
		this.cusId = cusId;
	}
	public String getCusOrg() {
		return cusOrg;
	}
	public void setCusOrg(String cusOrg) {
		this.cusOrg = cusOrg;
	}

	

	
}

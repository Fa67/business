package com.eayun.virtualization.model;



public class CloudFireWall extends BaseCloudFireWall {
	private static final long serialVersionUID = 1L;
	private String prjName;
	private String fwpName;
	private String policy;
	private String fwpId;
	private String isDeleting;//是否正在删除
	private String dcName;
	private int count ;
	private String tags;//标签名称（可有多个标签）
	private String statusForDis;//用于页面显示的中文状态
	private String cusName;//客户名称
	private String cusOrg;
	
	
	public String getCusOrg() {
		return cusOrg;
	}
	public void setCusOrg(String cusOrg) {
		this.cusOrg = cusOrg;
	}
	public String getCusName() {
		return cusName;
	}
	public void setCusName(String cusName) {
		this.cusName = cusName;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public String getPrjName() {
		return prjName;
	}
	public void setPrjName(String prjName) {
		this.prjName = prjName;
	}
	public String getFwpName() {
		return fwpName;
	}
	public void setFwpName(String fwpName) {
		this.fwpName = fwpName;
	}
	public String getPolicy() {
		return policy;
	}
	public void setPolicy(String policy) {
		this.policy = policy;
	}
	public String getFwpId() {
		return fwpId;
	}
	public void setFwpId(String fwpId) {
		this.fwpId = fwpId;
	}
	public String getIsDeleting() {
		return isDeleting;
	}
	public void setIsDeleting(String isDeleting) {
		this.isDeleting = isDeleting;
	}
	public String getDcName() {
		return dcName;
	}
	public void setDcName(String dcName) {
		this.dcName = dcName;
	}
	public String getTags() {
		return tags;
	}
	public void setTags(String tags) {
		this.tags = tags;
	}
	public String getStatusForDis() {
		return statusForDis;
	}
	public void setStatusForDis(String statusForDis) {
		this.statusForDis = statusForDis;
	}
	
	
	
	

}

package com.eayun.virtualization.model;

public class CloudFwRule extends BaseCloudFwRule {
	
	private static final long serialVersionUID = 1L;
	private String prjName;
	private String fwpName;
	private String isActive;
	private String isDeleting;//是否正在删除
	private String dcName;
	private String cusName;//客户姓名
	private String cusOrg;
	
	public String getCusOrg() {
		return cusOrg;
	}
	public void setCusOrg(String cusOrg) {
		this.cusOrg = cusOrg;
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
	public String getIsActive() {
		return isActive;
	}
	public void setIsActive(String isActive) {
		this.isActive = isActive;
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
	public String getCusName() {
		return cusName;
	}
	public void setCusName(String cusName) {
		this.cusName = cusName;
	}
	
	
}

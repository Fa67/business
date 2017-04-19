package com.eayun.virtualization.model;

public class CloudSecurityGroup extends BaseCloudSecurityGroup {
	/**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 7535230642112308659L;
    private String prjName;
	private String dcName;
	private String tagName;
	private String vmCount;
	
	private String cusId;
	private String cusName;
	private String cusOrg;
	
	
	public String getCusOrg() {
		return cusOrg;
	}
	public void setCusOrg(String cusOrg) {
		this.cusOrg = cusOrg;
	}
	public String getVmCount() {
		return vmCount;
	}
	public void setVmCount(String vmCount) {
		this.vmCount = vmCount;
	}
	public String getTagName() {
		return tagName;
	}
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}
	private String isDeleting;//是否正在删除
	
	public String getPrjName() {
		return prjName;
	}
	public void setPrjName(String prjName) {
		this.prjName = prjName;
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
	public String getCusId() {
		return cusId;
	}
	public void setCusId(String cusId) {
		this.cusId = cusId;
	}
	public String getCusName() {
		return cusName;
	}
	public void setCusName(String cusName) {
		this.cusName = cusName;
	}
	
	
	
	
}

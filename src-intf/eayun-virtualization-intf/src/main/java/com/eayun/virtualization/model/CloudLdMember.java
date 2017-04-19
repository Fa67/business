package com.eayun.virtualization.model;

public class CloudLdMember extends BaseCloudLdMember {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4891881306528831479L;
	private String isDeleting = "not";
	private String poolName;
	private String projectName;
	private String adminStateupStr;
	private int count;
	private String statusForMember;
	private String tagName;
	private String vmName;
	private String subnetId;
	private String cusId;//	客户ID
	private String cusOrg;//客户所属组织
	private String dcName;//数据中心名称
	
	public String getTagName() {
		return tagName;
	}
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}
	
	public String getStatusForMember() {
		return statusForMember;
	}

	public void setStatusForMember(String statusForMember) {
		this.statusForMember = statusForMember;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getAdminStateupStr() {
		return adminStateupStr;
	}

	public void setAdminStateupStr(String adminStateupStr) {
		this.adminStateupStr = adminStateupStr;
	}

	public String getPoolName() {
		return poolName;
	}

	public void setPoolName(String poolName) {
		this.poolName = poolName;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getIsDeleting() {
		return isDeleting;
	}

	public void setIsDeleting(String isDeleting) {
		this.isDeleting = isDeleting;
	}
	public String getVmName() {
		return vmName;
	}
	public void setVmName(String vmName) {
		this.vmName = vmName;
	}
	public String getSubnetId() {
		return subnetId;
	}
	public void setSubnetId(String subnetId) {
		this.subnetId = subnetId;
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
	public String getDcName() {
		return dcName;
	}
	public void setDcName(String dcName) {
		this.dcName = dcName;
	}
	
}

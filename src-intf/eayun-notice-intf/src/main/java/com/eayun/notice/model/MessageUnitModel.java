package com.eayun.notice.model;

public class MessageUnitModel {
	
	private String  unitName;//主体单位
	
	private String domainName;//网站域名
	
	private String recordType;//备案类型
	
	private String status;//备案进展
	
	private String time;//创建时间
	
	private String orgName;//客户名称
	
	private String unitFuzeName;//主体负责人
	
	private String webNo;//网站备案号
	

	public String getWebNo() {
		return webNo;
	}

	public void setWebNo(String webNo) {
		this.webNo = webNo;
	}

	public String getUnitFuzeName() {
		return unitFuzeName;
	}

	public void setUnitFuzeName(String unitFuzeName) {
		this.unitFuzeName = unitFuzeName;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public String getRecordType() {
		return recordType;
	}

	public void setRecordType(String recordType) {
		this.recordType = recordType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}
	
	

}

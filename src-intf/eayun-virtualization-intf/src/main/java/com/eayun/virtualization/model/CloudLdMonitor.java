package com.eayun.virtualization.model;

public class CloudLdMonitor extends BaseCloudLdMonitor {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8202841325295882325L;
	private String prjName;
	private String isDeleting;
	private String text;
	private String dcName;//数据中心名称
	private String poolNum;//监控被资源池绑定的数量
	private String tagName;
	private boolean isCheck;
	private String cusId;//	客户ID
	private String cusName;//客户名称
	private String cusOrg;//客户所属组织
	private String checkRadio;
	
	public String getCheckRadio() {
		return checkRadio;
	}
	public void setCheckRadio(String checkRadio) {
		this.checkRadio = checkRadio;
	}
	public String getTagName() {
		return tagName;
	}
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}
	
	public String getIsDeleting() {
		return isDeleting;
	}
	public void setIsDeleting(String isDeleting) {
		this.isDeleting = isDeleting;
	}
	public String getPrjName() {
		return prjName;
	}
	public void setPrjName(String prjName) {
		this.prjName = prjName;
	}

	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
	public String getDcName() {
		return dcName;
	}
	public void setDcName(String dcName) {
		this.dcName = dcName;
	}
	public String getPoolNum() {
		return poolNum;
	}
	public void setPoolNum(String poolNum) {
		this.poolNum = poolNum;
	}
	public boolean isCheck() {
		return isCheck;
	}
	public void setCheck(boolean isCheck) {
		this.isCheck = isCheck;
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
	public String getCusOrg() {
		return cusOrg;
	}
	public void setCusOrg(String cusOrg) {
		this.cusOrg = cusOrg;
	}
	
}

package com.eayun.virtualization.model;

public class CloudLdVip extends BaseCloudLdVip {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6912967830654835702L;
	private String subnetName;
	private String prjName;
	private String gatewayIp;
	private String poolName;
	private int count ;
	private String statusForVip;
	private String tagName;
	private String dcName;//数据中心名称
	private String cusId;//客户ID
	private String cusName;//客户名称
	private String cusOrg;//客户所属组织名称
	
	public String getTagName() {
		return tagName;
	}
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}
	
	public String getStatusForVip() {
		return statusForVip;
	}
	public void setStatusForVip(String statusForVip) {
		this.statusForVip = statusForVip;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public String getSubnetName() {
		return subnetName;
	}
	public void setSubnetName(String subnetName) {
		this.subnetName = subnetName;
	}
	public String getPrjName() {
		return prjName;
	}
	public void setPrjName(String prjName) {
		this.prjName = prjName;
	}
	public String getGatewayIp() {
		return gatewayIp;
	}
	public void setGatewayIp(String gatewayIp) {
		this.gatewayIp = gatewayIp;
	}
	public String getPoolName() {
		return poolName;
	}
	public void setPoolName(String poolName) {
		this.poolName = poolName;
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
	public String getCusOrg() {
		return cusOrg;
	}
	public void setCusOrg(String cusOrg) {
		this.cusOrg = cusOrg;
	}
	
}

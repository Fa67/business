package com.eayun.virtualization.model;

import java.util.List;


public class CloudFwPolicy extends BaseCloudFwPolicy {
	
	private static final long serialVersionUID = 1L;
	private String prjName;
	private List<CloudFwRule> firewallRules;
	private String fwrName;
	private String rulenum;
	private String isDeleting;//是否正在删除
	private String dcName;
	private String cusName;
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
	public String getFwrName() {
		return fwrName;
	}
	public void setFwrName(String fwrName) {
		this.fwrName = fwrName;
	}
	public String getRulenum() {
		return rulenum;
	}
	public void setRulenum(String rulenum) {
		this.rulenum = rulenum;
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
	public List<CloudFwRule> getFirewallRules() {
		return firewallRules;
	}
	public void setFirewallRules(List<CloudFwRule> firewallRules) {
		this.firewallRules = firewallRules;
	}
	public String getCusName() {
		return cusName;
	}
	public void setCusName(String cusName) {
		this.cusName = cusName;
	}
	
	
	
}

package com.eayun.virtualization.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.eayun.eayunstack.model.FirewallRule;

@Entity
@Table(name = "cloud_fwrule")
public class BaseCloudFwRule implements java.io.Serializable {
	private static final long serialVersionUID = -11167651198987706L;
	private String fwrId;
	private String fwrName;
	private String createName;
	private Date createTime;
	private String prjId;
	private String dcId;
	private String description;
	private String isShared;
	private String fwrStatus;
	private String protocol;
	private String sourcePort;
	private String sourceIpaddress;
	private String destinationPort;
	private String destinationIpaddress;
	private String ipVersion;
	private String fwrAction;
	private String fwrEnabled;
	private String fwpId;
	private String priority;
	
	@Id
	@Column(name = "fwr_id", unique = true, nullable = false, length = 100)
	public String getFwrId() {
		return fwrId;
	}
	public void setFwrId(String fwrId) {
		this.fwrId = fwrId;
	}
	
	@Column(name = "fwr_name", length = 100)
	public String getFwrName() {
		return fwrName;
	}
	public void setFwrName(String fwrName) {
		this.fwrName = fwrName;
	}
	
	@Column(name = "create_name", length = 50)
	public String getCreateName() {
		return createName;
	}
	public void setCreateName(String createName) {
		this.createName = createName;
	}
	
	@Column(name = "create_time", length = 0)
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	@Column(name = "prj_id", length = 100)
	public String getPrjId() {
		return prjId;
	}
	public void setPrjId(String prjId) {
		this.prjId = prjId;
	}
	
	@Column(name = "dc_id", length = 100)
	public String getDcId() {
		return dcId;
	}
	public void setDcId(String dcId) {
		this.dcId = dcId;
	}
	
	@Column(name = "description", length = 1000)
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Column(name = "is_shared", length = 1)
	public String getIsShared() {
		return isShared;
	}
	public void setIsShared(String isShared) {
		this.isShared = isShared;
	}
	
	@Column(name = "fwr_status", length = 20)
	public String getFwrStatus() {
		return fwrStatus;
	}
	public void setFwrStatus(String fwrStatus) {
		this.fwrStatus = fwrStatus;
	}
	
	@Column(name = "protocol", length = 100)
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	
	@Column(name = "source_port", length = 100)
	public String getSourcePort() {
		return sourcePort;
	}
	public void setSourcePort(String sourcePort) {
		this.sourcePort = sourcePort;
	}
	
	@Column(name = "source_ipaddress", length = 100)
	public String getSourceIpaddress() {
		return sourceIpaddress;
	}
	
	public void setSourceIpaddress(String sourceIpaddress) {
		this.sourceIpaddress = sourceIpaddress;
	}
	
	@Column(name = "destination_port", length = 100)
	public String getDestinationPort() {
		return destinationPort;
	}
	public void setDestinationPort(String destinationPort) {
		this.destinationPort = destinationPort;
	}
	
	@Column(name = "destination_ipaddress", length = 100)
	public String getDestinationIpaddress() {
		return destinationIpaddress;
	}
	public void setDestinationIpaddress(String destinationIpaddress) {
		this.destinationIpaddress = destinationIpaddress;
	}
	
	@Column(name = "ip_version", length = 1)
	public String getIpVersion() {
		return ipVersion;
	}
	public void setIpVersion(String ipVersion) {
		this.ipVersion = ipVersion;
	}
	
	@Column(name = "fwr_action", length = 20)
	public String getFwrAction() {
		return fwrAction;
	}
	public void setFwrAction(String fwrAction) {
		this.fwrAction = fwrAction;
	}
	
	@Column(name = "fwr_enabled", length = 1)
	public String getFwrEnabled() {
		return fwrEnabled;
	}
	public void setFwrEnabled(String fwrEnabled) {
		this.fwrEnabled = fwrEnabled;
	}
	
	@Column(name = "fwp_id", length = 100)
	public String getFwpId() {
		return fwpId;
	}
	public void setFwpId(String fwpId) {
		this.fwpId = fwpId;
	}
	
	@Column(name = "fwr_priority", length = 11)
	public String getPriority() {
		return priority;
	}
	public void setPriority(String priority) {
		this.priority = priority;
	}
	
	public BaseCloudFwRule(){}
	
	public BaseCloudFwRule(FirewallRule fwRule,String dcId){
		if(null!=fwRule){
			this.fwrId = fwRule.getId();
			this.fwrName = fwRule.getName();
			this.prjId = fwRule.getTenant_id();
			this.dcId = dcId;
			this.description = fwRule.getDescription();
			this.isShared = (fwRule.isShared())?"1":"0";
			this.protocol = fwRule.getProtocol();
			this.sourcePort = fwRule.getSource_port();
			this.sourceIpaddress = fwRule.getSource_ip_address();
			this.destinationPort = fwRule.getDestination_port();
			this.destinationIpaddress = fwRule.getDestination_ip_address();
			this.ipVersion = fwRule.getIp_version();
			this.fwrAction = fwRule.getAction();
			this.fwrEnabled = (fwRule.isEnabled())?"1":"0";
			this.fwpId = fwRule.getFirewall_policy_id();
		}
	}
}

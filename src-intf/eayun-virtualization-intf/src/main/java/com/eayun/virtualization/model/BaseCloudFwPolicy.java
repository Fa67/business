package com.eayun.virtualization.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.eayun.eayunstack.model.FirewallPolicy;
@Entity
@Table(name = "cloud_fwpolicy")
public class BaseCloudFwPolicy implements java.io.Serializable{
	
	
	private static final long serialVersionUID = -11167651198987706L;
	private String fwpId;
	private String fwpName;
	private String createName;
	private Date createTime;
	private String prjId;
	private String dcId;
	private String description;
	private String isShared;
	private String audited;
	private String fwpStatus;
	private String fwId;
	private String rules;
	
	@Id
	@Column(name = "fwp_id", unique = true, nullable = false, length = 100)
	public String getFwpId() {
		return fwpId;
	}
	public void setFwpId(String fwpId) {
		this.fwpId = fwpId;
	}
	
	@Column(name = "fwp_name", length = 100)
	public String getFwpName() {
		return fwpName;
	}
	public void setFwpName(String fwpName) {
		this.fwpName = fwpName;
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
	
	@Column(name = "audited", length = 1)
	public String getAudited() {
		return audited;
	}
	public void setAudited(String audited) {
		this.audited = audited;
	}
	
	@Column(name = "fwp_status", length = 20)
	public String getFwpStatus() {
		return fwpStatus;
	}
	public void setFwpStatus(String fwpStatus) {
		this.fwpStatus = fwpStatus;
	}
	
	@Column(name = "fw_id", length = 100)
	public String getFwId() {
		return fwId;
	}
	public void setFwId(String fwId) {
		this.fwId = fwId;
	}
	
	@Column(name = "rules", length = 1000)
	public String getRules() {
		return rules;
	}
	public void setRules(String rules) {
		this.rules = rules;
	}
	
	public BaseCloudFwPolicy(FirewallPolicy fwPolicy,String dcId){
		if(null!=fwPolicy){
			this.fwpId = fwPolicy.getId();
			this.fwpName = fwPolicy.getName();
			this.prjId = fwPolicy.getTenant_id();
			this.dcId = dcId;
			this.description = fwPolicy.getDescription();
			this.isShared = (fwPolicy.isShared())?"1":"0";
			this.audited = (fwPolicy.isAudited())?"1":"0";
			
		}
	}
	
	public BaseCloudFwPolicy(){}
	
}

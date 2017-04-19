package com.eayun.virtualization.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.eayun.eayunstack.model.Firewall;


@Entity
@Table(name = "cloud_firewall")
public class BaseCloudFireWall implements java.io.Serializable{

	private static final long serialVersionUID = -11167651198987706L;
	private String fwId;
	private String fwName;
	private String createName;
	private Date createTime;
	private String prjId;
	private String dcId;
	private String description;
	private String isShared;
	private String adminStateup;
	private String fwpId;
	private String fwStatus;
	
	@Id
	@Column(name = "fw_id", unique = true, nullable = false, length = 100)
	public String getFwId() {
		return fwId;
	}
	public void setFwId(String fwId) {
		this.fwId = fwId;
	}
	
	@Column(name = "fw_name", length = 100)
	public String getFwName() {
		return fwName;
	}
	public void setFwName(String fwName) {
		this.fwName = fwName;
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
	
	@Column(name = "admin_stateup", length = 1)
	public String getAdminStateup() {
		return adminStateup;
	}
	public void setAdminStateup(String adminStateup) {
		this.adminStateup = adminStateup;
	}
	
	@Column(name = "fwp_id", length = 100)
	public String getFwpId() {
		return fwpId;
	}
	public void setFwpId(String fwpId) {
		this.fwpId = fwpId;
	}
	
	@Column(name = "fw_status", length = 20)
	public String getFwStatus() {
		return fwStatus;
	}
	public void setFwStatus(String fwStatus) {
		this.fwStatus = fwStatus;
	}
	
	public BaseCloudFireWall(){}
	
	public BaseCloudFireWall(Firewall fw,String dcId){
		if(null!=fw){
			this.fwId = fw.getId();
			this.fwName = fw.getName();
			this.prjId = fw.getTenant_id();
			this.dcId = dcId;
			this.description = fw.getDescription();
			this.adminStateup = (fw.isAdmin_state_up())?"1":"0";
			this.fwpId = fw.getFirewall_policy_id();
			this.fwStatus = fw.getStatus()!=null?fw.getStatus().toUpperCase():"";
			this.isShared=fw.isShared()?"1":"0";
		}
	}
	
}

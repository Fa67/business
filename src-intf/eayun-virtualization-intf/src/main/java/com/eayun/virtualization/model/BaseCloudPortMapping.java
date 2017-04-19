package com.eayun.virtualization.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.eayun.eayunstack.model.PortMapping;

@Entity
@Table(name = "cloud_portmapping")
public class BaseCloudPortMapping implements java.io.Serializable {

	/**
     * 
     */
    private static final long serialVersionUID = -6173315687547915154L;
    private String pmId;
	private String dcId;
	private String prjId;
	private String protocol;
	private String resourceId;
	private String resourceIp;
	private String resourcePort;
	private String destinyId;
	private String destinyIp;
	private String destinyPort;
	private String createName;
	private Date createTime;
	
	@Id
	@Column(name = "pm_id", unique = true, nullable = false, length = 100)
	public String getPmId() {
		return pmId;
	}
	public void setPmId(String pmId) {
		this.pmId = pmId;
	}
	@Column(name = "dc_id", length = 100)
	public String getDcId() {
		return dcId;
	}
	public void setDcId(String dcId) {
		this.dcId = dcId;
	}
	@Column(name = "prj_id", length = 100)
	public String getPrjId() {
		return prjId;
	}
	public void setPrjId(String prjId) {
		this.prjId = prjId;
	}
	@Column(name = "protocol", length = 10)
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	@Column(name = "resource_id", length = 100)
	public String getResourceId() {
		return resourceId;
	}
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	@Column(name = "resource_ip", length = 50)
	public String getResourceIp() {
		return resourceIp;
	}
	public void setResourceIp(String resourceIp) {
		this.resourceIp = resourceIp;
	}
	@Column(name = "resource_port", length = 10)
	public String getResourcePort() {
		return resourcePort;
	}
	public void setResourcePort(String resourcePort) {
		this.resourcePort = resourcePort;
	}
	@Column(name = "destiny_id", length = 100)
	public String getDestinyId() {
		return destinyId;
	}
	public void setDestinyId(String destinyId) {
		this.destinyId = destinyId;
	}
	@Column(name = "destiny_ip", length = 50)
	public String getDestinyIp() {
		return destinyIp;
	}
	public void setDestinyIp(String destinyIp) {
		this.destinyIp = destinyIp;
	}
	@Column(name = "destiny_port", length = 10)
	public String getDestinyPort() {
		return destinyPort;
	}
	public void setDestinyPort(String destinyPort) {
		this.destinyPort = destinyPort;
	}
	@Column(name = "create_name", length = 100)
	public String getCreateName() {
		return createName;
	}
	public void setCreateName(String createName) {
		this.createName = createName;
	}
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "create_time", length = 19)
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	public BaseCloudPortMapping(){}
	
	public BaseCloudPortMapping(PortMapping portMapping, String dcId) {
	    this.dcId = dcId;
	    this.pmId = portMapping.getId();
	    this.prjId = portMapping.getTenant_id();
	    this.protocol = portMapping.getProtocol();
	    this.resourceId = portMapping.getRouter_id();
	    this.resourcePort = portMapping.getRouter_port();
	    this.destinyIp = portMapping.getDestination_ip();
	    this.destinyPort = portMapping.getDestination_port();
	}
	
}

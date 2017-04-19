package com.eayun.virtualization.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.StringUtils;

import com.eayun.eayunstack.model.HealthMonitor;

@Entity
@Table(name = "cloud_ldmonitor")
public class BaseCloudLdMonitor implements java.io.Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1540581401159038735L;
	private String ldmId;
	private String prjId;
	private String dcId;
	private String createName;
	private String ldmType;
	private Long ldmDelay;
	private Long ldmTimeout;
	private Long maxRetries;
	private Character adminStateup;
	private String urlPath;
	
	private Date createTime;
	private String reserve1;
	private String reserve2;
	private String ldmName;
	

	@Id
	@Column(name = "ldm_id", unique = true, nullable = false, length = 100)
	public String getLdmId() {
		return this.ldmId;
	}

	public void setLdmId(String ldmId) {
		this.ldmId = ldmId;
	}

	@Column(name = "prj_id", length = 100)
	public String getPrjId() {
		return this.prjId;
	}
	@Column(name = "ldm_name", length = 100)
	public String getLdmName() {
		return ldmName;
	}

	public void setLdmName(String ldmName) {
		this.ldmName = ldmName;
	}

	public void setPrjId(String prjId) {
		this.prjId = prjId;
	}

	@Column(name = "dc_id", length = 100)
	public String getDcId() {
		return this.dcId;
	}

	public void setDcId(String dcId) {
		this.dcId = dcId;
	}

	@Column(name = "create_name", length = 100)
	public String getCreateName() {
		return this.createName;
	}

	public void setCreateName(String createName) {
		this.createName = createName;
	}

	@Column(name = "ldm_type", length = 10)
	public String getLdmType() {
		return this.ldmType;
	}

	public void setLdmType(String ldmType) {
		this.ldmType = ldmType;
	}

	@Column(name = "ldm_delay", precision = 11, scale = 0)
	public Long getLdmDelay() {
		return this.ldmDelay;
	}

	public void setLdmDelay(Long ldmDelay) {
		this.ldmDelay = ldmDelay;
	}

	@Column(name = "ldm_timeout", precision = 11, scale = 0)
	public Long getLdmTimeout() {
		return this.ldmTimeout;
	}

	public void setLdmTimeout(Long ldmTimeout) {
		this.ldmTimeout = ldmTimeout;
	}

	@Column(name = "max_retries", precision = 11, scale = 0)
	public Long getMaxRetries() {
		return this.maxRetries;
	}

	public void setMaxRetries(Long maxRetries) {
		this.maxRetries = maxRetries;
	}

	@Column(name = "admin_stateup", length = 1)
	public Character getAdminStateup() {
		return this.adminStateup;
	}

	public void setAdminStateup(Character adminStateup) {
		this.adminStateup = adminStateup;
	}
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "create_time", length = 19)
	public Date getCreateTime() {
		return this.createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@Column(name = "reserve1", length = 100)
	public String getReserve1() {
		return this.reserve1;
	}

	public void setReserve1(String reserve1) {
		this.reserve1 = reserve1;
	}

	@Column(name = "reserve2", length = 100)
	public String getReserve2() {
		return this.reserve2;
	}

	public void setReserve2(String reserve2) {
		this.reserve2 = reserve2;
	}
	@Column(name = "url_path", length = 1000)
	public String getUrlPath() {
		return urlPath;
	}

	public void setUrlPath(String urlPath) {
		this.urlPath = urlPath;
	}
	
	public BaseCloudLdMonitor(){}
	
	public BaseCloudLdMonitor(HealthMonitor monitor,String dcId){
		if(null!=monitor){
			this.ldmId = monitor.getId();
			this.prjId = monitor.getTenant_id();
			this.dcId = dcId;
			this.ldmType = monitor.getType();
			if(!StringUtils.isEmpty(monitor.getDelay()))
				this.ldmDelay = Long.parseLong(monitor.getDelay());
			if(!StringUtils.isEmpty(monitor.getTimeout()))
				this.ldmTimeout = Long.parseLong(monitor.getTimeout());
			if(!StringUtils.isEmpty(monitor.getMax_retries()))
			this.maxRetries = Long.parseLong(monitor.getMax_retries());
			this.adminStateup = (monitor.isAdmin_state_up())?'1':'0';
		}
	}
}

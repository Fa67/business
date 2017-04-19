package com.eayun.virtualization.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "cloud_recycle")
public class BaseCloudRecycle implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5709212206786150625L;
	
	@Id
	@Column(name= "cycle_id", length=100)
	private String cycleId;
	@Column(name= "retention_type", length=1)
	private String retentionType;
	@Column(name= "retention_time")
	private int retentionTime;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name= "modify_time", length=19)
	private Date modifyTime;
	
	
	public String getCycleId() {
		return cycleId;
	}
	public String getRetentionType() {
		return retentionType;
	}
	public int getRetentionTime() {
		return retentionTime;
	}
	public Date getModifyTime() {
		return modifyTime;
	}
	public void setCycleId(String cycleId) {
		this.cycleId = cycleId;
	}
	public void setRetentionType(String retentionType) {
		this.retentionType = retentionType;
	}
	public void setRetentionTime(int retentionTime) {
		this.retentionTime = retentionTime;
	}
	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}
}

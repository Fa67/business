package com.eayun.virtualization.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "cloud_ldpoolldmonitor")
public class BaseCloudLdPoolMonitor implements Serializable{
	private static final long serialVersionUID = -908807656906551788L;
	
	private String poolId;
	private String ldmId;
	
	
	
	@Id
	@Column(name = "pool_id",  length = 100)
	public String getPoolId() {
		return poolId;
	}
	public void setPoolId(String poolId) {
		this.poolId = poolId;
	}
	@Id
	@Column(name = "ldm_id", length = 100)
	public String getLdmId() {
		return ldmId;
	}
	public void setLdmId(String ldmId) {
		this.ldmId = ldmId;
	}
	
}

package com.eayun.virtualization.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

/**
 * CloudVm entity. @author MyEclipse Persistence Tools
 */
@Entity
@Table(name = "cloud_vmsecuritygroup")
@IdClass(VmSGroupKey.class)
public class BaseCloudVmSgroup implements java.io.Serializable {
	private static final long serialVersionUID = -11167651198987706L;

	private String vmId;
	private String sgId;

	@Id
	@Column(name = "vm_id",  length = 100)
	public String getVmId() {
		return vmId;
	}

	public void setVmId(String vmId) {
		this.vmId = vmId;
	}

	@Id
	@Column(name = "sg_id", length = 100)
	public String getSgId() {
		return sgId;
	}

	public void setSgId(String sgId) {
		this.sgId = sgId;
	}

	public BaseCloudVmSgroup(String vmId, String sgId) {
		super();
		this.vmId = vmId;
		this.sgId = sgId;
	}

	public BaseCloudVmSgroup() {
		super();
	}

}

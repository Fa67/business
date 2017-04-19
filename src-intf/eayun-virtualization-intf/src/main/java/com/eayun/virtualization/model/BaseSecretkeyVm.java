package com.eayun.virtualization.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "secretkey_vm")
public class BaseSecretkeyVm {
	
	@Id
	@GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name = "sv_id", unique = true, nullable = false, length = 32)
	private String svId;//id
	
	
	@Column(name = "vm_id", length = 32)
	private String vmId;//云主机IDid
	
	
	@Column(name = "secretkey_id", length = 32)
	private String secretkeyId;//秘钥ID


	public String getSvId() {
		return svId;
	}


	public void setSvId(String svId) {
		this.svId = svId;
	}


	public String getVmId() {
		return vmId;
	}


	public void setVmId(String vmId) {
		this.vmId = vmId;
	}


	public String getSecretkeyId() {
		return secretkeyId;
	}


	public void setSecretkeyId(String secretkeyId) {
		this.secretkeyId = secretkeyId;
	}

	public BaseSecretkeyVm(){}
	public BaseSecretkeyVm(String svId, String vmId, String secretkeyId) {
		super();
		this.svId = svId;
		this.vmId = vmId;
		this.secretkeyId = secretkeyId;
	}
	
	
	
	
	

}

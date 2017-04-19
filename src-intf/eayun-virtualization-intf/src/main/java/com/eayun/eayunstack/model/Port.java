package com.eayun.eayunstack.model;

public class Port {
	private String status;
	private FixedIps [] fixed_ips;
	private String id;
	private String [] security_groups;
	private String network_id;
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public FixedIps[] getFixed_ips() {
		return fixed_ips;
	}
	public void setFixed_ips(FixedIps[] fixed_ips) {
		this.fixed_ips = fixed_ips;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String[] getSecurity_groups() {
		return security_groups;
	}
	public void setSecurity_groups(String[] security_groups) {
		this.security_groups = security_groups;
	}
	public String getNetwork_id() {
		return network_id;
	}
	public void setNetwork_id(String network_id) {
		this.network_id = network_id;
	}
}

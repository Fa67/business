package com.eayun.eayunstack.model;

public class Member {
	private String id;
	private String address;
	private String protocol_port;
	private String tenant_id;
	private String weight;
	private String subnet_id;
	private boolean admin_state_up;
	private String status;
	private String[] name;
	private String pool_id;
	private String pool_name;
	private String priority;

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getProtocol_port() {
		return protocol_port;
	}

	public void setProtocol_port(String protocol_port) {
		this.protocol_port = protocol_port;
	}

	public String getTenant_id() {
		return tenant_id;
	}

	public void setTenant_id(String tenant_id) {
		this.tenant_id = tenant_id;
	}

	public String getWeight() {
		return weight;
	}

	public void setWeight(String weight) {
		this.weight = weight;
	}

	public String getSubnet_id() {
		return subnet_id;
	}

	public void setSubnet_id(String subnet_id) {
		this.subnet_id = subnet_id;
	}

	public boolean isAdmin_state_up() {
		return admin_state_up;
	}

	public void setAdmin_state_up(boolean admin_state_up) {
		this.admin_state_up = admin_state_up;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPool_id() {
		return pool_id;
	}

	public void setPool_id(String pool_id) {
		this.pool_id = pool_id;
	}

	public String getPool_name() {
		return pool_name;
	}

	public void setPool_name(String pool_name) {
		this.pool_name = pool_name;
	}

	public String[] getName() {
		return name;
	}

	public void setName(String[] name) {
		this.name = name;
	}

}

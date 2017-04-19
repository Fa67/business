package com.eayun.eayunstack.model;

public class FloatIp {

	private String id;
	private String ip;
	private String fixed_ip;
	private String pool;
	private String instance_id;
	private String tenant_id;
	private String name;
	private String port_id;
	private String fixed_ip_address;
	private String floating_network_id;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getFixed_ip() {
		return fixed_ip;
	}

	public void setFixed_ip(String fixed_ip) {
		this.fixed_ip = fixed_ip;
	}

	public String getPool() {
		return pool;
	}

	public void setPool(String pool) {
		this.pool = pool;
	}

	public String getInstance_id() {
		return instance_id;
	}

	public void setInstance_id(String instance_id) {
		this.instance_id = instance_id;
	}

	public String getTenant_id() {
		return tenant_id;
	}

	public String getPort_id() {
		return port_id;
	}

	public void setPort_id(String port_id) {
		this.port_id = port_id;
	}

	public void setTenant_id(String tenant_id) {
		this.tenant_id = tenant_id;
	}

	public String getFixed_ip_address() {
		return fixed_ip_address;
	}

	public void setFixed_ip_address(String fixed_ip_address) {
		this.fixed_ip_address = fixed_ip_address;
	}

	public String getFloating_network_id() {
		return floating_network_id;
	}

	public void setFloating_network_id(String floating_network_id) {
		this.floating_network_id = floating_network_id;
	}

}

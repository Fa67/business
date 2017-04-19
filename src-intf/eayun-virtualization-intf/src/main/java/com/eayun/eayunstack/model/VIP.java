package com.eayun.eayunstack.model;


public class VIP {
	private String id;
	private String name;
	private String address;
	private String protocol;
	private String protocol_port;
	private String subnet_id;
	private String subnername;
	private String port_id;
	private String portname;
	private String tenant_id;
	private String pool_id;
	private String poolname;
	private String connection_limit;
	private String status;
	private boolean admin_state_up;
	private String description;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getProtocol_port() {
		return protocol_port;
	}

	public void setProtocol_port(String protocol_port) {
		this.protocol_port = protocol_port;
	}

	public String getSubnet_id() {
		return subnet_id;
	}

	public void setSubnet_id(String subnet_id) {
		this.subnet_id = subnet_id;
	}

	public String getPort_id() {
		return port_id;
	}

	public void setPort_id(String port_id) {
		this.port_id = port_id;
	}

	public String getTenant_id() {
		return tenant_id;
	}

	public void setTenant_id(String tenant_id) {
		this.tenant_id = tenant_id;
	}

	public String getPool_id() {
		return pool_id;
	}

	public void setPool_id(String pool_id) {
		this.pool_id = pool_id;
	}

	public String getConnection_limit() {
		return connection_limit;
	}

	public void setConnection_limit(String connection_limit) {
		this.connection_limit = connection_limit;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean getAdmin_state_up() {
		return admin_state_up;
	}

	public void setAdmin_state_up(boolean admin_state_up) {
		this.admin_state_up = admin_state_up;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSubnername() {
		return subnername;
	}

	public void setSubnername(String subnername) {
		this.subnername = subnername;
	}

	public String getPortname() {
		return portname;
	}

	public void setPortname(String portname) {
		this.portname = portname;
	}

	public String getPoolname() {
		return poolname;
	}

	public void setPoolname(String poolname) {
		this.poolname = poolname;
	}

}

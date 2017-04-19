package com.eayun.eayunstack.model;

public class Route {

	private String id;// 路由的id
	private String name;// 路由名称
	private String status;// 状态 ACTIVE
	private String admin_state_up;// true false
	private String tenant_id;// 项目ID
	private NetworkId external_gateway_info;

	private String subnet_id;
	private String port_id;
	private String ip_address;

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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAdmin_state_up() {
		return admin_state_up;
	}

	public void setAdmin_state_up(String admin_state_up) {
		this.admin_state_up = admin_state_up;
	}

	public String getTenant_id() {
		return tenant_id;
	}

	public void setTenant_id(String tenant_id) {
		this.tenant_id = tenant_id;
	}

	public NetworkId getExternal_gateway_info() {
		return external_gateway_info;
	}

	public void setExternal_gateway_info(NetworkId external_gateway_info) {
		this.external_gateway_info = external_gateway_info;
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

	public String getIp_address() {
		return ip_address;
	}

	public void setIp_address(String ip_address) {
		this.ip_address = ip_address;
	}

	
}

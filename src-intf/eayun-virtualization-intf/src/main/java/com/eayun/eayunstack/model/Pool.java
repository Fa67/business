package com.eayun.eayunstack.model;

public class Pool {
	private String id;
	private String tenant_id;
	private String name;
	private String description;
	private String protocol;
	private String lb_algorithm;
	private String[] session_persistence;
	private String healthmonitor_id;
	private String[] members;
	private boolean admin_state_up;
	private String status;
	private String vip_id;
	private String vipname;
	private String subnet_id;
	private String lb_method;
	private String provider;
	private String subnetwork;
	private String loadbalance;
	private String created_at;
	private String datacentername;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTenant_id() {
		return tenant_id;
	}

	public void setTenant_id(String tenant_id) {
		this.tenant_id = tenant_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getLb_algorithm() {
		return lb_algorithm;
	}

	public void setLb_algorithm(String lb_algorithm) {
		this.lb_algorithm = lb_algorithm;
	}

	public String[] getSession_persistence() {
		return session_persistence;
	}

	public void setSession_persistence(String[] session_persistence) {
		this.session_persistence = session_persistence;
	}

	public String getHealthmonitor_id() {
		return healthmonitor_id;
	}

	public void setHealthmonitor_id(String healthmonitor_id) {
		this.healthmonitor_id = healthmonitor_id;
	}

	public String[] getMembers() {
		return members;
	}

	public void setMembers(String[] members) {
		this.members = members;
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

	public String getVip_id() {
		return vip_id;
	}

	public void setVip_id(String vip_id) {
		this.vip_id = vip_id;
	}

	public String getSubnet_id() {
		return subnet_id;
	}

	public void setSubnet_id(String subnet_id) {
		this.subnet_id = subnet_id;
	}

	public String getLb_method() {
		return lb_method;
	}

	public void setLb_method(String lb_method) {
		this.lb_method = lb_method;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getSubnetwork() {
		return subnetwork;
	}

	public void setSubnetwork(String subnetwork) {
		this.subnetwork = subnetwork;
	}

	public String getLoadbalance() {
		return loadbalance;
	}

	public void setLoadbalance(String loadbalance) {
		this.loadbalance = loadbalance;
	}

	public String getCreated_at() {
		return created_at;
	}

	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}

	public String getDatacentername() {
		return datacentername;
	}

	public void setDatacentername(String datacentername) {
		this.datacentername = datacentername;
	}

	public String getVipname() {
		return vipname;
	}

	public void setVipname(String vipname) {
		this.vipname = vipname;
	}

}

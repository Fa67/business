package com.eayun.eayunstack.model;

public class Network {
	
	private String id;
	private String name;
	private boolean router_external;//是否外部网络
	private boolean admin_state_up;
	private boolean shared;
	private String status;
	private String tenant_id;
	private String [] subnets;	
	
	
	private String rspcode;
	private String rspdesc;
	
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
	public boolean getRouter_external() {
		return router_external;
	}
	public void setRouter_external(boolean router_external) {
		this.router_external = router_external;
	}
	public boolean getAdmin_state_up() {
		return admin_state_up;
	}
	public void setAdmin_state_up(boolean admin_state_up) {
		this.admin_state_up = admin_state_up;
	}
	public boolean getShared() {
		return shared;
	}
	public void setShared(boolean shared) {
		this.shared = shared;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getTenant_id() {
		return tenant_id;
	}
	public void setTenant_id(String tenant_id) {
		this.tenant_id = tenant_id;
	}
	public String[] getSubnets() {
		return subnets;
	}
	public void setSubnets(String[] subnets) {
		this.subnets = subnets;
	}
	public String getRspcode() {
		return rspcode;
	}
	public void setRspcode(String rspcode) {
		this.rspcode = rspcode;
	}
	public String getRspdesc() {
		return rspdesc;
	}
	public void setRspdesc(String rspdesc) {
		this.rspdesc = rspdesc;
	}
	
	
	
}

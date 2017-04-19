package com.eayun.eayunstack.model;

public class MeteringLabel {
	private String name;
	private String id;
	private String tenant_id;
	private String description;
	private String shared;
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
	public String getTenant_id() {
		return tenant_id;
	}
	public void setTenant_id(String tenant_id) {
		this.tenant_id = tenant_id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getShared() {
		return shared;
	}
	public void setShared(String shared) {
		this.shared = shared;
	}
	
}

package com.eayun.eayunstack.model;

public class LoadBalance {
	private String id;
	private String name;
	private String description;
	private String tenant_id;
	private boolean admin_state_up;
	private String status;

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTenant_id() {
		return tenant_id;
	}

	public void setTenant_id(String tenant_id) {
		this.tenant_id = tenant_id;
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

}

package com.eayun.eayunstack.model;

public class Firewall {
	private boolean admin_state_up;
	private String description;
	private String firewall_policy_id;
	private String id;
	private String name;
	private String status;
	private String tenant_id;
	private boolean shared;

	public boolean isAdmin_state_up() {
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

	public String getFirewall_policy_id() {
		return firewall_policy_id;
	}

	public void setFirewall_policy_id(String firewall_policy_id) {
		this.firewall_policy_id = firewall_policy_id;
	}

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

	public String getTenant_id() {
		return tenant_id;
	}

	public void setTenant_id(String tenant_id) {
		this.tenant_id = tenant_id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isShared() {
		return shared;
	}

	public void setShared(boolean shared) {
		this.shared = shared;
	}

}

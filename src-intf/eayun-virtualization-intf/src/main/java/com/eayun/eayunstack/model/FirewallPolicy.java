package com.eayun.eayunstack.model;

public class FirewallPolicy {
	private String id;
	private String name;
	private boolean shared;
	private String description;
	private String[] firewall_rules;
	private boolean audited;
	private String tenant_id;
	private String firewall_id;
	private boolean exist = false;

	public String getFirewall_id() {
		return firewall_id;
	}

	public void setFirewall_id(String firewall_id) {
		this.firewall_id = firewall_id;
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

	public boolean isShared() {
		return shared;
	}

	public void setShared(boolean shared) {
		this.shared = shared;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String[] getFirewall_rules() {
		return firewall_rules;
	}

	public void setFirewall_rules(String[] firewall_rules) {
		this.firewall_rules = firewall_rules;
	}

	public boolean isAudited() {
		return audited;
	}

	public void setAudited(boolean audited) {
		this.audited = audited;
	}

	public String getTenant_id() {
		return tenant_id;
	}

	public void setTenant_id(String tenant_id) {
		this.tenant_id = tenant_id;
	}

	public boolean isExist() {
		return exist;
	}

	public void setExist(boolean exist) {
		this.exist = exist;
	}

}

package com.eayun.eayunstack.model;

public class Listener {
	String id;
	String tenantId;
	String name;
	String description;
	String protocol;
	String protocolPort;
	String loadBalanceId;
	boolean adminStateUp;
	String status;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
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

	public String getProtocolPort() {
		return protocolPort;
	}

	public void setProtocolPort(String protocolPort) {
		this.protocolPort = protocolPort;
	}

	public String getLoadBalanceId() {
		return loadBalanceId;
	}

	public void setLoadBalanceId(String loadBalanceId) {
		this.loadBalanceId = loadBalanceId;
	}

	public boolean isAdminStateUp() {
		return adminStateUp;
	}

	public void setAdminStateUp(boolean adminStateUp) {
		this.adminStateUp = adminStateUp;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}

package com.eayun.eayunstack.model;

public class SubNetwork {

	private String id;
	private String name;
	private String network_id;
	private String enable_dhcp;
	private String cidr;
	private String ip_version;
	private String tenant_id;
	private String gateway_ip;
	private String[] dns_nameservers;
	private String[] host_routes;
	private Poolresource[] allocation_pools;

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

	public String getNetwork_id() {
		return network_id;
	}

	public void setNetwork_id(String network_id) {
		this.network_id = network_id;
	}

	public String getEnable_dhcp() {
		return enable_dhcp;
	}

	public void setEnable_dhcp(String enable_dhcp) {
		this.enable_dhcp = enable_dhcp;
	}

	public String getCidr() {
		return cidr;
	}

	public void setCidr(String cidr) {
		this.cidr = cidr;
	}

	public String getIp_version() {
		return ip_version;
	}

	public void setIp_version(String ip_version) {
		this.ip_version = ip_version;
	}

	public String getTenant_id() {
		return tenant_id;
	}

	public void setTenant_id(String tenant_id) {
		this.tenant_id = tenant_id;
	}

	public String getGateway_ip() {
		return gateway_ip;
	}

	public void setGateway_ip(String gateway_ip) {
		this.gateway_ip = gateway_ip;
	}

	public String[] getDns_nameservers() {
		return dns_nameservers;
	}

	public void setDns_nameservers(String[] dns_nameservers) {
		this.dns_nameservers = dns_nameservers;
	}

	public String[] getHost_routes() {
		return host_routes;
	}

	public void setHost_routes(String[] host_routes) {
		this.host_routes = host_routes;
	}

	public Poolresource[] getAllocation_pools() {
		return allocation_pools;
	}

	public void setAllocation_pools(Poolresource[] allocation_pools) {
		this.allocation_pools = allocation_pools;
	}

}

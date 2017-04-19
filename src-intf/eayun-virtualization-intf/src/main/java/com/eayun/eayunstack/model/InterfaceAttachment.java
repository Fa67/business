package com.eayun.eayunstack.model;

public class InterfaceAttachment {
	
	private String port_state;
	private String port_id;
	private String net_id;
	private String mac_addr;
	private FixedIps [] fixed_ips;
	
	public String getPort_state() {
		return port_state;
	}
	public String getPort_id() {
		return port_id;
	}
	public String getNet_id() {
		return net_id;
	}
	public String getMac_addr() {
		return mac_addr;
	}
	public FixedIps[] getFixed_ips() {
		return fixed_ips;
	}
	public void setPort_state(String port_state) {
		this.port_state = port_state;
	}
	public void setPort_id(String port_id) {
		this.port_id = port_id;
	}
	public void setNet_id(String net_id) {
		this.net_id = net_id;
	}
	public void setMac_addr(String mac_addr) {
		this.mac_addr = mac_addr;
	}
	public void setFixed_ips(FixedIps[] fixed_ips) {
		this.fixed_ips = fixed_ips;
	}
}

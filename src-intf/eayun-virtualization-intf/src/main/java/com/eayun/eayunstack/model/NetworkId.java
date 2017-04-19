package com.eayun.eayunstack.model;

public class NetworkId {

	private String network_id;

	private boolean enable_snat;

	public String getNetwork_id() {
		return network_id;
	}

	public void setNetwork_id(String network_id) {
		this.network_id = network_id;
	}

	public boolean getEnable_snat() {
		return enable_snat;
	}

	public void setEnable_snat(boolean enable_snat) {
		this.enable_snat = enable_snat;
	}

}

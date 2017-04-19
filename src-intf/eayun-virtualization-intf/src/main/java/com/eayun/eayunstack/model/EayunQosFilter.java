package com.eayun.eayunstack.model;

public class EayunQosFilter {
	private String id;
	private String tenant_id;
	private String qos_id;
	private String queue_id;
	private int prio;
	private String protocol;
	private int src_port;
	private int dst_port;
	private String src_addr;
	private String dst_addr;
	private String custom_match;

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

	public String getQos_id() {
		return qos_id;
	}

	public void setQos_id(String qos_id) {
		this.qos_id = qos_id;
	}

	public String getQueue_id() {
		return queue_id;
	}

	public void setQueue_id(String queue_id) {
		this.queue_id = queue_id;
	}

	public int getPrio() {
		return prio;
	}

	public void setPrio(int prio) {
		this.prio = prio;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public int getSrc_port() {
		return src_port;
	}

	public void setSrc_port(int src_port) {
		this.src_port = src_port;
	}

	public int getDst_port() {
		return dst_port;
	}

	public void setDst_port(int dst_port) {
		this.dst_port = dst_port;
	}

	public String getSrc_addr() {
		return src_addr;
	}

	public void setSrc_addr(String src_addr) {
		this.src_addr = src_addr;
	}

	public String getDst_addr() {
		return dst_addr;
	}

	public void setDst_addr(String dst_addr) {
		this.dst_addr = dst_addr;
	}

	public String getCustom_match() {
		return custom_match;
	}

	public void setCustom_match(String custom_match) {
		this.custom_match = custom_match;
	}

}

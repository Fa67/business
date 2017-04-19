package com.eayun.eayunstack.model;

public class LabelRule {
	private String id;
	private String exluded;
	private String direction;
	private String remote_ip_prefix;
	private String metering_label_id;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getExluded() {
		return exluded;
	}
	public void setExluded(String exluded) {
		this.exluded = exluded;
	}
	public String getDirection() {
		return direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}
	public String getRemote_ip_prefix() {
		return remote_ip_prefix;
	}
	public void setRemote_ip_prefix(String remote_ip_prefix) {
		this.remote_ip_prefix = remote_ip_prefix;
	}
	public String getMetering_label_id() {
		return metering_label_id;
	}
	public void setMetering_label_id(String metering_label_id) {
		this.metering_label_id = metering_label_id;
	}
		
}

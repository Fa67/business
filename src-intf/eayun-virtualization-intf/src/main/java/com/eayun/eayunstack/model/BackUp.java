package com.eayun.eayunstack.model;

public class BackUp {
	private String id;
	private String volume_id;
	private String availability_zone;
	private String status;
	private String container;
	private String name;
	private String created_at;
	private Link[] links;
	private String size;
	private String fail_reason;
	private String object_count;
	
	
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
	public Link[] getLinks() {
		return links;
	}
	public void setLinks(Link[] links) {
		this.links = links;
	}
	public String getVolume_id() {
		return volume_id;
	}
	public void setVolume_id(String volume_id) {
		this.volume_id = volume_id;
	}
	public String getAvailability_zone() {
		return availability_zone;
	}
	public void setAvailability_zone(String availability_zone) {
		this.availability_zone = availability_zone;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getContainer() {
		return container;
	}
	public void setContainer(String container) {
		this.container = container;
	}
	public String getCreated_at() {
		return created_at;
	}
	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public String getFail_reason() {
		return fail_reason;
	}
	public void setFail_reason(String fail_reason) {
		this.fail_reason = fail_reason;
	}
	public String getObject_count() {
		return object_count;
	}
	public void setObject_count(String object_count) {
		this.object_count = object_count;
	}
	
	
	
	
	
	

}

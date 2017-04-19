package com.eayun.eayunstack.model;

public class Flavor {
	
	private String id;
	private String name;
	private String vcpus;
	private String ram;
	private String disk;
	private Link[] links;
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
	public String getVcpus() {
		return vcpus;
	}
	public void setVcpus(String vcpus) {
		this.vcpus = vcpus;
	}
	public String getRam() {
		return ram;
	}
	public void setRam(String ram) {
		this.ram = ram;
	}
	public String getDisk() {
		return disk;
	}
	public void setDisk(String disk) {
		this.disk = disk;
	}
	public Link[] getLinks() {
		return links;
	}
	public void setLinks(Link[] links) {
		this.links = links;
	}
}

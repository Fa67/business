package com.eayun.eayunstack.model;

public class VolumeType {
	private String id;//云硬盘类型id
	private String name;//云硬盘类型名称
	private String extra_specs;
	private String is_public;//会否是公共的
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
	public String getExtra_specs() {
		return extra_specs;
	}
	public void setExtra_specs(String extra_specs) {
		this.extra_specs = extra_specs;
	}
	public String getIs_public() {
		return is_public;
	}
	public void setIs_public(String is_public) {
		this.is_public = is_public;
	}
	
	
	

}

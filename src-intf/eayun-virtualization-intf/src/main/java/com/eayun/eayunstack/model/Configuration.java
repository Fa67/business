package com.eayun.eayunstack.model;

/**
 * 云数据库实例配置文件 -- 底层数据映射
 *                       
 * @Filename: Configuration.java
 * @Description: 
 * @Version: 1.0
 * @Author: LiuZhuangzhuang
 * @Email: zhuangzhuang.liu@eayun.com
 * @History:<br>
 *<li>Date: 2017年2月21日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class Configuration {
	private String id;
	private String name;
	private Link [] links;
	
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
	
}

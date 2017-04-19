package com.eayun.eayunstack.model;

/**
 * 云数据库实例 -- 底层数据映射
 *                       
 * @Filename: ReplicaOf.java
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
public class ReplicaOf {
	
	private String id;
	private Link [] links;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Link[] getLinks() {
		return links;
	}
	public void setLinks(Link[] links) {
		this.links = links;
	}
	
	
}

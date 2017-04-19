package com.eayun.notice.model;

import java.util.Date;

public class MessageResourcesExpiredModel {
	 private String resourcesType;//资源类型
	private  String resourcesName;//资源名称
	private  Date expireDate;//停用时间
	public String getResourcesType() {
		return resourcesType;
	}
	public void setResourcesType(String resourcesType) {
		this.resourcesType = resourcesType;
	}
	public String getResourcesName() {
		return resourcesName;
	}
	public void setResourcesName(String resourcesName) {
		this.resourcesName = resourcesName;
	}
	public Date getExpireDate() {
		return expireDate;
	}
	public void setExpireDate(Date expireDate) {
		this.expireDate = expireDate;
	}
	
	
}

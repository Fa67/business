package com.eayun.notice.model;

import com.eayun.order.model.BaseOrderResource;

public class MessageOrderResourceNotice extends BaseOrderResource {
	/**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -9136227373664305002L;
    private String resourceType;//资源类型
	private String resourceName;//资源名称
	public String getResourceType() {
		return resourceType;
	}
	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}
	public String getResourceName() {
		return resourceName;
	}
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
}

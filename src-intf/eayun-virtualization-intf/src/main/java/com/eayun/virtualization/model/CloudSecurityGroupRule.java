package com.eayun.virtualization.model;

public class CloudSecurityGroupRule extends BaseCloudSecurityGroupRule {
	/**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1416442174373221074L;
    private String remoteGroupName;// 设置指定的那个安全组的名称；

	public String getRemoteGroupName() {
		return remoteGroupName;
	}

	public void setRemoteGroupName(String remoteGroupName) {
		this.remoteGroupName = remoteGroupName;
	}
}

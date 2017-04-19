package com.eayun.virtualization.model;

public class CloudSubNetWork extends BaseCloudSubNetWork {
	private String prjName;
	private String netName;
	private String tagName;//标签名称
	private String routeName;//路由用到
	private String subnetTypeStr;//子网类型的中文释义 自管 受管
	
	public String getRouteName() {
		return routeName;
	}
	public void setRouteName(String routeName) {
		this.routeName = routeName;
	}
	public String getTagName() {
		return tagName;
	}
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}
	public String getPrjName() {
		return prjName;
	}
	public void setPrjName(String prjName) {
		this.prjName = prjName;
	}
	public String getNetName() {
		return netName;
	}
	public void setNetName(String netName) {
		this.netName = netName;
	}
	public String getSubnetTypeStr() {
		return subnetTypeStr;
	}
	public void setSubnetTypeStr(String subnetTypeStr) {
		this.subnetTypeStr = subnetTypeStr;
	}
}

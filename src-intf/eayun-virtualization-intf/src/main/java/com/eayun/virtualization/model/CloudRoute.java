package com.eayun.virtualization.model;

public class CloudRoute extends BaseCloudRoute{
	private String prjName;
	private String connectsubnetnum;       //已连接子网数量	
	private String isDeleting;//是否正在删除
	private String routerExternal;//路由是否外部网络
	private String dcName;//数据中心名称
	private String netName;//外网名称
	private String bandCount;//设置的当前项目下的路由带宽总数
	private String statusForRoute;
	private String networkName;
	private String cusId;	//客户ID
	private String cusOrg;	//客户所属组织
	private String chargeState;  //使用私有网络的chargeState
	
	public String getStatusForRoute() {
		return statusForRoute;
	}
	public void setStatusForRoute(String statusForRoute) {
		this.statusForRoute = statusForRoute;
	}
	public String getBandCount() {
		return bandCount;
	}
	public void setBandCount(String bandCount) {
		this.bandCount = bandCount;
	}
	public String getNetName() {
		return netName;
	}
	public void setNetName(String netName) {
		this.netName = netName;
	}
	public String getPrjName() {
		return prjName;
	}
	public void setPrjName(String prjName) {
		this.prjName = prjName;
	}
	public String getConnectsubnetnum() {
		return connectsubnetnum;
	}
	public void setConnectsubnetnum(String connectsubnetnum) {
		this.connectsubnetnum = connectsubnetnum;
	}
	public String getIsDeleting() {
		return isDeleting;
	}
	public void setIsDeleting(String isDeleting) {
		this.isDeleting = isDeleting;
	}
	public String getRouterExternal() {
		return routerExternal;
	}
	public void setRouterExternal(String routerExternal) {
		this.routerExternal = routerExternal;
	}
	public String getDcName() {
		return dcName;
	}
	public void setDcName(String dcName) {
		this.dcName = dcName;
	}
	public String getNetworkName() {
		return networkName;
	}
	public void setNetworkName(String networkName) {
		this.networkName = networkName;
	}
	public String getCusId() {
		return cusId;
	}
	public void setCusId(String cusId) {
		this.cusId = cusId;
	}
	public String getCusOrg() {
		return cusOrg;
	}
	public void setCusOrg(String cusOrg) {
		this.cusOrg = cusOrg;
	}
	public String getChargeState() {
		return chargeState;
	}
	public void setChargeState(String chargeState) {
		this.chargeState = chargeState;
	}
	
	
}

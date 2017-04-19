package com.eayun.virtualization.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.eayun.eayunstack.model.Route;



@Entity
@Table(name = "cloud_route")
public class BaseCloudRoute {
	/*用到之前的routeVoe类，作为附属的属性设置，connectsubnetnum，isDeleting,Outnet*/
	private String routeId;
	private String routeName;
	private String createName;
	private Date createTime;
	private String dcId;//数据中心id
	private String prjId;//所在的项目ID
	private String routeStatus;
	private String netId;
	private int rate=0;
	private int rateOld=0;
	private String qosId;
	private String defaultQueueId;
	private String filterQueueId;
	private String netWorkId;
	private String gatewayIp;
	
	
	@Id
	@Column(name = "route_id", unique = true, nullable = false, length = 100)
	public String getRouteId() {
		return routeId;
	}
	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}
	@Column(name = "qos_id", length = 100)
	public String getQosId() {
		return qosId;
	}
	public void setQosId(String qosId) {
		this.qosId = qosId;
	}
	@Column(name = "route_name", length = 100)
	public String getRouteName() {
		return routeName;
	}
	public void setRouteName(String routeName) {
		this.routeName = routeName;
	}
	@Column(name = "create_name", length = 100)
	public String getCreateName() {
		return createName;
	}
	public void setCreateName(String createName) {
		this.createName = createName;
	}
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "create_time", length = 19)
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	@Column(name = "dc_id", length = 100)
	public String getDcId() {
		return dcId;
	}
	public void setDcId(String dcId) {
		this.dcId = dcId;
	}
	@Column(name = "prj_id", length = 100)
	public String getPrjId() {
		return prjId;
	}
	public void setPrjId(String prjId) {
		this.prjId = prjId;
	}
	@Column(name = "route_status", length = 50)
	public String getRouteStatus() {
		return routeStatus;
	}
	public void setRouteStatus(String routeStatus) {
		this.routeStatus = routeStatus;
	}
	
	@Column(name = "net_id", length = 100)
	public String getNetId() {
		return netId;
	}
	public void setNetId(String netId) {
		this.netId = netId;
	}
	@Column(name = "network_id", length = 100)
	public String getNetWorkId() {
		return netWorkId;
	}
	public void setNetWorkId(String netWorkId) {
		this.netWorkId = netWorkId;
	}
	@Column(name = "rate", length = 9)
	public int getRate() {
		return rate;
	}
	public void setRate(int rate) {
		this.rate = rate;
	}
	@Column(name = "rate_old", length = 9)
	public int getRateOld() {
		return rateOld;
	}
	public void setRateOld(int rateOld) {
		this.rateOld = rateOld;
	}
	@Column(name = "default_queue_id", length = 100)
	public String getDefaultQueueId() {
		return defaultQueueId;
	}
	public void setDefaultQueueId(String defaultQueueId) {
		this.defaultQueueId = defaultQueueId;
	}
	
	@Column(name = "filter_queue_id", length = 100)
	public String getFilterQueueId() {
		return filterQueueId;
	}
	
	public void setFilterQueueId(String filterQueueId) {
		this.filterQueueId = filterQueueId;
	}
	@Column(name = "gateway_ip", length = 100)
	public String getGatewayIp() {
		return gatewayIp;
	}
	public void setGatewayIp(String gatewayIp) {
		this.gatewayIp = gatewayIp;
	}
	public BaseCloudRoute(){}
	
	
	public BaseCloudRoute(Route route,String dcId){
		if(null!=route){
			this.routeId=route.getId();
			this.routeName=route.getName();
			this.dcId=dcId;
			this.prjId=route.getTenant_id();
			this.routeStatus=route.getStatus()!=null?route.getStatus().toUpperCase():"";
			if(null!=route.getExternal_gateway_info()){
				this.netId=route.getExternal_gateway_info().getNetwork_id();
			}
		}
	}
	
	
	
}

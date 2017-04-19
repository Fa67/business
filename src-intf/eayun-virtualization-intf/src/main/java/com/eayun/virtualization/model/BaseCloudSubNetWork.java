package com.eayun.virtualization.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.StringUtils;

import com.eayun.eayunstack.model.Poolresource;
import com.eayun.eayunstack.model.SubNetwork;
@Entity
@Table(name = "cloud_subnetwork")
public class BaseCloudSubNetWork {


	
	private String subnetId;//子网id
	private String subnetName;//子网名称
	private String createName;//创建人
	private Date createTime;//创建时间
	private String prjId;//项目id
	private String dcId;//数据中心id
	private String netId;//网络id
	private String routeId;//路由id
	private String ipVersion;//Ip版本
	private String cidr;//网络地址
	private String gatewayIp;//网关ip
	private String isShared;//共享
	private String pooldata;//网络地址池
	private String isForbiddengw="0"; //是否禁用网关
	private String inLabelRuleId;
	private String outLabelRuleId;
	private String dns;//子网的dns
	private String subnetType;//子网类型：0代表自管子网，1代表受管子网
	@Id
	@Column(name = "subnet_id", unique = true, nullable = false, length = 100)
	public String getSubnetId() {
		return subnetId;
	}
	public void setSubnetId(String subnetId) {
		this.subnetId = subnetId;
	}
	
	@Column(name = "subnet_name", length = 100)
	public String getSubnetName() {
		return subnetName;
	}
	public void setSubnetName(String subnetName) {
		this.subnetName = subnetName;
	}
	
	@Column(name = "create_name", length = 50)
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
	
	@Column(name = "prj_id", length = 100)
	public String getPrjId() {
		return prjId;
	}
	public void setPrjId(String prjId) {
		this.prjId = prjId;
	}
	
	@Column(name = "dc_id", length = 100)
	public String getDcId() {
		return dcId;
	}
	public void setDcId(String dcId) {
		this.dcId = dcId;
	}
	
	@Column(name = "net_id", length = 100)
	public String getNetId() {
		return netId;
	}
	public void setNetId(String netId) {
		this.netId = netId;
	}
	
	@Column(name = "route_id", length = 100)
	public String getRouteId() {
		return routeId;
	}
	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}
	
	@Column(name = "ip_version", length = 1)
	public String getIpVersion() {
		return ipVersion;
	}
	public void setIpVersion(String ipVersion) {
		this.ipVersion = ipVersion;
	}
	
	@Column(name = "cidr", length = 100)
	public String getCidr() {
		return cidr;
	}
	public void setCidr(String cidr) {
		this.cidr = cidr;
	}
	
	@Column(name = "gateway_ip", length = 100)
	public String getGatewayIp() {
		return gatewayIp;
	}
	public void setGatewayIp(String gatewayIp) {
		this.gatewayIp = gatewayIp;
	}
	
	@Column(name = "is_shared", length = 1)
	public String getIsShared() {
		return isShared;
	}
	public void setIsShared(String isShared) {
		this.isShared = isShared;
	}
	
	@Column(name = "pooldata", length = 100)
	public String getPooldata() {
		return pooldata;
	}
	public void setPooldata(String pooldata) {
		this.pooldata = pooldata;
	}
	
	@Column(name = "is_forbiddengw", length = 1)
	public String getIsForbiddengw() {
		return isForbiddengw;
	}
	public void setIsForbiddengw(String isForbiddengw) {
		this.isForbiddengw = isForbiddengw;
	}
	
	@Column(name= "in_label_rule_id", length = 100)
	public String getInLabelRuleId() {
		return inLabelRuleId;
	}
	public void setInLabelRuleId(String inLabelRuleId) {
		this.inLabelRuleId = inLabelRuleId;
	}
	
	@Column(name = "out_label_rule_id", length = 100)
	public String getOutLabelRuleId() {
		return outLabelRuleId;
	}
	public void setOutLabelRuleId(String outLabelRuleId) {
		this.outLabelRuleId = outLabelRuleId;
	}
	@Column(name = "dns", length = 100)
	public String getDns() {
		return dns;
	}
	public void setDns(String dns) {
		this.dns = dns;
	}
	@Column(name = "subnet_type", length = 1)
	public String getSubnetType() {
		return subnetType;
	}
	public void setSubnetType(String subnetType) {
		this.subnetType = subnetType;
	}
	public BaseCloudSubNetWork(){}
	
	public BaseCloudSubNetWork (SubNetwork network,String dcId){
		if(null!=network){
			this.subnetId = network.getId();
			this.subnetName = network.getName();
			this.prjId = network.getTenant_id();
			this.dcId = dcId;
			this.netId = network.getNetwork_id();
			this.ipVersion = network.getIp_version();
			this.cidr = network.getCidr();
			this.gatewayIp = network.getGateway_ip();
			if(!StringUtils.isEmpty(network.getGateway_ip())){
				this.isForbiddengw="0";
			}
			else{
				this.isForbiddengw="1";
			}
//			this.inLabelRuleId = network.getInLabelRuleId();
//			this.outLabelRuleId = network.getOutLabelRuleId();
			if(null!=network.getAllocation_pools()&&network.getAllocation_pools().length>0){
				String pooldateStr="";
				for(Poolresource pr1:network.getAllocation_pools()){
					pooldateStr=pooldateStr+pr1.getStart()+","+pr1.getEnd()+";";
				}
				if(null!=pooldateStr&&pooldateStr.length()>1){
					pooldateStr = pooldateStr.substring(0, pooldateStr.length()-1);
				}
				this.pooldata =pooldateStr ;
			}
		}
	}

}

package com.eayun.virtualization.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.eayun.eayunstack.model.Network;


@Entity
@Table(name = "cloud_network")
public class BaseCloudNetwork implements java.io.Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8527169905289664148L;
	/**
	 * 
	 */
	private String netId;				//网络id
	private String netName;				//网络名称
	private String createName;			//创建人
	private Date createTime;			//创建时间
	private String prjId;				//项目id
	private String dcId;				//数据中心id
	private String netStatus;			//状态
	private String adminStateup;		//管理员状态
	private String isShared;			//共享
	private String routerExternal;		//是否外网
	private String payType;				//付款方式：1预付费，2后付费
	private String chargeState;			//计费状态：0正常，1余额不足，2已欠费，3停服务
	private Date endTime;				//到期时间（针对预付费，后付费为null）
	private String isVisible = "1";     //是否展现给用户看：1展示，0隐藏
	
	
	// Property accessors
	@Id
	@Column(name = "net_id", unique = true, nullable = false, length = 100)
	public String getNetId() {
		return netId;
	}
	public void setNetId(String netId) {
		this.netId = netId;
	}
	
	@Column(name = "net_name", length = 100)
	public String getNetName() {
		return netName;
	}
	public void setNetName(String netName) {
		this.netName = netName;
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
	
	@Column(name = "net_status", length = 20)
	public String getNetStatus() {
		return netStatus;
	}
	public void setNetStatus(String netStatus) {
		this.netStatus = netStatus;
	}
	
	@Column(name = "admin_stateup", length = 100)
	public String getAdminStateup() {
		return adminStateup;
	}
	public void setAdminStateup(String adminStateup) {
		this.adminStateup = adminStateup;
	}
	
	@Column(name = "is_shared", length = 100)
	public String getIsShared() {
		return isShared;
	}
	public void setIsShared(String isShared) {
		this.isShared = isShared;
	}
	
	@Column(name = "router_external", length = 1)
	public String getRouterExternal() {
		return routerExternal;
	}
	public void setRouterExternal(String routerExternal) {
		this.routerExternal = routerExternal;
	}
	
	@Column(name = "pay_type", length = 1)
	public String getPayType() {
		return payType;
	}
	public void setPayType(String payType) {
		this.payType = payType;
	}
	
	@Column(name = "charge_state", length = 1)
	public String getChargeState() {
		return chargeState;
	}
	public void setChargeState(String chargeState) {
		this.chargeState = chargeState;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "end_time", length = 19)
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	
	@Column(name = "is_visible", length = 1)
	public String getIsVisible() {
        return isVisible;
    }
	
    public void setIsVisible(String isVisible) {
        this.isVisible = isVisible;
    }
    public BaseCloudNetwork(){}
	
	public BaseCloudNetwork (Network network,String dcId){
		if(null!=network){
			this.netId = network.getId();
			this.netName = network.getName();
			this.prjId = network.getTenant_id();
			this.dcId = dcId;
			this.netStatus = network.getStatus()!=null?network.getStatus().toUpperCase():"";
			this.adminStateup = (network.getAdmin_state_up())?"1":"0";
			this.isShared = (network.getShared())?"1":"0";
			this.routerExternal = (network.getRouter_external())?"1":"0";
		}
	}
	
	
}
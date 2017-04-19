package com.eayun.virtualization.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.eayun.eayunstack.model.Pool;

@Entity
@Table(name = "cloud_ldpool")
public class BaseCloudLdPool implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2235471096509851590L;
	private String poolId;
	private String poolName;
	private String prjId;
	private String dcId;
	private String poolDescription;
	private String poolProvider;
	private String subnetId;
	private String vipId;
	private String poolProtocol;
	private String lbMethod;
	private String poolStatus;
	private Character adminStateup;
	private String createName;
	private Date createTime;
	private String payType;
	private Date endTime;
	private String chargeState;
	private String isVisible = "1";
	private String reserve1;
	private String reserve2;
	private String reserve3;
	private String reserve4;
	private String mode;	//负载均衡模式  0为普通模式  1为主备模式
	
	
	@Id
	@Column(name = "pool_id", unique = true, nullable = false, length = 100)
	public String getPoolId() {
		return this.poolId;
	}

	public void setPoolId(String poolId) {
		this.poolId = poolId;
	}

	@Column(name = "pool_name", length = 100)
	public String getPoolName() {
		return this.poolName;
	}

	public void setPoolName(String poolName) {
		this.poolName = poolName;
	}

	@Column(name = "prj_id", length = 100)
	public String getPrjId() {
		return this.prjId;
	}

	public void setPrjId(String prjId) {
		this.prjId = prjId;
	}

	@Column(name = "dc_id", length = 100)
	public String getDcId() {
		return this.dcId;
	}

	public void setDcId(String dcId) {
		this.dcId = dcId;
	}

	@Column(name = "pool_description", length = 100)
	public String getPoolDescription() {
		return this.poolDescription;
	}

	public void setPoolDescription(String poolDescription) {
		this.poolDescription = poolDescription;
	}

	@Column(name = "pool_provider")
	public String getPoolProvider() {
		return this.poolProvider;
	}

	public void setPoolProvider(String poolProvider) {
		this.poolProvider = poolProvider;
	}

	@Column(name = "subnet_id", length = 100)
	public String getSubnetId() {
		return this.subnetId;
	}

	public void setSubnetId(String subnetId) {
		this.subnetId = subnetId;
	}

	@Column(name = "vip_id", length = 100)
	public String getVipId() {
		return this.vipId;
	}

	public void setVipId(String vipId) {
		this.vipId = vipId;
	}

	@Column(name = "pool_protocol", length = 32)
	public String getPoolProtocol() {
		return this.poolProtocol;
	}

	public void setPoolProtocol(String poolProtocol) {
		this.poolProtocol = poolProtocol;
	}

	@Column(name = "lb_method", length = 32)
	public String getLbMethod() {
		return this.lbMethod;
	}

	public void setLbMethod(String lbMethod) {
		this.lbMethod = lbMethod;
	}

	@Column(name = "pool_status", length = 16)
	public String getPoolStatus() {
		return this.poolStatus;
	}

	public void setPoolStatus(String poolStatus) {
		this.poolStatus = poolStatus;
	}

	@Column(name = "admin_stateup", length = 1)
	public Character getAdminStateup() {
		return this.adminStateup;
	}

	public void setAdminStateup(Character adminStateup) {
		this.adminStateup = adminStateup;
	}

	@Column(name = "create_name", length = 100)
	public String getCreateName() {
		return this.createName;
	}

	public void setCreateName(String createName) {
		this.createName = createName;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "create_time", length = 19)
	public Date getCreateTime() {
		return this.createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@Column(name = "pay_type", length = 1)
	public String getPayType() {
		return payType;
	}

	public void setPayType(String payType) {
		this.payType = payType;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "end_time", length = 19)
	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	@Column(name = "charge_state", length = 1)
	public String getChargeState() {
		return chargeState;
	}

	public void setChargeState(String chargeState) {
		this.chargeState = chargeState;
	}

	@Column(name = "reserve1", length = 100)
	public String getReserve1() {
		return this.reserve1;
	}

	public void setReserve1(String reserve1) {
		this.reserve1 = reserve1;
	}

	@Column(name = "reserve2", length = 100)
	public String getReserve2() {
		return this.reserve2;
	}

	public void setReserve2(String reserve2) {
		this.reserve2 = reserve2;
	}

	@Column(name = "reserve3", length = 100)
	public String getReserve3() {
		return this.reserve3;
	}

	public void setReserve3(String reserve3) {
		this.reserve3 = reserve3;
	}

	@Column(name = "reserve4", length = 100)
	public String getReserve4() {
		return this.reserve4;
	}

	public void setReserve4(String reserve4) {
		this.reserve4 = reserve4;
	}
	
	@Column(name = "is_visible", length = 1)
	public String getIsVisible() {
        return isVisible;
    }

    public void setIsVisible(String isVisible) {
        this.isVisible = isVisible;
    }
    @Column(name = "mode", length = 1)
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
    public BaseCloudLdPool (){}
	
	public BaseCloudLdPool(Pool pool,String dcId){
		if(null!=pool){
			this.poolId = pool.getId();
			this.poolName = pool.getName();
			this.prjId = pool.getTenant_id();
			this.dcId = dcId;
			this.poolDescription = pool.getDescription();
			this.poolProvider = pool.getProvider();
			this.subnetId = pool.getSubnet_id();
			this.vipId = pool.getVip_id();
			this.poolProtocol = pool.getProtocol();
			this.lbMethod = pool.getLb_method();
			this.poolStatus = pool.getStatus()!=null?pool.getStatus().toUpperCase():"";
			this.adminStateup = (pool.isAdmin_state_up())?'1':'0';
		}
	}
}

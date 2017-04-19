package com.eayun.virtualization.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.StringUtils;

import com.eayun.eayunstack.model.VIP;
@Entity
@Table(name = "cloud_ldvip")
public class BaseCloudLdVip implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7722530692952773865L;
	private String vipId;
	private String vipName;
	private String subnetId;
	private String poolId;
	private String prjId;
	private String dcId;
	private String createName;
	private Long protocolPort;
	private String vipProtocol;
	private String vipStatus;
	private Long connectionLimit;
	private Character adminStateup;
	private Date createTime;
	private String vipAddress;
	private String reserve1;
	private String reserve2;
	private String reserve3;
	private String portId;

	
	
	@Id
	@Column(name = "vip_id", unique = true, nullable = false, length = 100)
	public String getVipId() {
		return this.vipId;
	}

	public void setVipId(String vipId) {
		this.vipId = vipId;
	}

	@Column(name = "vip_name", length = 100)
	public String getVipName() {
		return this.vipName;
	}

	public void setVipName(String vipName) {
		this.vipName = vipName;
	}

	@Column(name = "subnet_id", length = 100)
	public String getSubnetId() {
		return this.subnetId;
	}

	public void setSubnetId(String subnetId) {
		this.subnetId = subnetId;
	}

	@Column(name = "pool_id", length = 100)
	public String getPoolId() {
		return this.poolId;
	}

	public void setPoolId(String poolId) {
		this.poolId = poolId;
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

	@Column(name = "create_name", length = 100)
	public String getCreateName() {
		return this.createName;
	}

	public void setCreateName(String createName) {
		this.createName = createName;
	}

	@Column(name = "protocol_port", precision = 11, scale = 0)
	public Long getProtocolPort() {
		return this.protocolPort;
	}

	public void setProtocolPort(Long protocolPort) {
		this.protocolPort = protocolPort;
	}

	@Column(name = "vip_protocol", length = 16)
	public String getVipProtocol() {
		return this.vipProtocol;
	}

	public void setVipProtocol(String vipProtocol) {
		this.vipProtocol = vipProtocol;
	}

	@Column(name = "vip_status", length = 16)
	public String getVipStatus() {
		return this.vipStatus;
	}

	public void setVipStatus(String vipStatus) {
		this.vipStatus = vipStatus;
	}

	@Column(name = "connection_limit", precision = 11, scale = 0)
	public Long getConnectionLimit() {
		return this.connectionLimit;
	}

	public void setConnectionLimit(Long connectionLimit) {
		this.connectionLimit = connectionLimit;
	}

	@Column(name = "admin_stateup", length = 1)
	public Character getAdminStateup() {
		return this.adminStateup;
	}

	public void setAdminStateup(Character adminStateup) {
		this.adminStateup = adminStateup;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "create_time", length = 19)
	public Date getCreateTime() {
		return this.createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@Column(name = "vip_address", length = 64)
	public String getVipAddress() {
		return this.vipAddress;
	}

	public void setVipAddress(String vipAddress) {
		this.vipAddress = vipAddress;
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

	@Column(name = "port_id", length = 100)
	public String getPortId() {
		return portId;
	}

	public void setPortId(String portId) {
		this.portId = portId;
	}

	public BaseCloudLdVip (){}
	public BaseCloudLdVip(VIP vip,String  dcId){
		if(null!=vip){
			this.vipId = vip.getId();
			this.vipName = vip.getName();
			this.subnetId = vip.getSubnet_id();
			this.poolId = vip.getPool_id();
			this.prjId = vip.getTenant_id();
			this.dcId = dcId;
			this.vipProtocol = vip.getProtocol();
			this.vipStatus = vip.getStatus()!=null?vip.getStatus().toUpperCase():"";
			this.vipAddress = vip.getAddress();
			this.adminStateup = (vip.getAdmin_state_up())?'1':'0';
			if(!StringUtils.isEmpty(vip.getProtocol_port()))
				this.protocolPort = Long.parseLong(vip.getProtocol_port());
			if(!StringUtils.isEmpty(vip.getProtocol_port()))
				this.connectionLimit = Long.parseLong(vip.getConnection_limit());
			if(!StringUtils.isEmpty(vip.getPort_id())){
				this.portId = vip.getPort_id();
			}
		}
	}
}

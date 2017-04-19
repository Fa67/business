package com.eayun.database.instance.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * 云数据库实例
 *                       
 * @Filename: BaseRDSInstance.java
 * @Description: 
 * @Version: 1.0
 * @Author: LiuZhuangzhuang
 * @Email: zhuangzhuang.liu@eayun.com
 * @History:<br>
 *<li>Date: 2017年2月21日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Entity
@Table(name = "cloud_rdsinstance")
public class BaseCloudRDSInstance implements java.io.Serializable{

	private static final long serialVersionUID = -2143043538787113147L;
	
	@Id
	@Column(name = "rds_id", unique = true, nullable = false, length = 100)
	private String rdsId;   	//主键ID
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "create_time", length = 19)
	private Date createTime;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "end_time", length = 19)
	private Date endTime;
	
	@Column(name = "create_name", length = 50)
	private String createName;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "delete_time", length = 19)
	private Date deleteTime;
	
	@Column(name = "delete_user", length = 50)
	private String deleteUser;
	
	@Column(name = "dc_id", length = 100)
	private String dcId;                // 数据中心ID
	
	@Column(name = "prj_id", length = 100)
	private String prjId;             // 项目ID
	
	@Column(name = "pay_type", length = 1)
	private String payType;            // 计费类型
	
	@Column(name = "charge_state", length = 1)
	private String chargeState;        // 计费状态
	
	@Column(name = "is_visible", length = 100)
	private String isVisible;          // 标志是否显示
	
	@Column(name = "rds_name", length = 100)
	private String rdsName;            // 云数据库实例名称
	
	@Column(name = "rds_description", length = 1000)
	private String rdsDescription;     // 云数据库实例描述
	
	@Column(name = "rds_status", length = 50)
	private String rdsStatus;          // 云数据库实例状态 
	
	@Column(name = "is_master", length = 1)
	private String isMaster;          // 是否为主库
	
	@Column(name = "master_id", length = 100)
	private String masterId;          // 主库ID
	
	@Column(name = "net_id", length = 100)
	private String netId;          // 私有网络ID
	
	@Column(name = "subnet_id", length = 100)
	private String subnetId;          // 受管子网ID
	
	@Column(name = "rds_ip", length = 150)
	private String rdsIp;          // 云数据库IP
	
	@Column(name = "port_id", length = 100)
	private String portId;          // 端口ID
	
	@Column(name = "version_id", length = 100)
	private String versionId;          // 版本ID
	
	@Column(name = "config_id", length = 100)
	private String configId;          // 云数据库实例使用的配置ID
	
	@Column(name = "flavor_id", length = 100)
	private String flavorId;          // 云数据库实例规格ID
	
	@Column(name = "volume_size")
	private int volumeSize;          // 存储容量
	
	@Column(name = "volume_type", length = 100)
	private String volumeType;          // 存储类型
	
	@Column(name = "is_deleted", length = 1)
	private String isDeleted;          // 是否删除
	
	@Column(name = "vm_id", length = 100)
	private String vmId;          // 云数据库实例对应的云主机ID
	@Column(name = "log_publishing", length = 20)
	private String logPublishing;          			// RDS 日志发布的状态   DBLog  SlowLog ErrorLog All 

	public String getRdsId() {
		return rdsId;
	}

	public void setRdsId(String rdsId) {
		this.rdsId = rdsId;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getCreateName() {
		return createName;
	}

	public void setCreateName(String createName) {
		this.createName = createName;
	}

	public Date getDeleteTime() {
		return deleteTime;
	}

	public void setDeleteTime(Date deleteTime) {
		this.deleteTime = deleteTime;
	}

	public String getDeleteUser() {
		return deleteUser;
	}

	public void setDeleteUser(String deleteUser) {
		this.deleteUser = deleteUser;
	}

	public String getDcId() {
		return dcId;
	}

	public void setDcId(String dcId) {
		this.dcId = dcId;
	}

	public String getPrjId() {
		return prjId;
	}

	public void setPrjId(String prjId) {
		this.prjId = prjId;
	}

	public String getPayType() {
		return payType;
	}

	public void setPayType(String payType) {
		this.payType = payType;
	}

	public String getChargeState() {
		return chargeState;
	}

	public void setChargeState(String chargeState) {
		this.chargeState = chargeState;
	}

	public String getIsVisible() {
		return isVisible;
	}

	public void setIsVisible(String isVisible) {
		this.isVisible = isVisible;
	}

	public String getRdsName() {
		return rdsName;
	}

	public void setRdsName(String rdsName) {
		this.rdsName = rdsName;
	}

	public String getRdsDescription() {
		return rdsDescription;
	}

	public void setRdsDescription(String rdsDescription) {
		this.rdsDescription = rdsDescription;
	}

	public String getRdsStatus() {
		return rdsStatus;
	}

	public void setRdsStatus(String rdsStatus) {
		this.rdsStatus = rdsStatus;
	}

	public String getIsMaster() {
		return isMaster;
	}

	public void setIsMaster(String isMaster) {
		this.isMaster = isMaster;
	}

	public String getMasterId() {
		return masterId;
	}

	public void setMasterId(String masterId) {
		this.masterId = masterId;
	}

	public String getNetId() {
		return netId;
	}

	public void setNetId(String netId) {
		this.netId = netId;
	}

	public String getSubnetId() {
		return subnetId;
	}

	public void setSubnetId(String subnetId) {
		this.subnetId = subnetId;
	}

	public String getRdsIp() {
		return rdsIp;
	}

	public void setRdsIp(String rdsIp) {
		this.rdsIp = rdsIp;
	}

	public String getPortId() {
		return portId;
	}

	public void setPortId(String portId) {
		this.portId = portId;
	}

	public String getVersionId() {
		return versionId;
	}

	public void setVersionId(String versionId) {
		this.versionId = versionId;
	}

	public String getConfigId() {
		return configId;
	}

	public void setConfigId(String configId) {
		this.configId = configId;
	}

	public String getFlavorId() {
		return flavorId;
	}

	public void setFlavorId(String flavorId) {
		this.flavorId = flavorId;
	}

	public int getVolumeSize() {
		return volumeSize;
	}

	public void setVolumeSize(int volumeSize) {
		this.volumeSize = volumeSize;
	}

	public String getVolumeType() {
		return volumeType;
	}

	public void setVolumeType(String volumeType) {
		this.volumeType = volumeType;
	}

	public String getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(String isDeleted) {
		this.isDeleted = isDeleted;
	}

	public String getVmId() {
		return vmId;
	}

	public void setVmId(String vmId) {
		this.vmId = vmId;
	}

	public String getLogPublishing() {
		return logPublishing;
	}

	public void setLogPublishing(String logPublishing) {
		this.logPublishing = logPublishing;
	}
	
	
}

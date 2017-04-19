package com.eayun.virtualization.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.eayun.eayunstack.model.Vm;
/**
 * 云主机     
 * @Filename: BaseCloudVm.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2015年11月11日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Entity
@Table(name = "cloud_vm")
public class BaseCloudVm implements java.io.Serializable {

	private static final long serialVersionUID = -11167651198987706L;

	private String vmId;           //主键id
	private String osType;         //操作系统
	private Date endTime;          //到期时间
	private String createName;     //创建人姓名
	private String dcId;           //数据中心id
	private String prjId;          //项目id
	private String sysType;        //（操作）系统类型
	private Date createTime;       //创建时间
	private String vmName;         //云主机名称
	private String vmStatus;       //云主机状态
	private String fromImageId;    //镜像id
	private String fromVolumeId;   //云硬盘id
	private String flavorId;       //云主机类型id
	private String netId;          //网络id
	private String vmIp;           //受管IP地址
	private String hostId;         //计算节点id
	private String hostName;       //计算节点名称
	private String vmDescripstion; //云主机描述
	private String vmFrom;         //云主机创建自（如全局镜像、自定义镜像、系统盘...）
	private String isDeleted;      //是否删除  0 未删除   1 已删除   2 回收站
	private Date deleteTime;       //删除时间
	private String deleteUser;     //删除人姓名
	private String resizeId;
	private String subnetId;	   //受管子网Id
	private String selfSubnetId;   //自管子网
	private String selfIp ;        //自管IP
	private String chargeState;    //计费状态 0 正常  1 余额不足  2 已到期  3已到期(停服务)
	private String payType;        //计费模式  1  包年包月   2 按需计费
	private String isVisable;      //0 不显示   1显示
	private String portId;		   //受管子网对应的portID
	private String selfPortId;	   //自管子网对应的portID
	// Property accessors
	@Id
	@Column(name = "vm_id", unique = true, nullable = false, length = 100)
	public String getVmId() {
		return vmId;
	}

	public void setVmId(String vmId) {
		this.vmId = vmId;
	}

	@Column(name = "os_type", length = 50)
	public String getOsType() {
		return osType;
	}

	public void setOsType(String osType) {
		this.osType = osType;
	}

	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "end_time", length = 19)
	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	@Column(name = "create_name", length = 100)
	public String getCreateName() {
		return createName;
	}

	public void setCreateName(String createName) {
		this.createName = createName;
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

	@Column(name = "sys_type", length = 50)
	public String getSysType() {
		return sysType;
	}

	public void setSysType(String sysType) {
		this.sysType = sysType;
	}

	
	// @DateTimeFormat(pattern="yyyy-MM-dd hh:mm:ss")
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "create_time", length = 19)
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@Column(name = "vm_name", length = 100)
	public String getVmName() {
		return vmName;
	}

	public void setVmName(String vmName) {
		this.vmName = vmName;
	}

	@Column(name = "vm_status", length = 50)
	public String getVmStatus() {
		return vmStatus;
	}

	public void setVmStatus(String vmStatus) {
		this.vmStatus = vmStatus;
	}

	@Column(name = "from_imageid", length = 100)
	public String getFromImageId() {
		return fromImageId;
	}

	public void setFromImageId(String fromImageId) {
		this.fromImageId = fromImageId;
	}

	@Column(name = "from_volumeid", length = 100)
	public String getFromVolumeId() {
		return fromVolumeId;
	}

	public void setFromVolumeId(String fromVolumeId) {
		this.fromVolumeId = fromVolumeId;
	}

	@Column(name = "flavor_id", length = 100)
	public String getFlavorId() {
		return flavorId;
	}

	public void setFlavorId(String flavorId) {
		this.flavorId = flavorId;
	}

	@Column(name = "net_id", length = 100)
	public String getNetId() {
		return netId;
	}

	public void setNetId(String netId) {
		this.netId = netId;
	}

	@Column(name = "vm_ip", length = 150)
	public String getVmIp() {
		return vmIp;
	}

	public void setVmIp(String vmIp) {
		this.vmIp = vmIp;
	}

	@Column(name = "host_id", length = 100)
	public String getHostId() {
		return hostId;
	}

	public void setHostId(String hostId) {
		this.hostId = hostId;
	}

	@Column(name = "host_name", length = 100)
	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	@Column(name = "vm_description", length = 1000)
	public String getVmDescripstion() {
		return vmDescripstion;
	}

	public void setVmDescripstion(String vmDescripstion) {
		this.vmDescripstion = vmDescripstion;
	}

	@Column(name = "vm_from", length = 50)
	public String getVmFrom() {
		return vmFrom;
	}

	public void setVmFrom(String vmFrom) {
		this.vmFrom = vmFrom;
	}
	
	
	@Column(name = "is_deleted", length = 1)
	public String getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(String isDeleted) {
		this.isDeleted = isDeleted;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "delete_time", length = 19)
	public Date getDeleteTime() {
		return deleteTime;
	}

	public void setDeleteTime(Date deleteTime) {
		this.deleteTime = deleteTime;
	}
	
	@Column(name = "delete_user", length = 50)
	public String getDeleteUser() {
		return deleteUser;
	}

	public void setDeleteUser(String deleteUser) {
		this.deleteUser = deleteUser;
	}
	
	@Column(name = "resize_id", length = 100)
	public String getResizeId() {
		return resizeId;
	}

	public void setResizeId(String resizeId) {
		this.resizeId = resizeId;
	}
	@Column(name = "subnet_id", length = 100)
	public String getSubnetId() {
		return subnetId;
	}
	
	public void setSubnetId(String subnetId) {
		this.subnetId = subnetId;
	}
	
	@Column(name = "port_id", length = 100)
	public String getPortId() {
		return portId;
	}
	
	@Column(name = "self_portid", length = 100)
	public String getSelfPortId() {
		return selfPortId;
	}
	
	public void setPortId(String portId) {
		this.portId = portId;
	}

	public void setSelfPortId(String selfPortId) {
		this.selfPortId = selfPortId;
	}

	public BaseCloudVm(String vmId, String osType, Date endTime, String createName,
			String dcId, String prjId, String sysType, Date createTime,
			String vmName, String vmStatus, String fromImageId,
			String fromVolumeId, String flavorId, String netId, String vmIp,
			String hostId, String hostName, String vmDescripstion,
			String vmFrom, String isDeleted, Date deleteTime, String deleteUser) {
		super();
		this.vmId = vmId;
		this.osType = osType;
		this.endTime = endTime;
		this.createName = createName;
		this.dcId = dcId;
		this.prjId = prjId;
		this.sysType = sysType;
		this.createTime = createTime;
		this.vmName = vmName;
		this.vmStatus = vmStatus;
		this.fromImageId = fromImageId;
		this.fromVolumeId = fromVolumeId;
		this.flavorId = flavorId;
		this.netId = netId;
		this.vmIp = vmIp;
		this.hostId = hostId;
		this.hostName = hostName;
		this.vmDescripstion = vmDescripstion;
		this.vmFrom = vmFrom;
		this.isDeleted = isDeleted;
		this.deleteTime = deleteTime;
		this.deleteUser = deleteUser;
	}

	public BaseCloudVm() {
		super();
	}

	public BaseCloudVm(String vmId) {
		super();
		this.vmId = vmId;
	}

	public BaseCloudVm(String vmId, String vmStatus) {
		super();
		this.vmId = vmId;
		this.vmStatus = vmStatus;
	}
	
	@Column(name = "self_subnetid", length = 100)
	public String getSelfSubnetId() {
		return selfSubnetId;
	}

	public void setSelfSubnetId(String selfSubnetId) {
		this.selfSubnetId = selfSubnetId;
	}
	
	@Column(name = "self_ip", length = 100)
	public String getSelfIp() {
		return selfIp;
	}

	public void setSelfIp(String selfIp) {
		this.selfIp = selfIp;
	}
	
	@Column(name = "charge_state", length = 1)
	public String getChargeState() {
		return chargeState;
	}

	public void setChargeState(String chargeState) {
		this.chargeState = chargeState;
	}
	@Column(name = "pay_type", length = 1)
	public String getPayType() {
		return payType;
	}

	public void setPayType(String payType) {
		this.payType = payType;
	}
	@Column(name = "is_visable", length = 1)
	public String getIsVisable() {
		return isVisable;
	}

	public void setIsVisable(String isVisable) {
		this.isVisable = isVisable;
	}

	public BaseCloudVm(Vm vm, String dcId) throws Exception {
		if (null != vm) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			this.dcId = dcId;
			this.prjId = vm.getTenant_id();
			this.createTime = sdf.parse(vm.getCreated().replace("T", " ")
					.replace("Z", ""));
			this.vmName = vm.getName();
			this.vmStatus = vm.getStatus();
			if(null!=vm.getFlavor()){
				this.flavorId = vm.getFlavor().getId();
			}
//			this.vmIp = vm.getAddresses().toJSONString();
			this.hostId = vm.getHostId();
			this.hostName = vm.getHypervisor_hostname();
			this.vmId = vm.getId();
		}
	}

}

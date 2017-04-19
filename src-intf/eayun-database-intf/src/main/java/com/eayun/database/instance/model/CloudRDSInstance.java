package com.eayun.database.instance.model;

import java.util.Date;
import java.util.List;

public class CloudRDSInstance extends BaseCloudRDSInstance{

	private static final long serialVersionUID = -8314734794308850649L;
	
	private String type;           // 数据库类型(eg:mysql)
	private String version;        // 数据库类型对应的数据库版本(eg:5.5)
	private String backupId;       // 备份ID
	private String torvePrjId;     // 云数据库实例所在的云主机所属的租户ID
	private String troveSecurityGroupId; // 云数据库实例所在的云主机的默认安全组
	private String orderNo;        // 订单编号
	private String cusId;          // 客户ID
	private String cusOrg;         // 客户名称
	private int cpu;            // cpu
	private int ram;            // ram
	private int count;          // 状态同步次数
	private String prjName;     // 项目名称
	private String dcName;      // 数据中心名称
	private String payTypeStr;  // 计费类型
	private String statusStr;   // 实例状态
	private String volumeTypeName;  // 硬盘类型名称
	private String netName;         // 网络名称
	private String subnetName;      // 子网名称
	private int slaveCount;      // 从库数量
	private int cycleCount;	    //剩余天数(升级)
	private String resizeType;  // 0:升级云主机规格  1：升级规格成功后升级数据盘大小 2：只升级数据盘大小  3：升级数据盘失败后回滚规格（视为对规格的再升级）
	private String oldFlavorId;  // 升级之前的flavorID
	private Date opDate;         // 升级订单完成时间，用户计费
	private String isNeedAttach;  // 0:正常重启操作；1：解绑后的重启操作；2：绑定后的重启操作
	private String configName;    // 配置文件名称
	private String subnetCidr;         // 创建从库时需要使用主库的cidr(用于展示)
	private String masterName;      // 主库名称
	private String password;    // 实例root用户对应的密码
	private List children;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getBackupId() {
		return backupId;
	}
	public void setBackupId(String backupId) {
		this.backupId = backupId;
	}
	public String getTorvePrjId() {
		return torvePrjId;
	}
	public void setTorvePrjId(String torvePrjId) {
		this.torvePrjId = torvePrjId;
	}
	public String getTroveSecurityGroupId() {
		return troveSecurityGroupId;
	}
	public void setTroveSecurityGroupId(String troveSecurityGroupId) {
		this.troveSecurityGroupId = troveSecurityGroupId;
	}
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
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
    public int getCpu() {
		return cpu;
	}
	public void setCpu(int cpu) {
		this.cpu = cpu;
	}
	public int getRam() {
		return ram;
	}
	public void setRam(int ram) {
		this.ram = ram;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public String getPrjName() {
		return prjName;
	}
	public void setPrjName(String prjName) {
		this.prjName = prjName;
	}
	public String getDcName() {
		return dcName;
	}
	public void setDcName(String dcName) {
		this.dcName = dcName;
	}
	public String getPayTypeStr() {
		return payTypeStr;
	}
	public void setPayTypeStr(String payTypeStr) {
		this.payTypeStr = payTypeStr;
	}
	public String getStatusStr() {
		return statusStr;
	}
	public void setStatusStr(String statusStr) {
		this.statusStr = statusStr;
	}
	public String getVolumeTypeName() {
		return volumeTypeName;
	}
	public void setVolumeTypeName(String volumeTypeName) {
		this.volumeTypeName = volumeTypeName;
	}
	public String getNetName() {
		return netName;
	}
	public void setNetName(String netName) {
		this.netName = netName;
	}
	public String getSubnetName() {
		return subnetName;
	}
	public void setSubnetName(String subnetName) {
		this.subnetName = subnetName;
	}
	public int getSlaveCount() {
		return slaveCount;
	}
	public void setSlaveCount(int slaveCount) {
		this.slaveCount = slaveCount;
	}
	public int getCycleCount() {
		return cycleCount;
	}
	public void setCycleCount(int cycleCount) {
		this.cycleCount = cycleCount;
	}
	public String getResizeType() {
		return resizeType;
	}
	public void setResizeType(String resizeType) {
		this.resizeType = resizeType;
	}
	public String getOldFlavorId() {
		return oldFlavorId;
	}
	public void setOldFlavorId(String oldFlavorId) {
		this.oldFlavorId = oldFlavorId;
	}
	public Date getOpDate() {
		return opDate;
	}
	public void setOpDate(Date opDate) {
		this.opDate = opDate;
	}
	public String getIsNeedAttach() {
		return isNeedAttach;
	}
	public void setIsNeedAttach(String isNeedAttach) {
		this.isNeedAttach = isNeedAttach;
	}
    public List getChildren() {
        return children;
    }
    public void setChildren(List children) {
        this.children = children;
    }
	public String getConfigName() {
		return configName;
	}
	public void setConfigName(String configName) {
		this.configName = configName;
	}
	public String getSubnetCidr() {
		return subnetCidr;
	}
	public void setSubnetCidr(String subnetCidr) {
		this.subnetCidr = subnetCidr;
	}
	public String getMasterName() {
		return masterName;
	}
	public void setMasterName(String masterName) {
		this.masterName = masterName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
}

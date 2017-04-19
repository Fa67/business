package com.eayun.database.instance.model;

import java.math.BigDecimal;
import java.util.Date;

public class CloudOrderRDSInstance extends BaseCloudOrderRDSInstance{

	private static final long serialVersionUID = -3804243526267311257L;

	private String dcName;                // 数据中心名称
	private String netName;               // 私有网络名称
	private String subnetName;                // 受管子网名称
	private Date endTime;                // 到期时间
	private Date orderCompleteDate;                // 订单完成名称
	private BigDecimal accountPayment;                // 账户余额付款
	private BigDecimal thirdPartPayment;                // 第三方支付金额
	private int rdsInstanceCpu;                        // 升级 -- 升级前的数据库实例的CPU -> 用于计费
	private int rdsInstanceRam;                        //　升级 -- 升级前的数据库实例的RAM -> 用于计费
	private int diskSize;                              // 升级 -- 升级前的数据库实例的存储容量
	private int cycleCount;						//预付费资源剩余天数 -> 用于计费
	private String subnetCidr;						//受管子网CIDR
	private String versionName;                 // eg:MySQL 5.5
	private String prodName;					//产品名称
	private String volumeTypeName;              // 云硬盘类型
	private String cycleType;
	
	public String getDcName() {
		return dcName;
	}
	public void setDcName(String dcName) {
		this.dcName = dcName;
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
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public Date getOrderCompleteDate() {
		return orderCompleteDate;
	}
	public void setOrderCompleteDate(Date orderCompleteDate) {
		this.orderCompleteDate = orderCompleteDate;
	}
	public BigDecimal getAccountPayment() {
		return accountPayment;
	}
	public void setAccountPayment(BigDecimal accountPayment) {
		this.accountPayment = accountPayment;
	}
	public BigDecimal getThirdPartPayment() {
		return thirdPartPayment;
	}
	public void setThirdPartPayment(BigDecimal thirdPartPayment) {
		this.thirdPartPayment = thirdPartPayment;
	}
	public int getRdsInstanceCpu() {
		return rdsInstanceCpu;
	}
	public void setRdsInstanceCpu(int rdsInstanceCpu) {
		this.rdsInstanceCpu = rdsInstanceCpu;
	}
	public int getRdsInstanceRam() {
		return rdsInstanceRam;
	}
	public void setRdsInstanceRam(int rdsInstanceRam) {
		this.rdsInstanceRam = rdsInstanceRam;
	}
	public int getCycleCount() {
		return cycleCount;
	}
	public void setCycleCount(int cycleCount) {
		this.cycleCount = cycleCount;
	}
	public String getSubnetCidr() {
		return subnetCidr;
	}
	public void setSubnetCidr(String subnetCidr) {
		this.subnetCidr = subnetCidr;
	}
	public String getVersionName() {
		return versionName;
	}
	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}
	public String getProdName() {
		return prodName;
	}
	public void setProdName(String prodName) {
		this.prodName = prodName;
	}
	public int getDiskSize() {
		return diskSize;
	}
	public void setDiskSize(int diskSize) {
		this.diskSize = diskSize;
	}
	public String getVolumeTypeName() {
		return volumeTypeName;
	}
	public void setVolumeTypeName(String volumeTypeName) {
		this.volumeTypeName = volumeTypeName;
	}
	public String getCycleType() {
		return cycleType;
	}
	public void setCycleType(String cycleType) {
		this.cycleType = cycleType;
	}
	
}

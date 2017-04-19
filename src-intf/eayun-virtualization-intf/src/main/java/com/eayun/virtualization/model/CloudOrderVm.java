package com.eayun.virtualization.model;

import java.math.BigDecimal;
import java.util.Date;

public class CloudOrderVm extends BaseCloudOrderVm {

	private static final long serialVersionUID = -8967340036350607836L;
	
	private String dcName;						//数据中心名称
	private String netName;						//私有网络名称
	private String subnetName;					//受管子网名称
	private String selfSubnetName;				//自管子网名称
	private String cidr;						//受管子网CIDR
	private String selfCidr;					//自管子网CIDR
	private String imageName;					//镜像名称
	private String vmFrom;						//镜像来源
	private String sgName;						//安全组名称
	private Date endTime;						//到期时间
	private Date orderCompleteDate;			    //订单完成时间
	private BigDecimal paymentAmount;			//产品总付款金额
	private BigDecimal accountPayment;			//账户余额支付款
	private BigDecimal thirdPartPayment;		//第三方支付额
	private int vmCpu;							//主机原来的CPU
	private int vmRam;							//主机原来的内存
	private String flavorId;					//云主机类型
	private String prodName;					//产品名称
	private String sysTypeEn;					//镜像计费别名
	private int cycleCount;						//预付费资源剩余天数
	private String cycleType;
	
	private String sysTypeAs;//云主机系统盘类型中文名称
	private String dataTypeAs;//云主机数据盘类型中文名称
	private String sysDiskType;//系统盘盘类型 1普通型 2性能型 3超高性能型
	private String dataDiskType;//数据盘类型 1普通型 2性能型 3超高性能型
	
	
	public String getDcName() {
		return dcName;
	}
	public String getNetName() {
		return netName;
	}
	public String getSubnetName() {
		return subnetName;
	}
	public String getSelfSubnetName() {
		return selfSubnetName;
	}
	public String getCidr() {
		return cidr;
	}
	public String getSelfCidr() {
		return selfCidr;
	}
	public String getImageName() {
		return imageName;
	}
	public void setDcName(String dcName) {
		this.dcName = dcName;
	}
	public void setNetName(String netName) {
		this.netName = netName;
	}
	public void setSubnetName(String subnetName) {
		this.subnetName = subnetName;
	}
	public void setSelfSubnetName(String selfSubnetName) {
		this.selfSubnetName = selfSubnetName;
	}
	public void setCidr(String cidr) {
		this.cidr = cidr;
	}
	public void setSelfCidr(String selfCidr) {
		this.selfCidr = selfCidr;
	}
	public void setImageName(String imageName) {
		this.imageName = imageName;
	}
	public String getVmFrom() {
		return vmFrom;
	}
	public void setVmFrom(String vmFrom) {
		this.vmFrom = vmFrom;
	}
	public String getSgName() {
		return sgName;
	}
	public void setSgName(String sgName) {
		this.sgName = sgName;
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
	public int getVmCpu() {
		return vmCpu;
	}
	public void setVmCpu(int vmCpu) {
		this.vmCpu = vmCpu;
	}
	public int getVmRam() {
		return vmRam;
	}
	public void setVmRam(int vmRam) {
		this.vmRam = vmRam;
	}
	public String getFlavorId() {
		return flavorId;
	}
	public void setFlavorId(String flavorId) {
		this.flavorId = flavorId;
	}
	public BigDecimal getPaymentAmount() {
		return paymentAmount;
	}
	public void setPaymentAmount(BigDecimal paymentAmount) {
		this.paymentAmount = paymentAmount;
	}
	public String getProdName() {
		return prodName;
	}
	public void setProdName(String prodName) {
		this.prodName = prodName;
	}
	public String getSysTypeEn() {
		return sysTypeEn;
	}
	public void setSysTypeEn(String sysTypeEn) {
		this.sysTypeEn = sysTypeEn;
	}
	public int getCycleCount() {
		return cycleCount;
	}
	public void setCycleCount(int cycleCount) {
		this.cycleCount = cycleCount;
	}
	public String getCycleType() {
		return cycleType;
	}
	public void setCycleType(String cycleType) {
		this.cycleType = cycleType;
	}
	public String getSysTypeAs() {
		return sysTypeAs;
	}
	public void setSysTypeAs(String sysTypeAs) {
		this.sysTypeAs = sysTypeAs;
	}
	public String getDataTypeAs() {
		return dataTypeAs;
	}
	public void setDataTypeAs(String dataTypeAs) {
		this.dataTypeAs = dataTypeAs;
	}
	public String getSysDiskType() {
		return sysDiskType;
	}
	public void setSysDiskType(String sysDiskType) {
		this.sysDiskType = sysDiskType;
	}
	public String getDataDiskType() {
		return dataDiskType;
	}
	public void setDataDiskType(String dataDiskType) {
		this.dataDiskType = dataDiskType;
	}
	
	
}

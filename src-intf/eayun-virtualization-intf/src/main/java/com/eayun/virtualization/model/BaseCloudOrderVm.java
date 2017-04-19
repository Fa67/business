package com.eayun.virtualization.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * 云主机订单
 * @author zhouhaitao
 *
 */
@Entity
@Table(name = "cloudorder_vm")
public class BaseCloudOrderVm implements java.io.Serializable {

	private static final long serialVersionUID = 4640889822217548419L;
	@Id
	@Column(name = "ordervm_id", unique = true, nullable = false, length = 100)
	private String ordervmId;   	//主键ID
	@Column(name = "vm_id", length = 100)
	private String vmId;			//云主机ID
	@Column(name = "order_no", length = 18)
	private String orderNo;			//订单编号
	@Column(name = "dc_id", length = 100)
	private String dcId;			//数据中心ID
	@Column(name = "prj_id", length = 100)
	private String prjId;			//项目ID
	@Column(name = "vm_name", length = 100)
	private String vmName;			//云主机名称
	@Column(name = "net_id", length = 100)
	private String netId;		   	//私有网络ID
	@Column(name = "subnet_id", length = 100)
	private String subnetId;		//受管子网ID
	@Column(name = "self_subnetid", length = 100)
	private String selfSubnetId;	//自管子网ID
	@Column(name = "count")
	private int count;				//批量创建主机数
	@Column(name = "os_type", length = 100)
	private String osType;			//操作系统
	@Column(name = "sys_type", length = 100)
	private String sysType;			//系统型号
	@Column(name = "cpu")
	private int cpu;				//CPU(核)
	@Column(name = "ram")
	private int ram;				//内存（GB）
	@Column(name = "disk")
	private int disk;				//磁盘（GB）
	@Column(name = "image_type", length = 1)
	private String imageType;		//镜像类型
	@Column(name = "image_id", length = 100)
	private String imageId;			//镜像ID
	@Column(name = "username", length = 100)
	private String username;		//用户名
	@Column(name = "password", length = 100)
	private String password;		//密码
	@Column(name = "sg_id", length = 100)
	private String sgId;			//安全组
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "create_orderdate", length = 19)
	private Date createOrderDate;	//订单创建日期
	@Column(name = "create_user", length = 100)
	private String createUser;		//创建人
	@Column(name = "cus_id", length = 100)
	private String cusId;			//创建客户ID
	@Column(name = "buy_cycle")
	private int buyCycle;			//购买周期（月）
	@Column(name = "price")
	private BigDecimal price;			//购买价格
	@Column(name = "buy_floatip", length = 1)
	private String buyFloatIp;		//是否购买公网IP
	@Column(name = "order_type", length = 1)
	private String orderType;		//订单类型
	@Column(name = "pay_type", length = 1)
	private String payType;			//支付类型
	@Column(name = "order_resources", length = 2000)
	private String orderResources;	//统一订单的其他资源ID
	@Column(name = "sys_typeid", length = 100)
	private String sysTypeId;//系统盘类型id
	@Column(name = "data_typeid", length = 100)
	private String dataTypeId;//数据盘类型id
	@Column(name = "data_disk")
	private int dataDisk;
	@Column(name = "login_type")
	private String loginType;		//登录方式（pwd、ssh密钥）
	@Column(name = "secret_key")
	private String secretKey;		//ssh密钥ID
	
	

	public String getOrdervmId() {
		return ordervmId;
	}
	public String getVmId() {
		return vmId;
	}
	public String getOrderNo() {
		return orderNo;
	}
	public String getDcId() {
		return dcId;
	}
	public String getPrjId() {
		return prjId;
	}
	public String getVmName() {
		return vmName;
	}
	public String getNetId() {
		return netId;
	}
	public String getSubnetId() {
		return subnetId;
	}
	public String getSelfSubnetId() {
		return selfSubnetId;
	}
	public int getCount() {
		return count;
	}
	public String getOsType() {
		return osType;
	}
	public String getSysType() {
		return sysType;
	}
	public int getCpu() {
		return cpu;
	}
	public int getRam() {
		return ram;
	}
	public int getDisk() {
		return disk;
	}
	public String getImageType() {
		return imageType;
	}
	public String getImageId() {
		return imageId;
	}
	public String getUsername() {
		return username;
	}
	public String getPassword() {
		return password;
	}
	public String getSgId() {
		return sgId;
	}
	public Date getCreateOrderDate() {
		return createOrderDate;
	}
	public String getCreateUser() {
		return createUser;
	}
	public String getCusId() {
		return cusId;
	}
	public int getBuyCycle() {
		return buyCycle;
	}
	public BigDecimal getPrice() {
		return price;
	}
	public String getBuyFloatIp() {
		return buyFloatIp;
	}
	public String getOrderType() {
		return orderType;
	}
	public String getPayType() {
		return payType;
	}
	public String getOrderResources() {
		return orderResources;
	}
	public void setOrdervmId(String ordervmId) {
		this.ordervmId = ordervmId;
	}
	public void setVmId(String vmId) {
		this.vmId = vmId;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	public void setDcId(String dcId) {
		this.dcId = dcId;
	}
	public void setPrjId(String prjId) {
		this.prjId = prjId;
	}
	public void setVmName(String vmName) {
		this.vmName = vmName;
	}
	public void setNetId(String netId) {
		this.netId = netId;
	}
	public void setSubnetId(String subnetId) {
		this.subnetId = subnetId;
	}
	public void setSelfSubnetId(String selfSubnetId) {
		this.selfSubnetId = selfSubnetId;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public void setOsType(String osType) {
		this.osType = osType;
	}
	public void setSysType(String sysType) {
		this.sysType = sysType;
	}
	public void setCpu(int cpu) {
		this.cpu = cpu;
	}
	public void setRam(int ram) {
		this.ram = ram;
	}
	public void setDisk(int disk) {
		this.disk = disk;
	}
	public void setImageType(String imageType) {
		this.imageType = imageType;
	}
	public void setImageId(String imageId) {
		this.imageId = imageId;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public void setSgId(String sgId) {
		this.sgId = sgId;
	}
	public void setCreateOrderDate(Date createOrderDate) {
		this.createOrderDate = createOrderDate;
	}
	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}
	public void setCusId(String cusId) {
		this.cusId = cusId;
	}
	public void setBuyCycle(int buyCycle) {
		this.buyCycle = buyCycle;
	}
	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	public void setBuyFloatIp(String buyFloatIp) {
		this.buyFloatIp = buyFloatIp;
	}
	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}
	public void setPayType(String payType) {
		this.payType = payType;
	}
	public void setOrderResources(String orderResources) {
		this.orderResources = orderResources;
	}
	public String getSysTypeId() {
		return sysTypeId;
	}
	public void setSysTypeId(String sysTypeId) {
		this.sysTypeId = sysTypeId;
	}
	public String getDataTypeId() {
		return dataTypeId;
	}
	public void setDataTypeId(String dataTypeId) {
		this.dataTypeId = dataTypeId;
	}
	public int getDataDisk() {
		return dataDisk;
	}
	public void setDataDisk(int dataDisk) {
		this.dataDisk = dataDisk;
	}
	public String getLoginType() {
		return loginType;
	}
	public void setLoginType(String loginType) {
		this.loginType = loginType;
	}
	public String getSecretKey() {
		return secretKey;
	}
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
	
}

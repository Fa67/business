package com.eayun.database.instance.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * 云数据库实例订单表
 *                       
 * @Filename: BaseCloudOrderRDSInstance.java
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
@Table(name = "cloudorder_rdsinstance")
public class BaseCloudOrderRDSInstance implements java.io.Serializable{

	private static final long serialVersionUID = 3986846359176239240L;

	@Id
	@Column(name = "orderrds_id", unique = true, nullable = false, length = 100)
	private String orderRdsId;   	//主键ID
	
	@Column(name = "rds_id", length = 100)
	private String rdsId;          // 云数据库实例ID
	
	@Column(name = "order_no", length = 18)
	private String orderNo;       // 订单编号
	
	@Column(name = "dc_id", length = 100)
	private String dcId;          // 数据中心ID
	
	@Column(name = "prj_id", length = 100)
	private String prjId;        // 项目ID
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "create_orderdate", length = 19)
	private Date createOrderDate;    //订单创建日期
	
	@Column(name = "create_user", length = 100)
	private String createUser;       //创建人
	
	@Column(name = "cus_id", length = 100)
	private String cusId;            //客户ID
	
	@Column(name = "buy_cycle")
	private int buyCycle;            // 购买周期
	
	@Column(name = "pay_type", length = 1)
	private String payType;            //计费类型（1-预付费；2-后付费）
	
	@Column(name = "price")
	private BigDecimal price;            //购买价格
	
	@Column(name = "order_type", length = 1)
	private String orderType;            //订单类型（0-新购；1-续费；2-升级）
	
	@Column(name = "net_id", length = 100)
	private String netId;          // 私有网络ID
	
	@Column(name = "subnet_id", length = 100)
	private String subnetId;          // 受管子网ID
	
	@Column(name = "version_id", length = 100)
	private String versionId;          // 版本ID
	
	@Column(name = "config_id", length = 100)
	private String configId;          // 云数据库实例使用的配置ID
	
	@Column(name = "cpu")
	private int cpu;            //CPU
	
	@Column(name = "ram")
	private int ram;            //内存
	
	@Column(name = "volume_size")
	private int volumeSize;          // 存储容量
	
	@Column(name = "volume_type", length = 100)
	private String volumeType;          // 存储类型
	
	@Column(name = "rds_name", length = 100)
	private String rdsName;          // 数据库实例名称
	
	@Column(name = "backup_id", length = 100)
	private String backupId;            //备份ID
	
	@Column(name = "master_id", length = 100)
	private String masterId;            //主库ID

	@Column(name = "is_master", length = 1)
	private String isMaster;    //是否为主库

	@Column(name = "password", length = 100)
	private String password;            //实例密码
	
	public String getOrderRdsId() {
		return orderRdsId;
	}

	public void setOrderRdsId(String orderRdsId) {
		this.orderRdsId = orderRdsId;
	}

	public String getRdsId() {
		return rdsId;
	}

	public void setRdsId(String rdsId) {
		this.rdsId = rdsId;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
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

	public Date getCreateOrderDate() {
		return createOrderDate;
	}

	public void setCreateOrderDate(Date createOrderDate) {
		this.createOrderDate = createOrderDate;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public String getCusId() {
		return cusId;
	}

	public void setCusId(String cusId) {
		this.cusId = cusId;
	}

	public int getBuyCycle() {
		return buyCycle;
	}

	public void setBuyCycle(int buyCycle) {
		this.buyCycle = buyCycle;
	}

	public String getPayType() {
		return payType;
	}

	public void setPayType(String payType) {
		this.payType = payType;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
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

	public int getCpu() {
		return cpu;
	}

	public void setCpu(int cpu) {
		this.cpu = cpu;
	}

	public int getVolumeSize() {
		return volumeSize;
	}

	public int getRam() {
		return ram;
	}

	public void setRam(int ram) {
		this.ram = ram;
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

	public String getBackupId() {
		return backupId;
	}

	public void setBackupId(String backupId) {
		this.backupId = backupId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getMasterId() {
		return masterId;
	}

	public void setMasterId(String masterId) {
		this.masterId = masterId;
	}

	public String getRdsName() {
		return rdsName;
	}

	public void setRdsName(String rdsName) {
		this.rdsName = rdsName;
	}

	public String getIsMaster() {
		return isMaster;
	}

	public void setIsMaster(String isMaster) {
		this.isMaster = isMaster;
	}
	
}

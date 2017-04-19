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
 * 云硬盘资源表
 * 
 * @Filename: BaseCloudVolumeOrder.java
 * @Description:
 * @Version: 1.0
 * @Author: chengxiaodong
 * @Email: xiaodong.cheng@eayun.com
 * @History:<br> <li>Date: 2016年08月02日</li> 
 * <li>Version: 1.0</li> 
 * <li>Content:create</li>
 */
@Entity
@Table(name = "cloudorder_volume")
public class BaseCloudOrderVolume implements java.io.Serializable {

	private static final long serialVersionUID = -1116765119289877206L;

	private String orderVolId;// id
	private String orderNo;// 订单编号
	private String volId;// 云硬盘id
	private String prjId;// 项目id
	private String dcId;// 数据中心id
	private String diskFrom;// 云硬盘来自
	private String orderType;// 订单类型 0购买1续费2扩容
	private String volTypeId;// 云硬盘类型id
	private String payType;// 1预付费2后付费
	private int buyCycle;// 购买时长
	private BigDecimal price;// 总额
	private String fromSnapId;// 备份id
	private int volSize;// 云硬盘大小
	private int volNumber;// 批量创建数量
	private String volName;// 云硬盘名称
	private String volDescription;// 云硬盘描述
	private String orderResources;// 创建成功的资源id
	private String createUser;// 创建人
	private Date createOrderDate;// 创建时间
	private String cusId;//客户id

	@Id
	@Column(name = "ordervol_id", unique = true, nullable = false, length = 32)
	public String getOrderVolId() {
		return orderVolId;
	}

	public void setOrderVolId(String orderVolId) {
		this.orderVolId = orderVolId;
	}

	@Column(name = "order_no", length = 19)
	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	@Column(name = "order_type", length = 1)
	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}
	
	@Column(name = "vol_typeid", length = 100)
	public String getVolTypeId() {
		return volTypeId;
	}

	public void setVolTypeId(String volTypeId) {
		this.volTypeId = volTypeId;
	}

	@Column(name = "buy_cycle", length = 10)
	public int getBuyCycle() {
		return buyCycle;
	}

	public void setBuyCycle(int buyCycle) {
		this.buyCycle = buyCycle;
	}

	@Column(name = "price", length = 16)
	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	@Column(name = "vol_number", length = 20)
	public int getVolNumber() {
		return volNumber;
	}

	public void setVolNumber(int volNumber) {
		this.volNumber = volNumber;
	}

	@Column(name = "order_resources", length = 2000)
	public String getOrderResources() {
		return orderResources;
	}

	public void setOrderResources(String orderResources) {
		this.orderResources = orderResources;
	}

	@Column(name = "create_user", length = 32)
	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "create_orderdate", length = 19)
	public Date getCreateOrderDate() {
		return createOrderDate;
	}

	public void setCreateOrderDate(Date createOrderDate) {
		this.createOrderDate = createOrderDate;
	}

	@Column(name = "vol_id", length = 100)
	public String getVolId() {
		return volId;
	}

	public void setVolId(String volId) {
		this.volId = volId;
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

	@Column(name = "disk_from", length = 50)
	public String getDiskFrom() {
		return diskFrom;
	}

	public void setDiskFrom(String diskFrom) {
		this.diskFrom = diskFrom;
	}

	@Column(name = "pay_type", length = 1)
	public String getPayType() {
		return payType;
	}

	public void setPayType(String payType) {
		this.payType = payType;
	}

	@Column(name = "from_snapid", length = 100)
	public String getFromSnapId() {
		return fromSnapId;
	}

	public void setFromSnapId(String fromSnapId) {
		this.fromSnapId = fromSnapId;
	}

	@Column(name = "vol_size", length = 20)
	public int getVolSize() {
		return volSize;
	}

	public void setVolSize(int volSize) {
		this.volSize = volSize;
	}

	@Column(name = "vol_name", length = 100)
	public String getVolName() {
		return volName;
	}

	public void setVolName(String volName) {
		this.volName = volName;
	}

	@Column(name = "vol_description", length = 1000)
	public String getVolDescription() {
		return volDescription;
	}

	public void setVolDescription(String volDescription) {
		this.volDescription = volDescription;
	}
	
	@Column(name = "cus_id", length = 100)
	public String getCusId() {
		return cusId;
	}

	public void setCusId(String cusId) {
		this.cusId = cusId;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}

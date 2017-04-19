package com.eayun.virtualization.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * 云硬盘备份资源表
 * 
 * @Filename: BaseCloudSnapshotOrder.java
 * @Description:
 * @Version: 1.0
 * @Author: chengxiaodong
 * @Email: xiaodong.cheng@eayun.com
 * @History:<br> <li>Date: 2016年08月02日</li> 
 * <li>Version: 1.0</li> 
 * <li>Content:create</li>
 */
@Entity
@Table(name = "cloudorder_snapshot")
public class BaseCloudOrderSnapshot implements java.io.Serializable {

	private static final long serialVersionUID = -1116765119289877206L;
	private String orderSnapId;// id
	private String orderNo;// 订单编号
	private String prjId;// 项目id
	private String dcId;// 数据中心id
	private String orderType;// 订单类型 0购买1续费2扩容
	private String volId;// 云硬盘id
	private String payType;// 1预付费2后付费
	private int snapSize;// 备份大小
	private String snapName;// 云硬盘备份名称
	private String snapDescription;// 云硬盘备份描述
	private String snapType;//备份类型
	private String orderResources;// 创建成功的资源id
	private String createUser;// 创建人
	private Date createOrderDate;// 创建时间
	
	
	@Id
	@Column(name = "ordersnap_id", unique = true, nullable = false, length = 100)
	public String getOrderSnapId() {
		return orderSnapId;
	}
	public void setOrderSnapId(String orderSnapId) {
		this.orderSnapId = orderSnapId;
	}
	@Column(name = "order_no", length = 19)
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
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
	@Column(name = "order_type", length = 1)
	public String getOrderType() {
		return orderType;
	}
	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}
	@Column(name = "vol_id", length = 100)
	public String getVolId() {
		return volId;
	}
	public void setVolId(String volId) {
		this.volId = volId;
	}
	@Column(name = "pay_type", length = 1)
	public String getPayType() {
		return payType;
	}
	public void setPayType(String payType) {
		this.payType = payType;
	}
	@Column(name = "snap_size", length = 20)
	public int getSnapSize() {
		return snapSize;
	}
	public void setSnapSize(int snapSize) {
		this.snapSize = snapSize;
	}
	@Column(name = "snap_name", length = 100)
	public String getSnapName() {
		return snapName;
	}
	public void setSnapName(String snapName) {
		this.snapName = snapName;
	}
	@Column(name = "snap_description", length = 1000)
	public String getSnapDescription() {
		return snapDescription;
	}
	public void setSnapDescription(String snapDescription) {
		this.snapDescription = snapDescription;
	}
	@Column(name = "snap_type", length = 1)
	public String getSnapType() {
		return snapType;
	}
	public void setSnapType(String snapType) {
		this.snapType = snapType;
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
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	

}

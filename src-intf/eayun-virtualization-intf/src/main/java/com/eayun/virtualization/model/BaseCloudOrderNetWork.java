package com.eayun.virtualization.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
/**
 * 私有网络订单
 * @Author: gaoxiang
 * @version: 1.0
 * @Email: xiang.gao@eayun.com
 * @History: <br>
 * <li>Date: 2016年08月02日</li>
 */
@Entity
@Table(name = "cloudorder_network")
public class BaseCloudOrderNetWork implements java.io.Serializable {

	
	/**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -7009509654409140902L;
    private String orderNetWorkId;
	private String orderNo;
	private String orderType;
	private int buyCycle;
	private String payType;
	private BigDecimal price;
	private String netId;
	private String netName;
	private int rate;
	private String createName;
	private Date createTime;
	private String prjId;
	private String dcId;
	private String cusId;
	
	@Id
	@GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name="ordernetwork_id", length=32)
	public String getOrderNetWorkId() {
		return orderNetWorkId;
	}
	public void setOrderNetWorkId(String orderNetWorkId) {
		this.orderNetWorkId = orderNetWorkId;
	}
	
	@Column(name="order_no", length=18)
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	
	@Column(name="order_type", length=1)
	public String getOrderType() {
		return orderType;
	}
	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}
	
	@Column(name="buy_cycle")
	public int getBuyCycle() {
		return buyCycle;
	}
	public void setBuyCycle(int buyCycle) {
		this.buyCycle = buyCycle;
	}
	
	@Column(name="pay_type", length=1)
	public String getPayType() {
		return payType;
	}
	public void setPayType(String payType) {
		this.payType = payType;
	}
	
	@Column(name="price", length=20)
	public BigDecimal getPrice() {
		return price;
	}
	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	
	@Column(name="net_id", length=100)
	public String getNetId() {
		return netId;
	}
	public void setNetId(String netId) {
		this.netId = netId;
	}
	
	@Column(name="net_name", length=100)
	public String getNetName() {
		return netName;
	}
	public void setNetName(String netName) {
		this.netName = netName;
	}
	
	@Column(name="rate", length=9)
	public int getRate() {
		return rate;
	}
	public void setRate(int rate) {
		this.rate = rate;
	}
	
	@Column(name="create_name", length=100)
	public String getCreateName() {
		return createName;
	}
	public void setCreateName(String createName) {
		this.createName = createName;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="create_time", length=19)
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	@Column(name="prj_id", length=100)
	public String getPrjId() {
		return prjId;
	}
	public void setPrjId(String prjId) {
		this.prjId = prjId;
	}
	
	@Column(name="dc_id", length=100)
	public String getDcId() {
		return dcId;
	}
	public void setDcId(String dcId) {
		this.dcId = dcId;
	}
	
	@Column(name="cus_id", length=32)
	public String getCusId() {
		return cusId;
	}
	public void setCusId(String cusId) {
		this.cusId = cusId;
	}
	
	
}

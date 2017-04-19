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

@Entity
@Table(name = "cloudorder_ldpool")
public class BaseCloudOrderLdPool implements java.io.Serializable {

	/**
     * 
     */
    private static final long serialVersionUID = 8052521936967810442L;
    private String orderPoolId;
	private String orderNo;
	private String orderType;
	private int buyCycle;
	private String payType;
	private BigDecimal price;
	private String poolId;
	private String poolName;
	private Long connectionLimit;
	private Long vipPort;
	private String vipId;
	private String dcId;
	private String prjId;
	private String poolDescription;
	private String poolProvider;
	private String subnetId;
	private String poolProtocol;
	private String lbMethod;
	private String createName;
	private Date createTime;
	private String cusId;
	private String mode;	//负载均衡模式  0为普通模式  1为主备模式
	
	@Id
	@GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name = "orderpool_id", length = 32)
	public String getOrderPoolId() {
		return orderPoolId;
	}
	public void setOrderPoolId(String orderPoolId) {
		this.orderPoolId = orderPoolId;
	}
	@Column(name = "order_no", length = 18)
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
	@Column(name = "buy_cycle")
	public int getBuyCycle() {
		return buyCycle;
	}
	public void setBuyCycle(int buyCycle) {
		this.buyCycle = buyCycle;
	}
	@Column(name = "pay_type", length = 1)
	public String getPayType() {
		return payType;
	}
	public void setPayType(String payType) {
		this.payType = payType;
	}
	@Column(name = "price", length = 20)
	public BigDecimal getPrice() {
		return price;
	}
	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	@Column(name = "pool_id", length = 100)
	public String getPoolId() {
		return poolId;
	}
	public void setPoolId(String poolId) {
		this.poolId = poolId;
	}
	@Column(name = "pool_name", length = 100)
	public String getPoolName() {
		return poolName;
	}
	public void setPoolName(String poolName) {
		this.poolName = poolName;
	}
	@Column(name = "connection_limit", precision = 11, scale = 0)
	public Long getConnectionLimit() {
		return connectionLimit;
	}
	public void setConnectionLimit(Long connectionLimit) {
		this.connectionLimit = connectionLimit;
	}
	@Column(name = "vip_port", precision = 11, scale = 0)
	public Long getVipPort() {
		return vipPort;
	}
	public void setVipPort(Long vipPort) {
		this.vipPort = vipPort;
	}
	@Column(name = "vip_id", length = 100)
	public String getVipId() {
	    return vipId;
	}
	public void setVipId(String vipId) {
	    this.vipId = vipId;
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
	@Column(name = "pool_description", length = 100)
	public String getPoolDescription() {
		return poolDescription;
	}
	public void setPoolDescription(String poolDescription) {
		this.poolDescription = poolDescription;
	}
	@Column(name = "pool_provider")
	public String getPoolProvider() {
		return poolProvider;
	}
	public void setPoolProvider(String poolProvider) {
		this.poolProvider = poolProvider;
	}
	@Column(name = "subnet_id", length = 100)
	public String getSubnetId() {
		return subnetId;
	}
	public void setSubnetId(String subnetId) {
		this.subnetId = subnetId;
	}
	@Column(name = "pool_protocol", length = 32)
	public String getPoolProtocol() {
		return poolProtocol;
	}
	public void setPoolProtocol(String poolProtocol) {
		this.poolProtocol = poolProtocol;
	}
	@Column(name = "ld_method", length = 32)
	public String getLbMethod() {
		return lbMethod;
	}
	public void setLbMethod(String lbMethod) {
		this.lbMethod = lbMethod;
	}
	@Column(name = "create_name", length = 100)
	public String getCreateName() {
		return createName;
	}
	public void setCreateName(String createName) {
		this.createName = createName;
	}
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "create_time", length = 19)
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	@Column(name = "cus_id", length = 32)
	public String getCusId() {
		return cusId;
	}
	public void setCusId(String cusId) {
		this.cusId = cusId;
	}
	@Column(name = "mode", length = 1)
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	
}

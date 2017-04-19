package com.eayun.order.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;


/**
 *                       
 * @Filename: BaseOrder.java
 * @Description: 订单实体映射表
 * @Version: 1.0
 * @Author: bo.zeng
 * @Email: bo.zeng@eayun.com
 * @History:<br>
 *<li>Date: 2016年7月27日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Entity
@Table(name = "order_info")
public class BaseOrder implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * 主键UUID
	 */
	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name = "order_id", unique = true, nullable = false, length = 32)
	private String orderId;
	
	/**
	 * 订单编号（订单标识号+日期+当前时间平台交易次数8位）
	 */
	@Column(name = "order_no", length = 18, updatable=false, nullable=false)
	private String orderNo;
	
	/**
	 * 订单类型（0-新购；1-续费；2-升级）
	 */
	@Column(name = "order_type", length = 1, updatable=false, nullable=false)
	private String orderType;
	
	/**
	 * 提交订单用户ID
	 */
	@Column(name = "user_id", length = 32, updatable=false, nullable=false)
	private String userId;
	
	/**
	 * 客户ID
	 */
	@Column(name = "cus_id", length = 32, updatable=false, nullable=false)
	private String cusId;
	
	/**
	 * 产品名称
	 */
	@Column(name = "prod_name", length = 200, updatable=false, nullable=false)
	private String prodName;
	
	/**
	 * 订单生成时间
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "create_time", updatable=false, nullable=false)
	private Date createTime;
	
	/**
	 * 订单完成时间
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "complete_time")
	private Date completeTime;
	
	/**
	 * 订单取消时间
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "canceled_time")
	private Date canceledTime;
	
	/**
	 * 审核通过时间（目前无审核流程，该字段暂时无用）
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "audit_pass_time", updatable=false)
	private Date auditPassTime;
	
	/**
	 * 数据中心ID
	 */
	@Column(name = "dc_id", length = 32, updatable=false, nullable=true)
	private String dcId;
	
	/**
	 * 产品数量
	 */
	@Column(name = "prod_count", updatable=false)
	private int prodCount;
	
	/**
	 * 产品配置
	 */
	@Column(name = "prod_config", length = 2000, updatable=false)
	private String prodConfig;
	
	/**
	 * 付款类型（1-预付费；2-后付费）
	 */
	/**
	 *Comment for <code>payType</code>
	 */
	@Column(name = "pay_type", length = 1, updatable=false)
	private String payType;
	
	/**
	 * 购买周期
	 */
	@Column(name = "buy_cycle", updatable=false)
	private int buyCycle;
	
	/**
	 * 支付过期时间（待支付状态24小时）
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "pay_expire_time", updatable=false)
	private Date payExpireTime;
	
	/**
	 * 单价
	 */
	@Column(name = "unit_price", updatable=false)
	private BigDecimal unitPrice = new BigDecimal(0.000);
	
	/**
	 * 审核标识（目前无审核流程，该字段暂时无用）
	 */
	@Column(name = "audit_flag", updatable=false)
	private String auditFlag;
	
	/**
	 * 计费周期（0-小时；1-天；2-其他）
	 */
	@Column(name = "billing_cycle", updatable=false)
	private String billingCycle;
	
	/**
	 * 订单状态（1-待支付；2-资源创建中；3-资源创建失败-已取消；4-已完成；5-已取消）
	 */
	@Column(name = "order_state", length = 1, nullable=false)
	private String orderState;
	
	/**
	 * 资源类型（0-云主机；1-云硬盘；2-云硬盘备份；3-私有网络；4-负载均衡；5-弹性公网IP；6-对象存储；7-VPN）
	 */
	@Column(name = "resource_type", length = 1, updatable=false, nullable=false)
	private String resourceType;
	
	/**
	 * 付费总金额
	 */
	@Column(name = "payment_amount", updatable=false)
	private BigDecimal paymentAmount = new BigDecimal(0.000);
	
	/**
	 * 账户支付金额
	 */
	@Column(name = "account_payment", updatable=false)
	private BigDecimal accountPayment = new BigDecimal(0.000);
	
	/**
	 * 第三方支付金额
	 */
	@Column(name = "third_part_payment", updatable=false)
	private BigDecimal thirdPartPayment = new BigDecimal(0.000);
	
	/**
	 * 备注
	 */
	@Column(name = "remark", length = 1000, updatable=false)
	private String remark;
	
	/**
	 * 第三方支付类型（0-支付宝）
	 */
	@Column(name = "third_part_type", length = 1, updatable=false)
	private String thirdPartType;
	
	/**
	 * 乐观锁标识字段
	 */
	@Column(name = "version", length = 1, nullable=false)
	@Version
	private int version;
	
	/**
	 * 业务参数JSON格式
	 */
	@Column(name = "params")
	private String params;
	
	/**
	 * 资源起始时间
	 */
	@Column(name = "resource_begin_time")
	private Date resourceBeginTime;
	
	/**
	 * 资源到期时间
	 */
	@Column(name = "resource_expire_time")
	private Date resourceExpireTime;

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getCusId() {
		return cusId;
	}

	public void setCusId(String cusId) {
		this.cusId = cusId;
	}

	public String getProdName() {
		return prodName;
	}

	public void setProdName(String prodName) {
		this.prodName = prodName;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getCompleteTime() {
		return completeTime;
	}

	public void setCompleteTime(Date completeTime) {
		this.completeTime = completeTime;
	}

	public Date getAuditPassTime() {
		return auditPassTime;
	}

	public void setAuditPassTime(Date auditPassTime) {
		this.auditPassTime = auditPassTime;
	}

	public String getDcId() {
		return dcId;
	}

	public void setDcId(String dcId) {
		this.dcId = dcId;
	}

	public int getProdCount() {
		return prodCount;
	}

	public void setProdCount(int prodCount) {
		this.prodCount = prodCount;
	}

	public String getProdConfig() {
		return prodConfig;
	}

	public void setProdConfig(String prodConfig) {
		this.prodConfig = prodConfig;
	}

	public String getPayType() {
		return payType;
	}

	public void setPayType(String payType) {
		this.payType = payType;
	}

	public int getBuyCycle() {
		return buyCycle;
	}

	public void setBuyCycle(int buyCycle) {
		this.buyCycle = buyCycle;
	}

	public Date getPayExpireTime() {
		return payExpireTime;
	}

	public void setPayExpireTime(Date payExpireTime) {
		this.payExpireTime = payExpireTime;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
	}

	public String getAuditFlag() {
		return auditFlag;
	}

	public void setAuditFlag(String auditFlag) {
		this.auditFlag = auditFlag;
	}

	public String getBillingCycle() {
		return billingCycle;
	}

	public void setBillingCycle(String billingCycle) {
		this.billingCycle = billingCycle;
	}

	public String getOrderState() {
		return orderState;
	}

	public void setOrderState(String orderState) {
		this.orderState = orderState;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public BigDecimal getPaymentAmount() {
		return paymentAmount;
	}

	public void setPaymentAmount(BigDecimal paymentAmount) {
		this.paymentAmount = paymentAmount;
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

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getThirdPartType() {
		return thirdPartType;
	}

	public void setThirdPartType(String thirdPartType) {
		this.thirdPartType = thirdPartType;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public Date getResourceBeginTime() {
		return resourceBeginTime;
	}

	public void setResourceBeginTime(Date resourceBeginTime) {
		this.resourceBeginTime = resourceBeginTime;
	}

	public Date getResourceExpireTime() {
		return resourceExpireTime;
	}

	public void setResourceExpireTime(Date resourceExpireTime) {
		this.resourceExpireTime = resourceExpireTime;
	}

	public Date getCanceledTime() {
		return canceledTime;
	}

	public void setCanceledTime(Date canceledTime) {
		this.canceledTime = canceledTime;
	}
	
}

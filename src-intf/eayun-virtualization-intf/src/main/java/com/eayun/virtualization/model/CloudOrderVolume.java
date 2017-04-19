package com.eayun.virtualization.model;

import java.math.BigDecimal;
import java.util.Date;

public class CloudOrderVolume extends BaseCloudOrderVolume{
	
	/**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -228148784931877985L;
    private String dcName;
	private Date endTime;//到期时间
	private Date orderCompleteDate;//订单完成时间
	private int volOldSize;//原来的大小
	private BigDecimal accountPayment;//账户支付金额
	private BigDecimal thirdPartPayment;//第三方支付金额
	private BigDecimal paymentAmount;//产品总付款金额
	private String prodName;//产品名称
	private int cycleCount;//预付费资源剩余天数
	private String cycleType;//按年按月
	private String snapName;//备份名称
	private String fromVolId;//是否从云硬盘详情页过来
	
	private String volumeTypeAs;//云硬盘类型中文名称
	private String volType;//云硬盘类型 1普通型 2性能型 3超高性能型

	public String getDcName() {
		return dcName;
	}

	public void setDcName(String dcName) {
		this.dcName = dcName;
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

	public int getVolOldSize() {
		return volOldSize;
	}

	public void setVolOldSize(int volOldSize) {
		this.volOldSize = volOldSize;
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

	public String getSnapName() {
		return snapName;
	}

	public void setSnapName(String snapName) {
		this.snapName = snapName;
	}

	public String getFromVolId() {
		return fromVolId;
	}

	public void setFromVolId(String fromVolId) {
		this.fromVolId = fromVolId;
	}

	public String getVolumeTypeAs() {
		return volumeTypeAs;
	}

	public void setVolumeTypeAs(String volumeTypeAs) {
		this.volumeTypeAs = volumeTypeAs;
	}

	public String getVolType() {
		return volType;
	}

	public void setVolType(String volType) {
		this.volType = volType;
	}
	
	
	
	

}

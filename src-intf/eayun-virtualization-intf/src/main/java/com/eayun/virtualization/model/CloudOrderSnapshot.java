package com.eayun.virtualization.model;

import java.math.BigDecimal;
import java.util.Date;

public class CloudOrderSnapshot extends BaseCloudOrderSnapshot {
	/**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -8740457615153581890L;
    private String dcName;//数据中心名称
	private String cusId;//客户id
	private String volName;//硬盘名称
	private Date orderCompleteDate;//订单完成时间
	private String prodName;//产品名称
	private BigDecimal paymentAmount;//产品总付款金额
	private String fromVolId;
	private String fromVmId;
	

	

	public String getDcName() {
		return dcName;
	}

	public void setDcName(String dcName) {
		this.dcName = dcName;
	}

	public String getCusId() {
		return cusId;
	}

	public void setCusId(String cusId) {
		this.cusId = cusId;
	}

	public String getVolName() {
		return volName;
	}

	public void setVolName(String volName) {
		this.volName = volName;
	}

	public Date getOrderCompleteDate() {
		return orderCompleteDate;
	}

	public void setOrderCompleteDate(Date orderCompleteDate) {
		this.orderCompleteDate = orderCompleteDate;
	}

	public String getProdName() {
		return prodName;
	}

	public void setProdName(String prodName) {
		this.prodName = prodName;
	}

	public BigDecimal getPaymentAmount() {
		return paymentAmount;
	}

	public void setPaymentAmount(BigDecimal paymentAmount) {
		this.paymentAmount = paymentAmount;
	}
	
	public String getFromVolId() {
		return fromVolId;
	}

	public void setFromVolId(String fromVolId) {
		this.fromVolId = fromVolId;
	}

	public String getFromVmId() {
		return fromVmId;
	}

	public void setFromVmId(String fromVmId) {
		this.fromVmId = fromVmId;
	}
	
	
	
	
	

}

package com.eayun.costcenter.bean;

import com.eayun.common.tools.ExcelTitle;

public class ExcelPrepaymentRecord {
	@ExcelTitle(name="计费时间")
    private String monTime;
	
	@ExcelTitle(name="产品")
    private String productName;
	
	@ExcelTitle(name="订单号")
    private String orderNo;
	
	@ExcelTitle(name="付款方式")
    private String payType;
	
	@ExcelTitle(name="应付金额")
    private String shouldPay;

	

	public String getMonTime() {
		return monTime;
	}

	public void setMonTime(String monTime) {
		this.monTime = monTime;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getPayType() {
		return payType;
	}

	public void setPayType(String payType) {
		this.payType = payType;
	}

	public String getShouldPay() {
		return shouldPay;
	}

	public void setShouldPay(String shouldPay) {
		this.shouldPay = shouldPay;
	}
	
	
}

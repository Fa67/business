package com.eayun.costcenter.bean;

import com.eayun.common.tools.ExcelTitle;

public class ExcelPostPayRecord {
	@ExcelTitle(name="计费时间")
    private String monTime;
	
	@ExcelTitle(name="账期")
    private String monMonth;
	
	@ExcelTitle(name="产品")
    private String productName;

	@ExcelTitle(name="资源id/名称")
    private String resource;
	
	@ExcelTitle(name="付款方式")
    private String payType;
	
	@ExcelTitle(name="应付金额")
    private String shouldPay;
	
	@ExcelTitle(name="支付状态")
    private String payState;

	public String getMonTime() {
		return monTime;
	}

	public void setMonTime(String monTime) {
		this.monTime = monTime;
	}

	public String getMonMonth() {
		return monMonth;
	}

	public void setMonMonth(String monMonth) {
		this.monMonth = monMonth;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
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

	public String getPayState() {
		return payState;
	}

	public void setPayState(String payState) {
		this.payState = payState;
	}
	
	
}

package com.eayun.customer.model;

public class CusBlockResourceVoe extends CusBlockResource{

	/**
	 *Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = -536730414341738107L;
	
	private String cusName;
	private String errorMsg;

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public String getCusName() {
		return cusName;
	}

	public void setCusName(String cusName) {
		this.cusName = cusName;
	}
	
	
}

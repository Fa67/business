package com.eayun.price.model;


public class BillingFactor extends BaseBillingFactor {

	/**
	 *Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1149529106219060143L;

	private String typeName;	//资源类型名称
	
	private String factorName;	//计费因子名称
	
	private String unitName;	//计费单位名称
	
	private String dcName;		//数据中心名称
	
	private String meterName;	//计量单位
	
	private String pricePay;	//计价方式
	
	private boolean haveShow;		//用于控制页面编辑删除按钮是否显示，无任何实际意义

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getFactorName() {
		return factorName;
	}

	public void setFactorName(String factorName) {
		this.factorName = factorName;
	}

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	public String getDcName() {
		return dcName;
	}

	public void setDcName(String dcName) {
		this.dcName = dcName;
	}

	public String getMeterName() {
		return meterName;
	}

	public void setMeterName(String meterName) {
		this.meterName = meterName;
	}

	public String getPricePay() {
		return pricePay;
	}

	public void setPricePay(String pricePay) {
		this.pricePay = pricePay;
	}

	public boolean getHaveShow() {
		return haveShow;
	}

	public void setHaveShow(boolean haveShow) {
		this.haveShow = haveShow;
	}


	
}

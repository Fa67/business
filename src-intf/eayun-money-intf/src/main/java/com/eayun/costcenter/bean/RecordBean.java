package com.eayun.costcenter.bean;

import java.math.BigDecimal;
import java.util.Date;

import com.eayun.price.bean.ParamBean;
import com.eayun.price.bean.PriceDetails;

public class RecordBean {
	private Date exchangeTime;
	private String ecscRemark;
	private String ecmcRemark;
	private BigDecimal exchangeMoney;
	private String productName;
	private String orderNo;
	private String resourceId;
	private String resourceName;
	private String payType;
	private String incomeType;
	private Date monStart;
	private Date monEnd;
	private String configure;
	private String cusId;
	private String resourceType;
	private String monContract;
	private String operType;
	private PriceDetails priceDetails;
	private ParamBean paramBean;
	private String imageName;
	private String dcId;
	private String inputCause;
	private String vpnInfo;
	
	
	public String getVpnInfo() {
		return vpnInfo;
	}
	public void setVpnInfo(String vpnInfo) {
		this.vpnInfo = vpnInfo;
	}
	public String getDcId() {
		return dcId;
	}
	public void setDcId(String dcId) {
		this.dcId = dcId;
	}
	public String getImageName() {
		return imageName;
	}
	public void setImageName(String imageName) {
		this.imageName = imageName;
	}
	public PriceDetails getPriceDetails() {
		return priceDetails;
	}
	public void setPriceDetails(PriceDetails priceDetails) {
		this.priceDetails = priceDetails;
	}
	public ParamBean getParamBean() {
		return paramBean;
	}
	public void setParamBean(ParamBean paramBean) {
		this.paramBean = paramBean;
	}
	public Date getExchangeTime() {
		return exchangeTime;
	}
	public void setExchangeTime(Date exchangeTime) {
		this.exchangeTime = exchangeTime;
	}
	public String getEcscRemark() {
		return ecscRemark;
	}
	public void setEcscRemark(String ecscRemark) {
		this.ecscRemark = ecscRemark;
	}
	public String getEcmcRemark() {
		return ecmcRemark;
	}
	public void setEcmcRemark(String ecmcRemark) {
		this.ecmcRemark = ecmcRemark;
	}
	public BigDecimal getExchangeMoney() {
		return exchangeMoney;
	}
	public void setExchangeMoney(BigDecimal exchangeMoney) {
		this.exchangeMoney = exchangeMoney;
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
	public String getResourceId() {
		return resourceId;
	}
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	public String getResourceName() {
		return resourceName;
	}
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
	public String getPayType() {
		return payType;
	}
	public void setPayType(String payType) {
		this.payType = payType;
	}
	public String getIncomeType() {
		return incomeType;
	}
	public void setIncomeType(String incomeType) {
		this.incomeType = incomeType;
	}
	public Date getMonStart() {
		return monStart;
	}
	public void setMonStart(Date monStart) {
		this.monStart = monStart;
	}
	public Date getMonEnd() {
		return monEnd;
	}
	public void setMonEnd(Date monEnd) {
		this.monEnd = monEnd;
	}
	public String getConfigure() {
		return configure;
	}
	public void setConfigure(String configure) {
		this.configure = configure;
	}
	public String getCusId() {
		return cusId;
	}
	public void setCusId(String cusId) {
		this.cusId = cusId;
	}
	public String getResourceType() {
		return resourceType;
	}
	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}
	public String getMonContract() {
		return monContract;
	}
	public void setMonContract(String monContract) {
		this.monContract = monContract;
	}
	public String getOperType() {
		return operType;
	}
	public void setOperType(String operType) {
		this.operType = operType;
	}
	public String getInputCause() {
		return inputCause;
	}
	public void setInputCause(String inputCause) {
		this.inputCause = inputCause;
	}
	
	
}

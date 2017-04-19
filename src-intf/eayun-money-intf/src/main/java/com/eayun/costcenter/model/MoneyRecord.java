package com.eayun.costcenter.model;

import java.math.BigDecimal;
import java.util.List;

import com.eayun.costcenter.bean.ConfigureBean;

public class MoneyRecord extends BaseMoneyRecord {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String moneyStr;				// 应付金额(字符串)
	private String accountBalanceStr;		//账户余额(字符串)
	private String monRealPayStr;			//实际支付(字符串)
	private String monArrearsMoney;			//欠费金额(字符串)
	private List<ConfigureBean> configList;
	private String resourceTypeStr;
	private String dcName;					//数据中心名称
	private BigDecimal balancePositive;		//账户余额绝对值
	private BigDecimal prepaymentMoney;		//预付费费用报表应付金额
	
	public BigDecimal getPrepaymentMoney() {
		return prepaymentMoney;
	}
	public void setPrepaymentMoney(BigDecimal prepaymentMoney) {
		this.prepaymentMoney = prepaymentMoney;
	}
	public BigDecimal getBalancePositive() {
		return balancePositive;
	}
	public void setBalancePositive(BigDecimal balancePositive) {
		this.balancePositive = balancePositive;
	}
	public String getDcName() {
		return dcName;
	}
	public void setDcName(String dcName) {
		this.dcName = dcName;
	}
	public String getResourceTypeStr() {
		return resourceTypeStr;
	}
	public void setResourceTypeStr(String resourceTypeStr) {
		this.resourceTypeStr = resourceTypeStr;
	}
	public List<ConfigureBean> getConfigList() {
		return configList;
	}
	public void setConfigList(List<ConfigureBean> configList) {
		this.configList = configList;
	}
	public String getMonArrearsMoney() {
		return monArrearsMoney;
	}
	public void setMonArrearsMoney(String monArrearsMoney) {
		this.monArrearsMoney = monArrearsMoney;
	}
	public String getMonRealPayStr() {
		return monRealPayStr;
	}
	public void setMonRealPayStr(String monRealPayStr) {
		this.monRealPayStr = monRealPayStr;
	}
	public String getAccountBalanceStr() {
		return accountBalanceStr;
	}
	public void setAccountBalanceStr(String accountBalanceStr) {
		this.accountBalanceStr = accountBalanceStr;
	}
	public String getMoneyStr() {
		return moneyStr;
	}
	public void setMoneyStr(String moneyStr) {
		this.moneyStr = moneyStr;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	

}

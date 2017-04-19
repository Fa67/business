package com.eayun.costcenter.bean;

import com.eayun.common.tools.ExcelTitle;

public class ExcelRecord {
	@ExcelTitle(name="流水号")
    private String serialNumber;
	
	@ExcelTitle(name="交易时间")
    private String monTime;
	
	@ExcelTitle(name="收支类型")
	private String incomeType;
	
	@ExcelTitle(name="交易备注")
	private String monEcscRemark;
	
	@ExcelTitle(name="交易金额")
	private String money;
	
	@ExcelTitle(name="账户余额")
	private String balance;

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getMonTime() {
		return monTime;
	}

	public void setMonTime(String monTime) {
		this.monTime = monTime;
	}

	public String getIncomeType() {
		return incomeType;
	}

	public void setIncomeType(String incomeType) {
		this.incomeType = incomeType;
	}

	public String getMonEcscRemark() {
		return monEcscRemark;
	}

	public void setMonEcscRemark(String monEcscRemark) {
		this.monEcscRemark = monEcscRemark;
	}

	public String getMoney() {
		return money;
	}

	public void setMoney(String money) {
		this.money = money;
	}

	public String getBalance() {
		return balance;
	}

	public void setBalance(String balance) {
		this.balance = balance;
	}
	
	
	
}

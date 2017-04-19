package com.eayun.notice.model;

import java.math.BigDecimal;
import java.util.Date;

public class MessageEcscToMailEcmc {
	
	private String serialNumber;//流水号
	
	private Date transactionTime;//交易时间
	
	private String shouruType;//收入类型
	
	private String transactiondesc;//交易备注
	
	private BigDecimal payMoney;//充值金额
	
	private BigDecimal balance;//余额
	
	private String cusId;

	public String getCusId() {
		return cusId;
	}

	public void setCusId(String cusId) {
		this.cusId = cusId;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public Date getTransactionTime() {
		return transactionTime;
	}

	public void setTransactionTime(Date transactionTime) {
		this.transactionTime = transactionTime;
	}

	public String getShouruType() {
		return shouruType;
	}

	public void setShouruType(String shouruType) {
		this.shouruType = shouruType;
	}

	public String getTransactiondesc() {
		return transactiondesc;
	}

	public void setTransactiondesc(String transactiondesc) {
		this.transactiondesc = transactiondesc;
	}

	public BigDecimal getPayMoney() {
		return payMoney;
	}

	public void setPayMoney(BigDecimal payMoney) {
		this.payMoney = payMoney;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}
	
	

}

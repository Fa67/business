package com.eayun.costcenter.model;

import java.math.BigDecimal;

public class MoneyAccount extends BaseMoneyAccount {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private boolean isCanPay=true;		//余额是否足以支付本次费用
	
	private BigDecimal balancePositive;	//账户余额的绝对值
	private boolean isSendMessage = false;			//是否允许发送消息
	private boolean isRefunded =false;			//是否已经退过款	true为已退过
	
	
	
	
	public boolean isRefunded() {
		return isRefunded;
	}
	public void setRefunded(boolean isRefunded) {
		this.isRefunded = isRefunded;
	}
	public boolean isSendMessage() {
		return isSendMessage;
	}
	public void setSendMessage(boolean isSendMessage) {
		this.isSendMessage = isSendMessage;
	}
	

	public BigDecimal getBalancePositive() {
		return balancePositive;
	}

	public void setBalancePositive(BigDecimal balancePositive) {
		this.balancePositive = balancePositive;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public boolean isCanPay() {
		return isCanPay;
	}

	public void setCanPay(boolean isCanPay) {
		this.isCanPay = isCanPay;
	}
	
	
}

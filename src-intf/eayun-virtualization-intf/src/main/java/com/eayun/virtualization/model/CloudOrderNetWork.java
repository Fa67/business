package com.eayun.virtualization.model;

import java.math.BigDecimal;
import java.util.Date;

public class CloudOrderNetWork extends BaseCloudOrderNetWork {

    /**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 4079762850628487006L;
    private String dcName;
    private Date endTime;
    private int rateOld;
    private BigDecimal accountPayment;              //账户支付金额
    private BigDecimal thirdPartPayment;            //第三方支付金额
    private String cycleType;
    
    public String getDcName() {
        return dcName;
    }
    public void setDcName(String dcName) {
        this.dcName = dcName;
    }
    public Date getEndTime() {
        return endTime;
    }
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
    public int getRateOld() {
        return rateOld;
    }
    public void setRateOld(int rateOld) {
        this.rateOld = rateOld;
    }
    public BigDecimal getAccountPayment() {
        return accountPayment;
    }
    public void setAccountPayment(BigDecimal accountPayment) {
        this.accountPayment = accountPayment;
    }
    public BigDecimal getThirdPartPayment() {
        return thirdPartPayment;
    }
    public void setThirdPartPayment(BigDecimal thirdPartPayment) {
        this.thirdPartPayment = thirdPartPayment;
    }
	public String getCycleType() {
		return cycleType;
	}
	public void setCycleType(String cycleType) {
		this.cycleType = cycleType;
	}
    
}

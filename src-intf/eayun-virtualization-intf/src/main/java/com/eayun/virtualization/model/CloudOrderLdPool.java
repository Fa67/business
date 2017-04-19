package com.eayun.virtualization.model;

import java.math.BigDecimal;
import java.util.Date;

public class CloudOrderLdPool extends BaseCloudOrderLdPool {

    /**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1483909181813628842L;
    private String dcName;
    private Date endTime;
    private Long connectionLimitOld;
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
    public Long getConnectionLimitOld() {
        return connectionLimitOld;
    }
    public void setConnectionLimitOld(Long connectionLimitOld) {
        this.connectionLimitOld = connectionLimitOld;
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

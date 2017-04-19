package com.eayun.virtualization.model;

import java.math.BigDecimal;
import java.util.List;

public class CloudOrderFloatIp extends BaseCloudOrderFloatIp {

    /**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3533303922324608053L;

    private String dcName;
    private BigDecimal thirdPartPayment;//第三方支付金额
    private BigDecimal accountPayment;//账户支付金额
    
    private String cycleType;
    private List<CloudFloatIp> cloudFloatIpList;

    public List<CloudFloatIp> getCloudFloatIpList() {
        return cloudFloatIpList;
    }

    public void setCloudFloatIpList(List<CloudFloatIp> cloudFloatIpList) {
        this.cloudFloatIpList = cloudFloatIpList;
    }

    public String getDcName() {
        return dcName;
    }

    public void setDcName(String dcName) {
        this.dcName = dcName;
    }

    public BigDecimal getThirdPartPayment() {
        return thirdPartPayment;
    }

    public void setThirdPartPayment(BigDecimal thirdPartPayment) {
        this.thirdPartPayment = thirdPartPayment;
    }

    public BigDecimal getAccountPayment() {
        return accountPayment;
    }

    public void setAccountPayment(BigDecimal accountPayment) {
        this.accountPayment = accountPayment;
    }

    public String getCycleType() {
        return cycleType;
    }

    public void setCycleType(String cycleType) {
        this.cycleType = cycleType;
    }
}

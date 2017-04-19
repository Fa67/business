package com.eayun.virtualization.model;

import java.math.BigDecimal;
import java.util.Date;

public class CloudOrderVpn extends BaseCloudOrderVpn {

    /**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -4183335916082846181L;

    private String dcName;
    
    private String netName;
    private String subnetName;
    private String subnetCidr;
    private Date endTime;
    
    private BigDecimal accountPayment;              //账户支付金额
    private BigDecimal thirdPartPayment;            //第三方支付金额
    private String cycleType;
    public String getDcName() {
        return dcName;
    }
    public void setDcName(String dcName) {
        this.dcName = dcName;
    }
    public String getNetName() {
        return netName;
    }
    public void setNetName(String netName) {
        this.netName = netName;
    }
    public String getSubnetName() {
        return subnetName;
    }
    public void setSubnetName(String subnetName) {
        this.subnetName = subnetName;
    }
    public String getSubnetCidr() {
        return subnetCidr;
    }
    public void setSubnetCidr(String subnetCidr) {
        this.subnetCidr = subnetCidr;
    }
    public Date getEndTime() {
        return endTime;
    }
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
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

/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.income.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 *                       
 * @Filename: IncomeInfoSearchResult.java
 * @Description: 
 * @Version: 1.0
 * @Author: fangjun.yang
 * @Email: fangjun.yang@eayun.com
 * @History:<br>
 *<li>Date: 2017年4月14日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class IncomeInfoResultItem implements Serializable {

    private static final long serialVersionUID = 6277730258428083737L;

    private String            monId;

    private String            serialNumber;

    private String            thirdId;

    private Date              paidTime;

    private BigDecimal        amount;

    private String            cusId;

    private String            cusName;

    private String            cusPhone;

    private String            prodName;

    private String            resourceType;

    private String            resouceTypeName;

    private String            incomeType;

    private String            incomeTypeName;

    private String            payType;

    private String            dcId;

    private String            dcName;

    public String getMonId() {
        return monId;
    }

    public void setMonId(String monId) {
        this.monId = monId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getThirdId() {
        return thirdId;
    }

    public void setThirdId(String thirdId) {
        this.thirdId = thirdId;
    }

    public Date getPaidTime() {
        return paidTime;
    }

    public void setPaidTime(Date paidTime) {
        this.paidTime = paidTime;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCusId() {
        return cusId;
    }

    public void setCusId(String cusId) {
        this.cusId = cusId;
    }

    public String getCusName() {
        return cusName;
    }

    public void setCusName(String cusName) {
        this.cusName = cusName;
    }

    public String getCusPhone() {
        return cusPhone;
    }

    public void setCusPhone(String cusPhone) {
        this.cusPhone = cusPhone;
    }

    public String getProdName() {
        return prodName;
    }

    public void setProdName(String prodName) {
        this.prodName = prodName;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResouceTypeName() {
        return resouceTypeName;
    }

    public void setResouceTypeName(String resouceTypeName) {
        this.resouceTypeName = resouceTypeName;
    }

    public String getIncomeType() {
        return incomeType;
    }

    public void setIncomeType(String incomeType) {
        this.incomeType = incomeType;
    }

    public String getIncomeTypeName() {
        return incomeTypeName;
    }

    public void setIncomeTypeName(String incomeTypeName) {
        this.incomeTypeName = incomeTypeName;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public String getDcId() {
        return dcId;
    }

    public void setDcId(String dcId) {
        this.dcId = dcId;
    }

    public String getDcName() {
        return dcName;
    }

    public void setDcName(String dcName) {
        this.dcName = dcName;
    }

}

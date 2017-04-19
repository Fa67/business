/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.pay.model;

import java.io.Serializable;

/**
 *                       
 * @Filename: AlipayTradeResult.java
 * @Description: 
 * @Version: 1.0
 * @Author: fangjun.yang
 * @Email: fangjun.yang@eayun.com
 * @History:<br>
 *<li>Date: 2016年8月1日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class TradeResult implements Serializable {

    private static final long serialVersionUID = -1123371587955100409L;

    private boolean           isQuerySuccess   = false;

    private String            thirdId;

    private String            tradeNo;

    private String            payId;

    private String            payStatus;

    private String            thirdResult;

    public boolean isQuerySuccess() {
        return isQuerySuccess;
    }

    public void setQuerySuccess(boolean isQuerySuccess) {
        this.isQuerySuccess = isQuerySuccess;
    }

    public String getThirdId() {
        return thirdId;
    }

    public void setThirdId(String thirdId) {
        this.thirdId = thirdId;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public String getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(String payStatus) {
        this.payStatus = payStatus;
    }

    public String getPayId() {
        return payId;
    }

    public void setPayId(String payId) {
        this.payId = payId;
    }
    
    public String getThirdResult() {
        return thirdResult;
    }

    public void setThirdResult(String thirdResult) {
        this.thirdResult = thirdResult;
    }

    public TradeResult() {

    }

}

/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.pay.model;

import java.io.Serializable;

import javax.persistence.Column;

/**
 * 支付记录和订单的关联关系                
 * @Filename: BasePayOrderRecord.java
 * @Description: 
 * @Version: 1.0
 * @Author: fangjun.yang
 * @Email: fangjun.yang@eayun.com
 * @History:<br>
 *<li>Date: 2016年7月27日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
//@Entity
//@Table(name = "pay_orderrecord")
public class BasePayOrderRecord implements Serializable{

    private static final long serialVersionUID = -4650161396483957386L;

    @Column(name = "trade_no", length = 32)
    private String tradeNo;                 //支付流水号
    
    private String orderNo;                 //订单号

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }
    
}

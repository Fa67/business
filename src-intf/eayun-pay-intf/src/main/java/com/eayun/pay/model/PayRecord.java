/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.pay.model;

/**
 *                       
 * @Filename: PayRecord.java
 * @Description: 
 * @Version: 1.0
 * @Author: fangjun.yang
 * @Email: fangjun.yang@eayun.com
 * @History:<br>
 *<li>Date: 2016年7月28日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class PayRecord extends BasePayRecord {

    private static final long serialVersionUID = 2734230099167398954L;

    /**
     * 支付类型
     */
    public interface PayType {
        /**
         * 余额支付
         */
        String BALANCE = "0";
        /**
         * 支付宝
         */
        String ALIPAY  = "1";
    }

    /**
     * 交易类型
     */
    public interface TradeType {
        /**
         * 订单支付
         */
        String ORDER_PAY = "1";
        /**
         * 充值
         */
        String RECHARGE  = "2";
    }

    /**
     * 支付状态
     */
    public interface PayStatus {
        /**
         * 支付中
         */
        String ON_PAY  = "0";
        /**
         * 支付成功
         */
        String SUCCESS = "1";
        /**
         * 支付失败
         */
        String FAIL    = "2";
        /**
         * 支付超时
         */
        String TIME_OUT = "3";
    }
}

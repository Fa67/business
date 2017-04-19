/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.pay.model;

/**
 *                       
 * @Filename: RefundRecord.java
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
public class RefundRecord extends BaseRefundRecord {

    private static final long serialVersionUID = 9083680005256662775L;

    /**
     * 退款类型
     */
    public interface RefundType {
        /**
         * 余额退款
         */
        String BALANCE = "0";
        /**
         * 支付宝退款
         */
        String ALIPAY  = "1";
    }

    /**
     * 退款状态
     */
    public interface RefundStatus {
        /**
         * 退款中
         */
        String ON_REFUND = "0";

        /**
         * 退款成功
         */
        String SUCCESS   = "1";

        /**
         * 退款失败
         */
        String FAIL      = "2";

    }
    
}

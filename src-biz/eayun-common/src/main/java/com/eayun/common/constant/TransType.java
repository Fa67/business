package com.eayun.common.constant;

/**
 *
 * @Filename: TransType.java
 * @Description: 交易类型 - 用户费用中心交易记录中
 * @Version: 1.0
 * @Author: fan.zhang
 * @Email: fan.zhang@eayun.com
 * @History:<br>
 *<li>Date: 2016年8月4日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public interface TransType {
    /**
     * 充值
     */
    String RECHARGE = "1";
    /**
     * 退款
     */
    String REFUND = "2";
    /**
     * 消费
     */
    String EXPEND = "3";
    /**
     * 系统变更，运维后台<strong>增加</strong>客户余额
     */
    String SYSTEM_INCREASE = "4";
    /**
     * 系统变更，运维后台<strong>减少</strong>客户余额
     */
    String SYSTEM_CUT = "5";
}

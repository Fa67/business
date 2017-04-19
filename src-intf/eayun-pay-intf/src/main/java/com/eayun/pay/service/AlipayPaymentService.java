/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.pay.service;

import java.math.BigDecimal;
import java.util.Map;

import com.eayun.common.exception.AppException;
import com.eayun.pay.model.TradeResult;

/**
 *                       
 * @Filename: AlipayPaymentService.java
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
public interface AlipayPaymentService {

    /**
     * 创建即时到账交易支付表单
     * @param outTradeNo 商户订单号，商户网站订单系统中唯一订单号
     * @param amount 支付金额
     * @param prodName 产品名称
     * @param prodDesc 产品描述 可空
     * @return
     * @throws AppException
     */
    public String createDirectPayForm(String outTradeNo, BigDecimal amount, String prodName, String prodDesc) throws AppException;
    
    /**
     * 处理交易通知
     * @param params
     * @throws AppException
     */
    public void processTradeNotify(Map<String, String> params) throws AppException;
    
    /**
     * 查询交易状态<br>
     * 参数不能同时为空，否则抛出 {@link java.lang.IllegalArgumentException}
     * @param ourTradeNo 商户订单号，商户网站订单系统中唯一订单号
     * @param alipayTradeNo 支付宝流水号
     * @return
     */
    public TradeResult singleTradeQuery(String ourTradeNo, String alipayTradeNo);

}

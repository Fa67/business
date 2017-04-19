/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.pay.service;

import java.math.BigDecimal;
import java.util.List;

import com.eayun.common.exception.AppException;

/**
 *                       
 * @Filename: EayunPaymentService.java
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
public interface EayunPaymentService {
    
    /**
     * 充值
     * @param cusId
     * @param userId
     * @param amount
     * @param payType
     * @return
     * @throws AppException
     */
    public String createRechargeForm(String cusId, String userId, BigDecimal amount, String payType) throws AppException;

    /**
     * 余额支付
     * @param orderNo 订单号
     * @param amount 支付金额
     * @param cusId 客户ID
     * @param userId 用户ID
     * @param prodName 产品名称
     * @return tradeNo 支付流水号
     */
    public String balancePay(String orderNo, String cusId, String userId, String prodName, BigDecimal amount, String resourceTpe) throws AppException;
    
    /**
     * 获取支付宝即时交易支付表单
     * @param orderNoList
     * @param cusId
     * @param userId
     * @param amount
     * @param prodName
     * @param prodDesc
     * @return
     * @throws AppException
     */
    public String getOrderAlipayForm(List<String> orderNoList, String cusId, String userId, BigDecimal amount, String prodName, String prodDesc) throws AppException;
    
    /**
     * 校验订单列表中是否存在已经完成第三方支付的订单
     * @param orderNoList
     * @return
     */
    public boolean containsThirdPartPaidOrder(List<String> orderNoList);
    /**
     * 订单退款
     * @param orderNo
     * @param cusId
     * @param amount
     * @param prodName
     * @param remark
     * @throws AppException
     */
    public void orderRefund(String orderNo, String cusId, BigDecimal amount, String prodName) throws AppException;
    
    /**
     * 第三方支付结果确认
     * @param payId 支付记录的ID
     * @param thirdId
     * @param isPaid
     * @param thirdResult
     */
    public void confirmPaid(String payId, String thirdId, boolean isPaid, String thirdResult);
    
    /**
     * 查询第三方支付是否成功<br>
     * 注:如果本地支付状态是支付中,去第三方查询到支付状态为成功/失败<br>
     * 会调用支付确认接口{@link com.eayun.pay.service.EayunPaymentService#confirmPaid}
     * @param tradeNo 支付记录流水号
     * @return
     * @throws AppException
     */
    public boolean isThirdPaid(String tradeNo) throws AppException;
    
    /**
     * 检测支付记录状态
     */
    public void doCheckPayStatus();

}

/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.invoice.service;

import java.math.BigDecimal;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.invoice.model.InvoiceApply;
import com.eayun.invoice.model.InvoiceBillableAmount;

/**
 *                       
 * @Filename: InvoiceService.java
 * @Description: 
 * @Version: 1.0
 * @Author: fangjun.yang
 * @Email: fangjun.yang@eayun.com
 * @History:<br>
 *<li>Date: 2017年2月24日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public interface InvoiceService {
    
    /**
     * 添加开票申请
     * @param apply
     * @throws Exception
     */
    public void addInvoiceApply(InvoiceApply apply) throws Exception;

    /**
     * 分页查询开票申请
     * @param queryMap
     * @param cusId
     * @param status
     * @param startTime 创建时间起始范围
     * @param endTime 创建时间结束范围
     * @return
     * @throws Exception
     */
    public Page queryInvoiceApplys(QueryMap queryMap, String cusId, String status, String startTime, String endTime) throws Exception;

    /**
     * 取消开票申请
     * @param id
     * @throws Exception
     */
    public void cancelInvoiceApply(String id) throws Exception;
    
    /**
     * 获取单个开票申请
     * @param id
     * @return
     */
    public InvoiceApply getInvoiceApply(String id);

    /**
     * 获取可开票金额
     * @param cusId
     * @return
     */
    public InvoiceBillableAmount getBillableAmount(String cusId);

    /**
     * 初始化可开票金额（仅创建客户时调用）
     * @param cusId
     */
    public void initBillableAmount(String cusId);

    /**
     * 累加可开票总金额
     * @param cusId
     * @param incrAmount
     * @throws Exception
     */
    public void incrBillableTotalAmount(String cusId, BigDecimal incrAmount) throws Exception;

    /**
     * 累加已开票金额
     * @param cusId
     * @param incrAmount
     * @throws Exception
     */
    public void incrBillableInvoicedAmount(String cusId, BigDecimal incrAmount) throws Exception;

    /**
     * 累加冻结金额
     * @param cusId
     * @param incrAmount
     * @throws Exception
     */
    public void incrBillableFrozenAmount(String cusId, BigDecimal incrAmount) throws Exception;

    /**
     * 减少冻结金额
     * @param cusId
     * @param decrAmount
     * @throws Exception
     */
    public void decrBillableFrozenAmount(String cusId, BigDecimal decrAmount) throws Exception;
    
    /**
     * 获取可开票金额下限
     * @return
     */
    public String getBillableLowerLimit();

}

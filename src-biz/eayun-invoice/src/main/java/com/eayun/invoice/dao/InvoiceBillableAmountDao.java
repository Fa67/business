/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.invoice.dao;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.invoice.model.BaseInvoiceBillableAmount;

/**
 *                       
 * @Filename: InvoiceBillableAmountDao.java
 * @Description: 
 * @Version: 1.0
 * @Author: fangjun.yang
 * @Email: fangjun.yang@eayun.com
 * @History:<br>
 *<li>Date: 2017年2月27日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public interface InvoiceBillableAmountDao extends IRepository<BaseInvoiceBillableAmount, String> {

    public List<BaseInvoiceBillableAmount> findByCusId(String cusId);

    public Long countByCusId(String cusId);

    @Query("update BaseInvoiceBillableAmount ba set ba.totalAmount = (ba.totalAmount + ?2) where ba.cusId = ?1")
    @Modifying
    public void incrTotalAmount(String cusId, BigDecimal incrAmount);

    @Query("update BaseInvoiceBillableAmount ba set ba.invoicedAmount = (ba.invoicedAmount + ?2) where ba.cusId = ?1")
    @Modifying
    public void incrInvoicedAmount(String cusId, BigDecimal incrAmount);

    @Query("update BaseInvoiceBillableAmount ba set ba.frozenAmount = (ba.frozenAmount + ?2) where ba.cusId = ?1")
    @Modifying
    public void incrFrozenAmount(String cusId, BigDecimal incrAmount);

    @Query("update BaseInvoiceBillableAmount ba set ba.frozenAmount = (ba.frozenAmount - ?2) where ba.cusId = ?1")
    @Modifying
    public void decrFrozenAmount(String cusId, BigDecimal decrAmount);

}

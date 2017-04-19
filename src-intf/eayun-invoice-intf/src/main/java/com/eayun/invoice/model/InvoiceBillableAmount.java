/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.invoice.model;

import java.math.BigDecimal;

/**
 *                       
 * @Filename: InvoiceBillableAmount.java
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
public class InvoiceBillableAmount extends BaseInvoiceBillableAmount {

    private static final long serialVersionUID = -8811994951717530781L;

    private BigDecimal        billableAmount;

    /**
     * 调用该方法，需确保 totalAmount invoicedAmount frozenAmount 三个字段不为null，否则将返回null
     * @return
     */
    public BigDecimal getBillableAmount() {
        if (billableAmount == null && getTotalAmount() != null && getInvoicedAmount() != null && getFrozenAmount() != null) {
            double amount = getTotalAmount().doubleValue() - getInvoicedAmount().doubleValue() - getFrozenAmount().doubleValue();
            billableAmount = new BigDecimal(amount);
        }
        return billableAmount;
    }

    public void setBillableAmount(BigDecimal billableAmount) {
        this.billableAmount = billableAmount;
    }

}

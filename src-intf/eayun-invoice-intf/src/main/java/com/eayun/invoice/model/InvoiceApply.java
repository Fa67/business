/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.invoice.model;

import com.eayun.common.constant.Invoice;

/**
 *                       
 * @Filename: InvoiceApply.java
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
public class InvoiceApply extends BaseInvoiceApply {

    private static final long serialVersionUID = 3793919688620107418L;

    private String            invoiceTypeName;

    private String            statusName;

    private String            cusName;

    /**
     * 调用该方法，需确保invoiceType字段不为空，否则返回null
     * @return 发票类型名称
     */
    public String getInvoiceTypeName() {
        if (invoiceTypeName == null) {
            invoiceTypeName = Invoice.getInvoiceTypeName(getInvoiceType());
        }
        return invoiceTypeName;
    }

    /**
     * 调用该方法，需确保status字段不为空，否则返回null
     * @return 状态名称
     */
    public String getStatusName() {
        if (statusName == null) {
            statusName = Invoice.getApplyStatusName(getStatus());
        }
        return statusName;
    }

    public String getCusName() {
        return cusName;
    }

    public void setCusName(String cusName) {
        this.cusName = cusName;
    }
    
}

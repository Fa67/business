/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.invoice.model;

import com.eayun.common.constant.Invoice;

/**
 *                       
 * @Filename: InvoiceInfo.java
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
public class InvoiceInfo extends BaseInvoiceInfo {

    private static final long serialVersionUID = 6800315194453793376L;

    private String invoiceTypeName;

    /**
     * 调用该方法需确保invoiceType不为空，否则返回null
     * @return 发票类型名称
     */
    public String getInvoiceTypeName() {
        if(invoiceTypeName == null) {
            invoiceTypeName =  Invoice.getInvoiceTypeName(getInvoiceType());
        }
        return invoiceTypeName;
    }

}

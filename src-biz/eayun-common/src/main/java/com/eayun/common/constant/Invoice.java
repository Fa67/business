/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.common.constant;

import org.apache.commons.lang3.StringUtils;

import com.eayun.common.constant.Invoice.InvoiceInfo.InvoiceType;
import com.eayun.common.constant.Invoice.InvoiceInfo.InvoieTypeName;

/**
 *                       
 * @Filename: Invoice.java
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
public class Invoice {

    public interface InvoiceInfo {
        /**
         * 开票信息最多能够创建的调试
         */
        public long MAX_CREATEABLE_COUNT = 5;

        public interface DefaultItem {
            String YES = "1";
            String NO  = "0";
        }

        public interface InvoiceType {
            String ORDINARY = "1";
            String SPECIAL  = "2";
        }

        public interface InvoieTypeName {
            String ORDINARY = "增值税普通发票";
            String SPECIAL  = "增值税专用发票";
        }
    }

    public interface DeliverAddress {
        public interface DefaultItem {
            String YES = "1";
            String NO  = "0";
        }
    }

    public interface Status {
        /**
         * 待开票
         */
        String PENDING        = "0";

        /**
         * 处理中
         */
        String PROCESSING     = "10";

        /**
         * 已开票
         */
        String INVOICED       = "20";

        /**
         * 已取消(用户自己取消)
         */
        String CANCEL_BY_USER = "30";

        /**
         * 已取消(ecmc取消)
         */
        String CANCEL_BY_ECMC = "31";
    }

    public interface StatusName {
        /**
         * 待开票
         */
        String PENDING    = "待开票";

        /**
         * 处理中
         */
        String PROCESSING = "处理中";

        /**
         * 已开票
         */
        String INVOICED   = "已开票";

        /**
         * 已取消
         */
        String CANCELED   = "已取消";
    }

    public interface NodeId {
        String BILLABLE_AMOUNT_LOWER_LIMIT = "0019001";
        String CANCEL_REASONS              = "0019002";
        String EXPRESS_NAMES               = "0019003";
        String NOEXPRESS_TIPS              = "0019004";
    }

    public interface UsedExpress {
        String YES = "1";
        String NO  = "0";
    }

    public static String getInvoiceTypeName(String invoiceType) {
        if (StringUtils.isNotBlank(invoiceType)) {
            if (invoiceType.equals(InvoiceType.ORDINARY)) {
                return InvoieTypeName.ORDINARY;
            } else if (invoiceType.equals(InvoiceType.SPECIAL)) {
                return InvoieTypeName.SPECIAL;
            }
        }
        return null;
    }

    public static String getApplyStatusName(String status) {
        if (StringUtils.isNotBlank(status)) {
            if (status.equals(Status.PENDING)) {
                return StatusName.PENDING;
            } else if (status.equals(Status.PROCESSING)) {
                return StatusName.PROCESSING;
            } else if (status.equals(Status.INVOICED)) {
                return StatusName.INVOICED;
            } else if (status.equals(Status.CANCEL_BY_USER) || status.equals(Status.CANCEL_BY_ECMC)) {
                return StatusName.CANCELED;
            }
        }
        return "";
    }

}

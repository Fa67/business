package com.eayun.pay.model;

/**
 * 
 *                       
 * @Filename: IAlipayPayment.java
 * @Description: 
 * @Version: 1.0
 * @Author: fangjun.yang
 * @Email: fangjun.yang@eayun.com
 * @History:<br>
 *<li>Date: 2016年7月29日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class AlipayConstants {

    /**
     * 支付宝通知类型
     */
    public interface NotifyType {
        /**
         * 支付宝钱包支付通知
         */
        final String TRADE_STATUS_SYNC   = "trade_status_sync";
        /**
         * 即时到账批量退款有密接口通知
         */
        final String BATCH_REFUND_NOTIFY = "batch_refund_notify";
        /**
         * 批量转账通知消息
         */
        final String BATCH_TRANS_NOTIFY  = "batch_trans_notify";
    }

    /**
     * 支付宝接口名称
     */
    public interface Service {
        /**
         * 手机wap重定向支付
         */
        final String WAP_DIRECT_PAY                   = "alipay.wap.create.direct.pay.by.user";
        /**
         * 即时到账交易
         */
        final String CREATE_DIRECT_PAY_BY_USER        = "create_direct_pay_by_user";
        /**
         * 客户支付宝钱包支付
         */
        final String MOBILE_SECURITY_PAY              = "mobile.securitypay.pay";
        /**
         * 单笔交易查询
         */
        final String SINGLE_TRADE_QUERY               = "single_trade_query";
        /**
         * 即时到账批量退款有密接口
         */
        final String REFUND_FASTPAY_BY_PLATFORM_PWD   = "refund_fastpay_by_platform_pwd";
        /**
         * 即时到账批量退款无密接口
         */
        final String REFUND_FASTPAY_BY_PLATFORM_NOPWD = "refund_fastpay_by_platform_nopwd";
    }

    /**
     * 支付宝参数Key
     */
    public interface ParamKeys {
        /**
         * 内部支付流水号
         */
        final String OUT_TRADE_NO        = "out_trade_no";
        /**
         * 支付宝流水号
         */
        final String TRADE_NO            = "trade_no";
        /**
         * 支付状态
         */
        final String TRADE_STATUS        = "trade_status";

        final String GMT_CREATE          = "gmt_create";

        final String GMT_PAYMENT         = "gmt_payment";

        final String TOTAL_FEE           = "total_fee";

        final String NOTIFY_ID           = "notify_id";

        final String NOTIFY_TYPE         = "notify_type";

        final String SERVICE             = "service";

        final String PARTNER             = "partner";

        final String SELLER_ID           = "seller_id";

        final String _INPUT_CHARSET      = "_input_charset";

        final String PAYMENT_TYPE        = "payment_type";

        final String NOTIFY_URL          = "notify_url";

        final String RETURN_URL          = "return_url";

        final String SUBJECT             = "subject";

        final String IT_B_PAY            = "it_b_pay";

        final String SIGN                = "sign";

        final String SIGN_TYPE           = "sign_type";

        final String SELLER_USER_ID      = "seller_user_id";

        final String REFUND_DATE         = "refund_date";

        final String BATCH_NO            = "batch_no";

        final String BATCH_NUM           = "batch_num";

        final String DETAIL_DATA         = "detail_data";

        final String RESULT_DETAILS      = "result_details";

        final String SUCCESS_DETAILS     = "success_details";

        final String FAIL_DETAILS        = "fail_details";

        final String SUCCESS_NUM         = "success_num";

        final String BUYER_ID            = "buyer_id";

        final String BUYER_EMAIL         = "buyer_email";

        final String USE_COUPON          = "use_coupon";

        final String NOTIFY_TIME         = "notify_time";

        final String DISCOUNT            = "discount";

        final String IS_TOTAL_FEE_ADJUST = "is_total_fee_adjust";

        final String PRICE               = "price";

        final String QUANTITY            = "quantity";

    }

    /**
     * 交易状态
     */
    public interface TradeStatus {
        /**
         * 交易创建，等待买家付款。
         */
        final String WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
        /**
         * 在指定时间段内未支付时关闭的交易；
         * 在交易完成全额退款成功时关闭的交易。
         */
        final String TRADE_CLOSED   = "TRADE_CLOSED";
        /**
         * 交易成功，且可对该交易做操作，如：多级分润、退款等。
         */
        final String TRADE_SUCCESS  = "TRADE_SUCCESS";
        /**
         *  交易成功且结束，即不可再做任何操作。
         */
        final String TRADE_FINISHED = "TRADE_FINISHED";

    }

    /**
     * 退款状态
     */
    public interface RefundStatus {
        /**
         * 退款成功：
         *	<li>全额退款情况： trade_status= TRADE_CLOSED，而refund_status=REFUND_SUCCESS</li>
         *	<li>非全额退款情况： trade_status= TRADE_SUCCESS，而 refund_status=REFUND_SUCCESS</li>
         */
        final String REFUND_SUCCESS     = "REFUND_SUCCESS";
        /**
         * 退款关闭
         */
        final String REFUND_CLOSED      = "REFUND_CLOSED";
        /**
         * 等待支付宝退款
         */
        final String WAIT_ALIPAY_REFUND = "WAIT_ALIPAY_REFUND";
        /**
         * 进行中的退款，供查询
         */
        final String ACTIVE_REFUND      = "ACTIVE_REFUND";
    }

    public interface BatchTransNotifyDetailsIdx {
        final short IDX_ORDER_ID    = 0;
        final short IDX_ACCOUNT     = 1;
        final short IDX_NAME        = 2;
        final short IDX_AMOUNT      = 3;
        final short IDX_IDENTITY    = 4;
        final short IDX_REASON      = 5;
        final short IDX_TRADE_NO    = 6;
        final short IDX_FINISH_TIME = 7;
    }

    /**
     * 支付宝xml keys
     */
    public interface XMLKeys {
        final String IS_SUCCESS = "is_success";
        final String PARAM      = "param";
        final String REQUEST    = "request";
        final String RESPONSE   = "response";
        final String TRADE      = "trade";
        final String ALIPAY     = "alipay";
    }

    public interface XMLResult {
        /**
         * 成功
         */
        final String T = "T";
        /**
         * 失败
         */
        final String F = "F";
        /**
         * 处理中
         */
        final String P = "P";
    }

    /**
     * 支付宝账号错误原因
     */
    public enum AccountError {
        ACCOUN_NAME_NOT_MATCH("账号和用户名不匹配"),
        RECEIVE_USER_NOT_EXIST("账号不存在"),
        ERROR_OTHER_NOT_REALNAMED("用户名不是真实姓名");

        private final String value;

        AccountError(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}

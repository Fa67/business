package com.eayun.common.constant;

/**
 * 队列名称定义
 *
 * @author zhouhaitao
 */
public final class EayunQueueConstant {

    /**
     * 云主机（新建/升级）订单支付成功队列
     */
    public static final String QUEUE_ORDER_PAY_VM = "order.pay.vm";
    /**
     * 云硬盘（新建/升级）订单支付成功队列
     */
    public static final String QUEUE_ORDER_PAY_VOLUME = "order.pay.volume";
    /**
     * 私有网络（新建/升级）订单支付成功队列
     */
    public static final String QUEUE_ORDER_PAY_VPC = "order.pay.vpc";
    /**
     * 弹性公网ip订单支付成功队列
     */
    public static final String QUEUE_ORDER_PAY_FLOATIP = "order.pay.floatip";
    /**
     * 负载均衡（新建/升级）订单支付成功队列
     */
    public static final String QUEUE_ORDER_PAY_BALANCER = "order.pay.balancer";
    /**
     * vpn（新建）订单支付成功队列
     */
    public static final String QUEUE_ORDER_PAY_VPN = "order.pay.vpn";

    /**
     * 新购资源计费队列
     */
    public static final String QUEUE_BILL_RESOURCE_PURCHASE = "BILL_RESOURCE_PURCHASE";
    /**
     * 变配资源计费队列
     */
    public static final String QUEUE_BILL_RESOURCE_UPGRADE = "BILL_RESOURCE_UPGRADE";
    /**
     * 删除资源计费队列
     */
    public static final String QUEUE_BILL_RESOURCE_DELETE = "BILL_RESOURCE_DELETE";
    /**
     * 限制资源服务计费队列
     */
    public static final String QUEUE_BILL_RESOURCE_RESTRICT = "BILL_RESOURCE_RESTRICT";
    /**
     * 恢复资源服务计费队列
     */
    public static final String QUEUE_BILL_RESOURCE_RECOVER = "BILL_RESOURCE_RECOVER";
    /**
     * 客户解冻计费队列
     */
    public static final String QUEUE_BILL_CUSTOMER_UNBLOCK = "BILL_CUSTOMER_UNBLOCK";
    /**
     * 资源放入回收站计费队列
     */
    public static final String QUEUE_BILL_RESOURCE_RECYCLE = "BILL_RESOURCE_RECYCLE";
    /**
     * 资源在回收站中还原计费队列
     */
    public static final String QUEUE_BILL_RESOURCE_RESTORE = "BILL_RESOURCE_RESTORE";

    /**
     * 欠费但在保留时长内队列
     */
    public static final String QUEUE_ARREARAGE_IN_RENTENTIONTIME = "ARREARAGE_IN_RENTENTIONTIME";

    /**
     * 欠费达信用额度队列
     */
    public static final String QUEUE_ARREARAGE_REACH_CREDITLIMIT = "ARREARAGE_REACH_CREDITLIMIT";

    /**
     * 欠费且在保留时长外队列
     */
    public static final String QUEUE_ARREARAGE_OUT_RENTENTIONTIME = "ARREARAGE_OUT_RENTENTIONTIME";

    /**
     * OBS数据统计成功队列-OBS计费模块监听该队列
     */
    public static final String QUEUE_BILL_OBS_GATHER_SUCCEED = "BILL_OBS_GATHER_SUCCEED";
    /**
     * 资源续费模块监听该队列
     */
    public static final String QUEUE_RESOURCE_RENEW = "RESOURCE_RENEW";
    /**
     * 余额变动监听该队列
     */
    public static final String QUEUE_BALANCE_CHANGE = "BALANCE_CHANGE";
    /**
     * 重复读取rabbitMQ中每一条消息记录的最大次数
     */
    public static final int RABBITMQ_REPEAT_MAXTIME = 100;


}

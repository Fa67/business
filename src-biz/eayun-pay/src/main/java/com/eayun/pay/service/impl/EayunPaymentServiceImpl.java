/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.pay.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.druid.support.json.JSONUtils;
import com.eayun.common.ConstantClazz;
import com.eayun.common.constant.TransType;
import com.eayun.common.exception.AppException;
import com.eayun.common.service.SerialNumService;
import com.eayun.common.template.EayunRabbitTemplate;
import com.eayun.common.util.TransactionHookUtil;
import com.eayun.common.util.TransactionHookUtil.Hook;
import com.eayun.common.zk.DistributedLockBean;
import com.eayun.common.zk.DistributedLockService;
import com.eayun.common.zk.LockService;
import com.eayun.costcenter.bean.RecordBean;
import com.eayun.costcenter.service.ChangeBalanceService;
import com.eayun.customer.model.BaseUser;
import com.eayun.customer.serivce.UserService;
import com.eayun.invoice.service.InvoiceService;
import com.eayun.log.service.LogService;
import com.eayun.notice.model.MessageEcscToMailEcmc;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.order.model.BaseOrder;
import com.eayun.order.service.OrderService;
import com.eayun.pay.dao.PayRecordDao;
import com.eayun.pay.dao.RefundRecordDao;
import com.eayun.pay.model.BasePayRecord;
import com.eayun.pay.model.BaseRefundRecord;
import com.eayun.pay.model.PayRecord.PayStatus;
import com.eayun.pay.model.PayRecord.PayType;
import com.eayun.pay.model.PayRecord.TradeType;
import com.eayun.pay.model.RefundRecord.RefundStatus;
import com.eayun.pay.model.RefundRecord.RefundType;
import com.eayun.pay.model.TradeResult;
import com.eayun.pay.service.AlipayPaymentService;
import com.eayun.pay.service.EayunPaymentService;

/**
 *                       
 * @Filename: PaymentServiceImpl.java
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
@Service
@Transactional
public class EayunPaymentServiceImpl implements EayunPaymentService {

    private final static Logger    logger = LoggerFactory.getLogger(EayunPaymentServiceImpl.class);

    @Autowired
    private PayRecordDao           payRecordDao;

    @Autowired
    private RefundRecordDao        refundRecordDao;

    @Autowired
    private AlipayPaymentService   alipayPaymentService;

    @Autowired
    private OrderService           orderService;

    @Autowired
    private ChangeBalanceService   changeBalanceService;

    @Autowired
    private EayunRabbitTemplate    eayunRabbitTemplate;

    @Autowired
    private SerialNumService       serialNumService;

    @Autowired
    private DistributedLockService distributedLockService;

    @Autowired
    private LogService             logService;

    @Autowired
    private UserService            userService;
    @Autowired
    private MessageCenterService messagecenterService;

    @Autowired
    private InvoiceService         invoiceService;

    public String createRechargeForm(String cusId, String userId, BigDecimal amount, String payType) throws AppException {
        this.validRechargeParameters(cusId, userId, amount, payType);
        String tradeNo = this.nextTradeNo();
        BasePayRecord payRecord = new BasePayRecord();
        payRecord.setCreateTime(new Date());
        payRecord.setCusId(cusId);
        payRecord.setUserId(userId);
        payRecord.setPayStatus(PayStatus.ON_PAY);
        payRecord.setPayType(payType);
        payRecord.setTradeType(TradeType.RECHARGE);
        payRecord.setPayAmount(amount);
        payRecord.setTradeNo(tradeNo);
        //保存支付记录
        payRecordDao.save(payRecord);
        //构建支付表单
        return alipayPaymentService.createDirectPayForm(payRecord.getPayId(), amount, "充值", "充值");
    }

    protected void validRechargeParameters(String cusId, String userId, BigDecimal amount, String payType) {
        if (StringUtils.isBlank(cusId) || StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException("must get cusId and userId to recharge");
        }
        if (amount == null || amount.compareTo(new BigDecimal("0.000")) <= 0) {
            logger.error("amount error for recharge:{}", amount);
            throw new IllegalArgumentException("amount error for recharge");
        }
        if (StringUtils.isEmpty(payType) || !hasPayType(payType)) {
            throw new IllegalArgumentException("undefined pay type");
        }
    }

    protected boolean hasPayType(String payType) {
        if (PayType.ALIPAY.equals(payType)) {
            return true;
        }
        if (PayType.BALANCE.equals(payType)) {
            return true;
        }
        return false;
    }

    @Override
    public String balancePay(String orderNo, String cusId, String userId, String prodName, BigDecimal amount, String resourceType) throws AppException {

        String tradeNo = this.nextTradeNo();
        BasePayRecord payRecord = new BasePayRecord();
        payRecord.setCreateTime(new Date());
        payRecord.setCusId(cusId);
        payRecord.setUserId(userId);
        payRecord.setPayStatus(PayStatus.SUCCESS);
        payRecord.setPayType(PayType.BALANCE);
        payRecord.setTradeType(TradeType.ORDER_PAY);
        payRecord.setPayAmount(amount);
        payRecord.setTradeNo(tradeNo);
        try {
            this.changeBalanceByOrderBalancePay(orderNo, cusId, prodName, amount, resourceType);
        } catch (Exception e) {
            logger.error("余额支付失败", e);
            throw new AppException("余额不足", e);
        }
        //保存支付记录
        payRecordDao.save(payRecord);
        //保存支付记录-订单关系
        payRecordDao.savePayOrderRecord(tradeNo, orderNo);
        return tradeNo;
    }

    public String getOrderAlipayForm(List<String> orderNoList, String cusId, String userId, BigDecimal amount, String prodName, String prodDesc) throws AppException {

        if (CollectionUtils.isEmpty(orderNoList) || StringUtils.isBlank(cusId) || StringUtils.isBlank(userId) || StringUtils.isBlank(prodName) || amount.compareTo(new BigDecimal("0.000")) <= 0) {
            throw new AppException("参数错误");
        }
        String tradeNo = this.nextTradeNo();

        BasePayRecord payRecord = new BasePayRecord();
        payRecord.setCreateTime(new Date());
        payRecord.setCusId(cusId);
        payRecord.setUserId(userId);
        payRecord.setPayStatus(PayStatus.ON_PAY);
        payRecord.setPayType(PayType.ALIPAY);
        payRecord.setTradeType(TradeType.ORDER_PAY);
        payRecord.setPayAmount(amount);
        payRecord.setTradeNo(tradeNo);
        //保存支付记录
        payRecordDao.save(payRecord);
        //保存支付记录-订单关系
        for (String orderNo : orderNoList) {
            payRecordDao.savePayOrderRecord(tradeNo, orderNo);
        }
        //返回支付表单
        return alipayPaymentService.createDirectPayForm(payRecord.getPayId(), amount, prodName, prodDesc);
    }

    public boolean containsThirdPartPaidOrder(List<String> orderNoList) {
        List<String> tradeNoList = payRecordDao.findTradeNoByOrderNoIn(orderNoList);
        if (CollectionUtils.isNotEmpty(tradeNoList)) {
            for (int i = 0; i < tradeNoList.size(); i++) {
                if (isThirdPaid(tradeNoList.get(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    public void orderRefund(String orderNo, String cusId, BigDecimal amount, String prodName) throws AppException {
        logger.info("order refund:orderNo[{}],cusId[{}], amount[{}]", orderNo, cusId, amount);
        BaseRefundRecord refundRecord = new BaseRefundRecord();
        refundRecord.setCreateTime(new Date());
        refundRecord.setCusId(cusId);
        refundRecord.setFinishTime(new Date());
        refundRecord.setRefundAmount(amount);
        refundRecord.setOrderNo(orderNo);
        refundRecord.setRefundStatus(RefundStatus.SUCCESS);
        refundRecord.setRefundType(RefundType.BALANCE);
        this.changeBalanceByOrderRefund(orderNo, cusId, prodName, amount);
        refundRecordDao.save(refundRecord);
    }

    public void confirmPaid(final String payId, final String thirdId, final boolean isPaid, final String thirdResult) {
        DistributedLockBean dlBean = new DistributedLockBean();
        dlBean.setGranularity("payrecord:" + payId);
        dlBean.setLockService(new LockService() {
            @Override
            public Object doService() throws Exception {
                processConfirmPaid(payId, thirdId, isPaid, thirdResult);
                return null;
            }
        });
        //在确认支付业务上添加分布式锁
        distributedLockService.doServiceByLock(dlBean, true);
    }

    /**
     * 处理确认支付业务
     * @param payId
     * @param thirdId
     * @param isPaid
     * @param thirdResult
     */
    protected void processConfirmPaid(String payId, String thirdId, final boolean isPaid, String thirdResult) {
        String payStatus = isPaid ? PayStatus.SUCCESS : PayStatus.FAIL;
        final BasePayRecord payRecord = payRecordDao.findOne(payId);
        if (payRecord == null) {
            logger.warn("to confirm paid but not find pay record, payId:{}", payId);
            return;
        }
        if (!PayStatus.ON_PAY.equals(payRecord.getPayStatus())) {
            logger.info("to confirm paid but the pay record is paid, payId:{}", payId);
            //表示该条交易已经处理过，无需进行业务处理
            return;
        }
        logger.info("confirm paid to process business, payId:{}", payId);
        if (TradeType.RECHARGE.equals(payRecord.getTradeType())) {
            //调用充值 operType=1 => 余额充值
            BaseUser user = userService.findUserById(payRecord.getUserId());
            if (isPaid) {
                logger.info("change balance by recharge success, payId[{}]", payId);
                this.changeBalanceByRecharge(payRecord.getCusId(), payRecord.getPayAmount(), TransType.RECHARGE);
                logService.addLog("账户充值", user.getUserAccount(), ConstantClazz.LOG_TYPE_ACCOUNT, null, null, payRecord.getCusId(), ConstantClazz.LOG_STATU_SUCCESS, null);
            } else {
                logger.info("recharge is failed by not paid, tradeNo:{}", payRecord.getTradeNo());
                logService.addLog("账户充值", user.getUserAccount(), ConstantClazz.LOG_TYPE_ACCOUNT, null, null, payRecord.getCusId(), ConstantClazz.LOG_STATU_ERROR, null);
            }
        } else if (TradeType.ORDER_PAY.equals(payRecord.getTradeType())) {
            final List<String> orderNoList = payRecordDao.findOrderNo(payRecord.getTradeNo());
            if (isPaid) {
                List<BaseOrder> orderList = orderService.getByOrdersNo(orderNoList);
                logger.info("change balance by third part pay success, payId[{}]", payId);
                //订单支付-充值到余额 operType=3 => 订单支付充值
                this.changeBalanceByRecharge(payRecord.getCusId(), payRecord.getPayAmount(), TransType.EXPEND);
                //余额扣费
                this.changeBalanceByOrdersPaid(orderList);
            } else {
                logger.info("trade failed by not paid, tradeNo:{}", payRecord.getTradeNo());
            }
            TransactionHookUtil.registAfterCommitHook(new Hook() {
                @Override
                public void execute() {
                    //MQ通知订单
                    sendOrderConfirmPaidMQ(payRecord.getTradeNo(), orderNoList, isPaid, payRecord.getUserId());
                }
            });
        }
        this.updatePayRecordByConfirmPaid(payRecord, thirdId, thirdResult, payStatus);
    }

    /**
     * 当确认付款时修改支付记录
     * @param payRecord
     * @param thirdId
     * @param thirdResult
     * @param payStatus
     */
    protected void updatePayRecordByConfirmPaid(BasePayRecord payRecord, String thirdId, String thirdResult, String payStatus) {
        payRecord.setThirdId(thirdId);
        payRecord.setThirdResult(thirdResult);
        payRecord.setPayStatus(payStatus);
        payRecord.setFinishTime(new Date());
        payRecordDao.saveOrUpdate(payRecord);
    }

    /**
     * 订单支付，余额扣费
     * @param orderList 
     */
    protected void changeBalanceByOrdersPaid(List<BaseOrder> orderList) {
        //按订单分别扣费
        if (CollectionUtils.isNotEmpty(orderList)) {
            for (BaseOrder baseOrder : orderList) {
                //修改余额-订单扣费
                try {
                    this.changeBalanceByOrderThirdPay(baseOrder.getOrderNo(), baseOrder.getCusId(), baseOrder.getProdName(), baseOrder.getThirdPartPayment(), baseOrder.getOrderType());
                } catch (Exception e) {
                    //修改余额失败，仅打印异常
                    logger.error("change balance failed:cusId[{}], orderNo[{}]", baseOrder.getCusId(), baseOrder.getOrderNo());
                    logger.error("change balance failed", e);
                }
            }
        }
    }

    public boolean isThirdPaid(String tradeNo) throws AppException {
        if (StringUtils.isEmpty(tradeNo)) {
            throw new AppException("支付流水号不能为空");
        }
        return isThirdPaid(payRecordDao.findByTradeNo(tradeNo));
    }

    protected boolean isThirdPaid(BasePayRecord payRecord) {
        //非余额支付，才进行是否已完成第三方支付判断
        if (payRecord != null && !PayType.BALANCE.equals(payRecord.getPayType())) {
            if (PayStatus.SUCCESS.equals(payRecord.getPayStatus())) {
                return true;
            }
            if (PayType.ALIPAY.equals(payRecord.getPayType())) {
                TradeResult result = alipayPaymentService.singleTradeQuery(payRecord.getPayId(), null);
                if (result != null && result.isQuerySuccess()) {
                    if (!PayStatus.ON_PAY.equals(result.getPayStatus())) {
                        boolean isPaid = PayStatus.SUCCESS.equals(result.getPayStatus());
                        this.confirmPaid(result.getPayId(), result.getThirdId(), isPaid, result.getThirdResult());
                        return isPaid;
                    }
                }
            }
            //other pay type
        }
        return false;
    }

    /**
     * 余额支付，扣费修改余额
     * @param orderNo
     * @param cusId
     * @param prodName
     * @param amount
     * @param resourceType
     * @return
     * @throws Exception
     */
    protected BigDecimal changeBalanceByOrderBalancePay(String orderNo, String cusId, String prodName, BigDecimal amount, String resourceType) throws Exception {
        return changeBalanceService.changeBalanceByPay(newExpenseRecordBean(cusId, prodName, amount, orderNo, resourceType));
    }

    /**
     * 第三方支付，扣费修改余额
     * @param orderNo
     * @param cusId
     * @param prodName
     * @param amount
     * @param resourceType
     * @return
     */
    protected BigDecimal changeBalanceByOrderThirdPay(String orderNo, String cusId, String prodName, BigDecimal amount, String resourceType) {
        return changeBalanceService.changeBalanceForThird(newExpenseRecordBean(cusId, prodName, amount, orderNo, resourceType));
    }

    protected RecordBean newExpenseRecordBean(String cusId, String prodName, BigDecimal amount, String orderNo, String resourceType) {
        RecordBean recordBean = new RecordBean();
        recordBean.setCusId(cusId);
        recordBean.setExchangeTime(new Date());
        recordBean.setEcscRemark("消费-" + prodName);
        recordBean.setEcmcRemark("消费-" + prodName);
        recordBean.setExchangeMoney(amount);
        recordBean.setProductName(prodName);
        recordBean.setOrderNo(orderNo);
        recordBean.setPayType(com.eayun.common.constant.PayType.PAYBEFORE);
        recordBean.setIncomeType("2");
        recordBean.setOperType(TransType.EXPEND);
        recordBean.setResourceType(resourceType);
        return recordBean;
    }

    protected BigDecimal changeBalanceByOrderRefund(String orderNo, String cusId, String prodName, BigDecimal amount) {
        RecordBean recordBean = new RecordBean();
        recordBean.setCusId(cusId);
        recordBean.setExchangeTime(new Date());
        recordBean.setEcscRemark("退款-" + prodName);
        recordBean.setEcmcRemark("退款-" + prodName);
        recordBean.setExchangeMoney(amount);
        recordBean.setProductName(prodName);
        recordBean.setOrderNo(orderNo);
        recordBean.setIncomeType("1");
        recordBean.setOperType(TransType.REFUND);
        return changeBalanceService.changeBalance(recordBean);
    }

    protected void changeBalanceByRecharge(String cusId, BigDecimal amount, String operType) {
        try {
            RecordBean recordBean = new RecordBean();
            recordBean.setCusId(cusId);
            recordBean.setExchangeTime(new Date());
            recordBean.setEcscRemark("充值");
            recordBean.setEcmcRemark("充值");
            recordBean.setExchangeMoney(amount);
            recordBean.setIncomeType("1");
            recordBean.setOperType(operType);
            BigDecimal balance= changeBalanceService.changeBalanceForThird(recordBean);
           
            this.sendMailAfterRecharge(operType, cusId, amount, balance);
           
            
            //增加客户可开票金额的累计金额
            invoiceService.incrBillableTotalAmount(cusId, amount);
        } catch (Exception e) {
            logger.error("change balance failed when recharge:cusId[{}],amount[{}], operType[{}]", cusId, amount, operType);
            logger.error("change balance failed ", e);
        }
        
    }

    /**
     * 确认支付后，发送MQ消息通知订单
     * @param tradeNo
     * @param ordersNo
     * @param isPaid
     * @param userId 
     */
    protected void sendOrderConfirmPaidMQ(String tradeNo, List<String> ordersNo, boolean isPaid, String userId) {
        Map<String, Object> messageMap = new HashMap<String, Object>();
        messageMap.put("userId", userId);
        messageMap.put("tradeNo", tradeNo);
        messageMap.put("ordersNo", ordersNo);
        messageMap.put("isPaid", isPaid);
        String msg = JSONUtils.toJSONString(messageMap);
        logger.info("send order.thirdpart.pay mq,msg content:{}", msg);
        eayunRabbitTemplate.send("order.thirdpart.pay", msg);
    }

    /**
     * 获取下一个支付记录流水号
     * @return
     */
    private String nextTradeNo() {
        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String prefix = "02" + dateStr;
        return serialNumService.getSerialNum(prefix, 8);
    }

    public void doCheckPayStatus() {
        logger.info("check pay status starting");
        List<BasePayRecord> payRecords = payRecordDao.findThirdPartPayRecordByPayStatus(PayStatus.ON_PAY);
        if (CollectionUtils.isNotEmpty(payRecords)) {
            BasePayRecord payRecord;
            for (int i = 0; i < payRecords.size(); i++) {
                payRecord = payRecords.get(i);
                if (isPayExpired(payRecord.getCreateTime())) {
                    payRecord.setPayStatus(PayStatus.TIME_OUT);
                    payRecordDao.save(payRecord);
                } else {
                    //主动检查第三方支付状态
                    this.checkThirdPartPaid(payRecord);
                }
            }
        }
        logger.info("check pay status ended");
    }

    protected void checkThirdPartPaid(BasePayRecord basePayRecord) {
        this.isThirdPaid(basePayRecord);
    }

    protected boolean isPayExpired(Date createtime) {
        return createtime != null && (System.currentTimeMillis() - createtime.getTime() > DateUtils.MILLIS_PER_DAY);
    }

    
    private void sendMailAfterRecharge(String sendWho,String cusId,BigDecimal amount,BigDecimal balance){
    	 MessageEcscToMailEcmc model=new MessageEcscToMailEcmc();
         model.setCusId(cusId);
         model.setPayMoney(amount);
         model.setShouruType("收入");
         model.setTransactiondesc("充值");
         model.setTransactionTime(new Date());
         model.setBalance(balance);
    
    	messagecenterService.ecscToMailEcmc(model,sendWho);
    	
         
}

}

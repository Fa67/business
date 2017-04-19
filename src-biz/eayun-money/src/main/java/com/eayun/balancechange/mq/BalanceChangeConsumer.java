package com.eayun.balancechange.mq;

import com.alibaba.fastjson.JSONObject;
import com.eayun.balancechange.service.PaybackService;
import com.eayun.balancechange.service.PostpayResHandlerService;
import com.eayun.common.constant.EayunQueueConstant;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.constant.TransType;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.template.EayunRabbitTemplate;
import com.eayun.common.zk.DistributedLockBean;
import com.eayun.common.zk.DistributedLockService;
import com.eayun.common.zk.LockService;
import com.eayun.costcenter.service.AccountOverviewService;
import com.eayun.customer.model.Customer;
import com.eayun.customer.serivce.CustomerService;
import com.eayun.syssetup.service.SysDataTreeService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 余额变动监听
 *
 * @Filename: BalanceChangeConsumer.java
 * @Description:
 * @Version: 1.0
 * @Author: zhangfan
 * @Email: fan.zhang@eayun.com
 * @History:<br> <li>Date: 2016年8月12日</li>
 * <li>Version: 1.0</li>
 * <li>Content: create</li>
 */
@Component
public class BalanceChangeConsumer implements ChannelAwareMessageListener {

    private static final Logger log = LoggerFactory.getLogger(BalanceChangeConsumer.class);

    @Autowired
    private JedisUtil jedisUtil;

    @Autowired
    private EayunRabbitTemplate rabbitTemplate;

    @Autowired
    private PaybackService paybackService;

    @Autowired
    private PostpayResHandlerService postpayResHandlerService;

    @Autowired
    private AccountOverviewService accountOverviewService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private SysDataTreeService sysDataTreeService;

    @Autowired
    private DistributedLockService distributedLockService;

    @Override
    public void onMessage(Message msg, Channel channel) throws Exception {
        log.info("监听到客户账户余额变动消息");
        try {
            String msgStr = new String(msg.getBody());
            JSONObject msgJSON = JSONObject.parseObject(msgStr);
            final String transType = msgJSON.getString("biz");
            final BigDecimal revenue = msgJSON.getBigDecimal("revenue");
            final String cusId = msgJSON.getString("customer");
            BigDecimal balanceMsg = msgJSON.getBigDecimal("balance");
            if (balanceMsg == null) {
                balanceMsg = accountOverviewService.getAccountInfo(cusId).getMoney();
            }
            final BigDecimal balance = balanceMsg;

            //声明分布式锁对象
            DistributedLockBean distributedLockBean = new DistributedLockBean();
            distributedLockBean.setGranularity("BalanceChangeOfCus-" + cusId);
            distributedLockBean.setLockService(new LockService() {
                @Override
                public Object doService() throws Exception {
                    Customer customer = customerService.findCustomerById(cusId);
                    BigDecimal credit = customer.getCreditLines();
                    BigDecimal negativeCredit = credit.negate();
                    //判断客户是否欠费超过保留时长
                    boolean isBeyondRetentionTime = isBeyondRetentionTime(customer);
                    //判断客户是否已达信用额度
                    boolean isAlreadyOverCredit = customer.getOverCreditTime() != null ? true : false;

                    if (TransType.RECHARGE.equals(transType)
                            || TransType.REFUND.equals(transType)
                            || TransType.SYSTEM_INCREASE.equals(transType)) {
                        //1.如果是充值、退款、系统增加导致账户余额增加，则检查并补齐欠费记录
                        paybackService.doPayback(cusId, revenue);
                        //2.判断当前账户余额是否超过开通值，检查并恢复受限的后付费资源使用。
                        //获取资源恢复的开通值
                        String openLimitStr = sysDataTreeService.getRenewCondition();
                        BigDecimal openLimit = new BigDecimal(openLimitStr);
                        if (balance.compareTo(openLimit) >= 0) {
                            if (isBeyondRetentionTime) {
                                //如果超过保留时长，则资源已经受限制，则需要修改资源状态并恢复资源的使用
                                postpayResHandlerService.recoverPostPayResource(cusId);
                            } else {
                                //如果未超过保留时长，则只需要修改资源状态为正常即可
                                postpayResHandlerService.modifyResourceStatus(cusId);
                            }

                            if (isAlreadyOverCredit) {
                                //如果客户在本次充值、退款、系统增加前已经达到信用额度（已开始记录保留时长，则首次达到信用额度时间不为null），则要更新首次达到信用额度时间为null
                                customer.setOverCreditTime(null);
                                customerService.updateCreditTime(customer);
                                try {
                                    //为了控制客户达到信用额度只发送一次消息，需要翔宇在redis中写一个key来表示是否发送过，如果发送过消息，则置为1，当客户未达到信用额度后，在这里把key删除掉
                                    jedisUtil.delete(RedisKey.REACH_CREDIST_LIMIT + cusId);
                                } catch (Exception e) {
                                    log.error("后付费资源恢复中删除客户是否发送过达信用额度消息的RedisKey异常", e);
                                }
                            }
                        }
                    } else if (TransType.SYSTEM_CUT.equals(transType)) {
                        //注：消费（续费、购买、升级）是不会导致账户余额为负的！
                        //2.如果是系统减少导致账户余额减少，判断是否余额小于0，检查并修改正常的后付费资源计费状态为余额不足
                        boolean isNegative = balance.compareTo(BigDecimal.ZERO) < 0;
                        if (isNegative) {
                            //判断客户当前是否已经超过信用额度
                            if (balance.compareTo(negativeCredit) <= 0) {
                                //如果客户当前余额修改后已达信用额度
                                if (!isAlreadyOverCredit) {
                                    //如果是第一次达到信用额度，则要更新首次达到信用额度时间
                                    Date date = new Date();
                                    customer.setOverCreditTime(date);
                                    customerService.updateCreditTime(customer);
                                    JSONObject json = new JSONObject();
                                    json.put("customerId", cusId);
                                    json.put("time", date.getTime());
                                    rabbitTemplate.send(EayunQueueConstant.QUEUE_ARREARAGE_REACH_CREDITLIMIT, json.toJSONString());
                                }
                            }
                            //如果欠费，则与定时扣费欠费处理一样，发消息给翔宇。
                            JSONObject json = new JSONObject();
                            json.put("customerId", cusId);
                            if (isBeyondRetentionTime) {
                                //如果超过保留时长，则需要向超过保留时长的队列中发送消息
                                rabbitTemplate.send(EayunQueueConstant.QUEUE_ARREARAGE_OUT_RENTENTIONTIME, json.toJSONString());
                            } else {
                                //如果未超过保留时长，则需要想消息队列“欠费且在保留时长内”发送消息
                                rabbitTemplate.send(EayunQueueConstant.QUEUE_ARREARAGE_IN_RENTENTIONTIME, json.toJSONString());
                            }
                        }
                        //通知超级管理员系统后台扣费,已经在翔宇changeBalance时调用消息中心接口发送过消息，这里不再处理
                    }
                    return null;
                }
            });

            //执行分布式锁内的业务逻辑
            distributedLockService.doServiceByLock(distributedLockBean);

            log.info("客户账户理余额变动消息处成功");
        } catch (Exception e) {
            log.error("客户账户余额变动消息处理失败", e);
        } finally {
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
        }
    }

    private boolean isBeyondRetentionTime(Customer customer) {
        String recoveryTime = sysDataTreeService.getRecoveryTime();
        int retentionTime = Integer.valueOf(recoveryTime);
        Date overCreditTime = customer.getOverCreditTime();
        Date currentTime = new Date();

        //如果欠费首次打到信用额度时间为null，则表示还没有达到信用额度
        if (overCreditTime != null) {
            //计算本次计费时的当前时间距离保留时长计时开始时间的时间间隔
            long timeSpan = currentTime.getTime() - overCreditTime.getTime();
            return timeSpan >= (retentionTime * 60 * 60 * 1000) ? true : false;
        }
        return false;
    }
}

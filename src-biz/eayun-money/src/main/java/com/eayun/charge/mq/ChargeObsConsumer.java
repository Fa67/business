package com.eayun.charge.mq;

import com.alibaba.fastjson.JSONObject;
import com.eayun.charge.bean.ChargeConstant;
import com.eayun.charge.model.ObsStatsBean;
import com.eayun.charge.service.ObsChargeService;
import com.eayun.charge.util.ChargeRecordUtil;
import com.eayun.common.constant.EayunQueueConstant;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.template.EayunRabbitTemplate;
import com.eayun.common.zk.DistributedLockBean;
import com.eayun.common.zk.DistributedLockService;
import com.eayun.common.zk.LockService;
import com.eayun.costcenter.service.AccountOverviewService;
import com.eayun.customer.model.Customer;
import com.eayun.customer.serivce.CustomerService;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.syssetup.service.SysDataTreeService;
import com.rabbitmq.client.Channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * OBS统计成功后计费的消息消费者
 *
 * @Filename: ChargeObsConsumer.java
 * @Description:
 * @Version: 1.0
 * @Author: zhangfan
 * @Email: fan.zhang@eayun.com
 * @History:<br> <li>Date: 2016年8月10日</li>
 * <li>Version: 1.0</li>
 * <li>Content: create</li>
 */
@Component
public class ChargeObsConsumer implements ChannelAwareMessageListener {

    private static final Logger log = LoggerFactory.getLogger(ChargeObsConsumer.class);

    @Autowired
    private ObsChargeService obsChargeService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AccountOverviewService accountOverviewService;

    @Autowired
    private MessageCenterService msgCenterService;

    @Autowired
    private SysDataTreeService sysDataTreeService;

    @Autowired
    private JedisUtil jedisUtil;

    @Autowired
    private EayunRabbitTemplate rabbitTemplate;

    @Autowired
    private ChargeRecordUtil chargeRecordUtil;
    @Autowired
    private DistributedLockService distributedLockService;

    @Override
    public void onMessage(Message msg, Channel channel) throws Exception {
        String cus = null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH");
        try {
            //获取消息，根据类型将对应统计成功与否放入redis指定key中
            String m = new String(msg.getBody());
            JSONObject json = JSONObject.parseObject(m);
            final ObsStatsBean obsStats = JSONObject.toJavaObject(json, ObsStatsBean.class);
            final long chargeTo = obsStats.getChargeTo().getTime();//本次计费时间
            final String cusId = obsStats.getCusId();
            cus = cusId;
            final Date currentTime = format.parse(format.format(new Date(chargeTo)));
            DistributedLockBean dlBean = new DistributedLockBean();
            dlBean.setGranularity("ObsChargeOfCus-" + cusId);
            dlBean.setLockService(new LockService() {

                @Override
                public Object doService() throws Exception {
                    if (obsStats.getStatsType().equals(ChargeConstant.OBS_STATS_TYPE.STORAGE)) {
                        log.info("监听到客户["+cusId+"]的OBS[存储空间用量]统计成功消息");
                        String key = RedisKey.CHARGE_OBS_STORAGE + cusId + ":" + chargeTo;
                        jedisUtil.setEx(key, "1", 1800);//30min*60=1800s
                    } else if (obsStats.getStatsType().equals(ChargeConstant.OBS_STATS_TYPE.USED)) {
                        log.info("监听到客户["+cusId+"]的OBS[下载流量和请求数]统计成功消息");
                        String key = RedisKey.CHARGE_OBS_USED + cusId + ":" + chargeTo;
                        jedisUtil.setEx(key, "1", 1800);//30min*60=1800s
                    } else if (obsStats.getStatsType().equals(ChargeConstant.OBS_STATS_TYPE.CDN_BACK_ORIGIN)) {
                        log.info("监听到客户["+cusId+"]的CDN[回源流量]统计成功消息");
                        String key = RedisKey.CHARGE_OBS_CDN_BACKSOURCE + cusId + ":" + chargeTo;
                        jedisUtil.setEx(key, "1", 1800);
                    } else if (obsStats.getStatsType().equals(ChargeConstant.OBS_STATS_TYPE.CLOUD_RES_CHARGE_DONE)) {
                        log.info("监听到客户["+cusId+"]的[云资源计费]成功消息");
                        String key = RedisKey.CHARGE_OBS_CLOUD_RES_CHARGE_DONE + cusId + ":" + chargeTo;
                        jedisUtil.setEx(key, "1", 1800);
                    } else if(obsStats.getStatsType().equals(ChargeConstant.OBS_STATS_TYPE.CDN_DETAIL_GATHER_DONE)){
                        log.info("监听到客户["+cusId+"]的CDN[下载流量、动态请求数和HTTPS请求数]统计成功消息");
                        String key = RedisKey.CHARGE_OBS_CDN_DETAIL_GATHER_DONE + cusId + ":" + chargeTo;
                        jedisUtil.setEx(key, "1", 1800);
                    }
                    //检查是否存储空间、下载流量和请求数、CDN回源流量都统计成功，如果都统计成功，则执行计费
                    boolean isStorageOK = ("1").equals(jedisUtil.get(RedisKey.CHARGE_OBS_STORAGE + cusId + ":" + chargeTo)) ? true : false;
                    boolean isUsedOK = ("1").equals(jedisUtil.get(RedisKey.CHARGE_OBS_USED + cusId + ":" + chargeTo)) ? true : false;
                    boolean isCDNBackSourceOK = ("1").equals(jedisUtil.get(RedisKey.CHARGE_OBS_CDN_BACKSOURCE + cusId + ":" + chargeTo)) ? true : false;
                    boolean isCloudResChargeDone = ("1").equals(jedisUtil.get(RedisKey.CHARGE_OBS_CLOUD_RES_CHARGE_DONE + cusId + ":" + chargeTo)) ? true : false;
                    boolean isCDNDetailGatherDone = ("1").equals(jedisUtil.get(RedisKey.CHARGE_OBS_CDN_DETAIL_GATHER_DONE + cusId + ":" + chargeTo)) ? true : false;

                    if (isStorageOK && isUsedOK && isCDNBackSourceOK && isCloudResChargeDone && isCDNDetailGatherDone) {
                        BigDecimal totalCost = obsChargeService.doCharge(obsStats);
                        Customer customer = customerService.findCustomerById(cusId);
                        checkPostcondition(customer, totalCost, currentTime);
                    }
                    return null;
                }
            });
            distributedLockService.doServiceByLock(dlBean);
        } catch (Exception e) {
            log.error("客户["+cus+"]OBS计费失败", e);
            chargeRecordUtil.doLog(msg, e, ChargeConstant.CHARGE_RECORD_OP_TYPE.OBS);
        } finally {
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
        }
    }

    private void checkPostcondition(Customer customer, BigDecimal totalCost, Date currentTime) {
        log.info("OBS计费后置条件检查");
        String customerId = customer.getCusId();
        try {
            BigDecimal balance = accountOverviewService.getAccountInfo(customerId).getMoney();
            //判断客户是否已欠费
            if (balance.compareTo(new BigDecimal(0.0)) >= 0) {
                //如果客户不欠费，判断是否账户余额小于本次扣费总金额
                if (balance.compareTo(totalCost) < 0) {
                    //调用消息中心接口完成OBS扣费引起的余额不足通知推送
                    msgCenterService.balanLackceMessage(customerId);
                }
            } else {
                //客户欠费，则需要判断是否达到信用额度
                boolean isBelowCredit = isBelowCredit(customer, balance);
                boolean isBeyondRetentionTime = false;
                if (isBelowCredit) {
                    //如果达到信用额度，需要判断是否首次达到信用额度，首次达到信用额度，则需要发送首次达信用额度的消息
                    boolean isAlreadyOverCredit = isAlreadyOverCredit(customer);
                    if (!isAlreadyOverCredit) {
                        sendFirstReachCreditLimitMsg(customer, currentTime);
                    }

                    //因为已经超过信用额度，则需要判断是否超过保留时长
                    String recoveryTime = sysDataTreeService.getRecoveryTime();
                    int retentionTime = Integer.valueOf(recoveryTime);
                    Date overCreditTime = customer.getOverCreditTime();
                    if (overCreditTime != null) {
                        long timeSpan = currentTime.getTime() - overCreditTime.getTime();
                        isBeyondRetentionTime = timeSpan >= (retentionTime * 60 * 60 * 1000) ? true : false;//这里是大于等于号！

                        if (isBeyondRetentionTime) {
                            //欠费超过信用额度，发送欠费超保留时长消息，后置处理结束
                            sendOutRententionTimeMsg(customerId, currentTime);
                        } else {
                            sendInRententionTimeMsg(customerId);
                        }
                    }
                } else {
                    sendInRententionTimeMsg(customerId);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    private boolean isAlreadyOverCredit(Customer customer) {
        return customer.getOverCreditTime() != null ? true : false;
    }

    private boolean isBelowCredit(Customer customer, BigDecimal balance) {
        BigDecimal credit = customer.getCreditLines();
        BigDecimal negativeCredit = credit.negate();
        return balance.compareTo(negativeCredit) <= 0;
    }

    private void sendInRententionTimeMsg(String customerId) {
        JSONObject msg = new JSONObject();
        msg.put("customerId", customerId);
        log.info("欠费未超过保留时长消息发送开始");
        rabbitTemplate.send(EayunQueueConstant.QUEUE_ARREARAGE_IN_RENTENTIONTIME, msg.toJSONString());
    }

    private void sendOutRententionTimeMsg(String customerId, Date currentTime) {
        JSONObject msg = new JSONObject();
        msg.put("customerId", customerId);
        msg.put("time", currentTime.getTime());
        log.info("欠费超过保留时长消息发送开始");
        rabbitTemplate.send(EayunQueueConstant.QUEUE_ARREARAGE_OUT_RENTENTIONTIME, msg.toJSONString());
    }

    private void sendFirstReachCreditLimitMsg(Customer customer, Date currentTime) {
        customer.setOverCreditTime(currentTime);
        customerService.updateCustomer(customer);
        JSONObject json = new JSONObject();
        json.put("customerId", customer.getCusId());
        json.put("time", currentTime.getTime());

        log.info("欠费首次达信用额度消息发送开始");
        rabbitTemplate.send(EayunQueueConstant.QUEUE_ARREARAGE_REACH_CREDITLIMIT, json.toJSONString());
    }

}

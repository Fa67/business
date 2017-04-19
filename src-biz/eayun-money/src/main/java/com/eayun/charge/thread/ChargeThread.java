package com.eayun.charge.thread;

import com.alibaba.fastjson.JSONObject;
import com.eayun.charge.bean.ChargeConstant;
import com.eayun.charge.model.ChargeRecord;
import com.eayun.charge.model.ObsStatsBean;
import com.eayun.charge.service.ChargeRecordService;
import com.eayun.charge.service.ChargeService;
import com.eayun.common.constant.EayunQueueConstant;
import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.template.EayunRabbitTemplate;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.TransactionHookUtil;
import com.eayun.costcenter.model.MoneyAccount;
import com.eayun.costcenter.service.AccountOverviewService;
import com.eayun.customer.model.Customer;
import com.eayun.customer.serivce.CustomerService;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.syssetup.service.SysDataTreeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 后付费资源（除OBS外）计费线程
 *
 * @Filename: ChargeJob.java
 * @Description:
 * @Version: 1.0
 * @Author: zhangfan
 * @Email: fan.zhang@eayun.com
 * @History:<br> <li>Date: 2016年8月2日</li>
 * <li>Version: 1.0</li>
 * <li>Content: create</li>
 */
public class ChargeThread implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ChargeThread.class);
    private EayunRabbitTemplate rabbitTemplate;
    private AccountOverviewService accountOverviewService;
    private CustomerService customerService;
    private ChargeRecordService chargeRecordService;
    private ChargeService chargeService;
    private MessageCenterService msgCenterService;
    private SysDataTreeService sysDataTreeService;
    private String customerId;
    private Date currentTime;
    private MongoTemplate mongoTemplate;

    public ChargeThread(EayunRabbitTemplate rabbitTemplate, AccountOverviewService accountOverviewService,
                        CustomerService customerService, ChargeRecordService chargeRecordService,
                        ChargeService chargeService, MessageCenterService msgCenterService, SysDataTreeService sysDataTreeService,
                        MongoTemplate mongoTemplate, String cusId, Date currentTime) {
        this.rabbitTemplate = rabbitTemplate;
        this.accountOverviewService = accountOverviewService;
        this.customerService = customerService;
        this.chargeRecordService = chargeRecordService;
        this.customerId = cusId;
        this.currentTime = currentTime;
        this.chargeService = chargeService;
        this.msgCenterService = msgCenterService;
        this.sysDataTreeService = sysDataTreeService;
        this.mongoTemplate = mongoTemplate;
    }


    @Override
    public void run() {
        log.info("客户[" + customerId + "]后付费资源计费开始");
        try {
            //一、判断客户是否有后付费资源，如果没有，结束后付费资源计费
            List<ChargeRecord> chargeRecordList = chargeRecordService.getAllValidChargeRecordByCusId(customerId, currentTime);
            if (chargeRecordList.isEmpty()) {
                return;
            }
            //二、如果客户有后付费资源，则先进行前置条件检查——客户是否被冻结，客户是否是否已欠费或是否超过信用额度
            Customer customer = customerService.findCustomerById(customerId);
            if(customer==null){
                log.error("根据ID["+customerId+"]无法找到客户");
                return ;
            }
            Boolean isCustomerBlocked = customer.getIsBlocked();
            if(isCustomerBlocked != null && isCustomerBlocked.booleanValue()){
                //客户被冻结，即：该字段不为null，并且isCustomerBlocked=true，此时不计费。
                log.info("客户["+customerId+"]被冻结，本次计费不执行，结束");
                return;
            }
            boolean isPreconditionPassed = checkPrecondition(customer);
            //三、客户通过检查，可以正常计费
            if (isPreconditionPassed) {
                BigDecimal totalCost = new BigDecimal(0.0);
                for (ChargeRecord chargeRecord : chargeRecordList) {
                    //调用扣费service接口，根据计费清单记录，完成对一个资源的扣费，并得到扣费金额
                    try {
                        BigDecimal cost = chargeService.doCharge(currentTime, chargeRecord);
                        //累加客户每一条计费清单的金额，得到本次本客户的总扣费
                        totalCost = totalCost.add(cost);
                    } catch (Exception e) {
                        JSONObject json = (JSONObject) JSONObject.toJSON(chargeRecord);
                        json.put("timestamp", new Date());
                        json.put("exception",e.getMessage());
                        mongoTemplate.insert(json, MongoCollectionName.LOG_CHARGE_FAILED);
                        log.error("类型为[" + chargeRecord.getResourceType() + "]的资源[" + chargeRecord.getResourceId() + "]计费失败", e);
                    }
                }
                //四、得到本期计费客户扣费的总金额后，进行后置条件检查（含发送MSG到MQ）
                checkPostcondition(customer, totalCost);
//                sendChargeDoneMsg(customerId, currentTime);
            } else {
                return;
            }
        } catch (Exception e) {
            JSONObject json = new JSONObject();
            json.put("customerId", customerId);
            json.put("timestamp",new Date());
            json.put("exception",e.getMessage());
            mongoTemplate.insert(json, MongoCollectionName.LOG_CHARGE_FAILED);
            log.error("客户[" + customerId + "]后付费资源计费异常", e);
        } finally {
            //如果客户云资源线程结束，必须发消息通知OBS计费，告知计费已结束。
            sendChargeDoneMsg(customerId, currentTime);
        }

    }

    private void sendChargeDoneMsg(String customerId, Date currentTime) {
        Date beginTime = DateUtil.addDay(currentTime, new int[]{0,0,0,-1});
        ObsStatsBean obsStatsBean=new ObsStatsBean();
        obsStatsBean.setChargeFrom(beginTime);
        obsStatsBean.setChargeTo(currentTime);
        obsStatsBean.setCusId(customerId);
        obsStatsBean.setStatsType(ChargeConstant.OBS_STATS_TYPE.CLOUD_RES_CHARGE_DONE);
        JSONObject json=new JSONObject();
        json=(JSONObject) JSONObject.toJSON(obsStatsBean);
        rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_OBS_GATHER_SUCCEED, json.toJSONString());
    }

    /**
     * 后置条件检查处理。
     *
     * @param customer
     * @param totalCost
     */
    private void checkPostcondition(Customer customer, BigDecimal totalCost) {
        log.info("计费后置条件检查");
        try {
            BigDecimal balance = accountOverviewService.getAccountInfo(customerId).getMoney();
            //判断客户是否已欠费
            if (balance.compareTo(new BigDecimal(0.0)) >= 0) {
                //如果客户不欠费，判断是否账户余额小于本次扣费总金额
                if (balance.compareTo(totalCost) < 0) {
                    //调用消息中心接口完成余额不足通知推送
                    msgCenterService.balanLackceMessage(customerId);
                }
            } else {
                //客户欠费，则需要判断是否达到信用额度
                boolean isBelowCredit = isBelowCredit(customer, balance);
                boolean isBeyondRetentionTime = false;
                if(isBelowCredit){
                    //如果达到信用额度，需要判断是否首次达到信用额度，首次达到信用额度，则需要发送首次达信用额度的消息
                    boolean isAlreadyOverCredit = isAlreadyOverCredit(customer);
                    if (!isAlreadyOverCredit) {
                        sendFirstReachCreditLimitMsg(customer);
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
                            sendOutRententionTimeMsg();
                        } else {
                            sendInRententionTimeMsg();
                        }
                    }
                }else {
                    sendInRententionTimeMsg();
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    private void sendFirstReachCreditLimitMsg(Customer customer) {
        customer.setOverCreditTime(currentTime);
        customerService.updateCustomer(customer);
        JSONObject json = new JSONObject();
        json.put("customerId",customerId);
        json.put("time", currentTime.getTime());

        log.info("欠费首次达信用额度消息发送开始");
        rabbitTemplate.send(EayunQueueConstant.QUEUE_ARREARAGE_REACH_CREDITLIMIT, json.toJSONString());
    }

    private boolean isAlreadyOverCredit(Customer customer) {
        return customer.getOverCreditTime() != null ? true : false;
    }

    private boolean checkPrecondition(Customer customer) {
        log.info("计费前置条件检查");
        //0.flag表示是否通过前置条件检查
        boolean flag = false;
        //1.获取客户账户余额，判断是否欠费
        try {
            MoneyAccount account = accountOverviewService.getAccountInfo(customerId);
            BigDecimal balance = account.getMoney();
            if (balance.compareTo(new BigDecimal(0.0)) >= 0) {
                flag = true;
            } else {
                //已欠费的话，需要判断是否欠费达到信用额度(取出的信用额度为正数，需要获取一下负数) && 超过保留时长
                boolean isBelowCredit = isBelowCredit(customer, balance);
                boolean isBeyondRetentionTime = false;
                if(isBelowCredit){
                    //如果超过信用额度，需要判断是否达到保留时长
                    String recoveryTime = sysDataTreeService.getRecoveryTime();
                    int retentionTime = Integer.valueOf(recoveryTime);
                    Date overCreditTime = customer.getOverCreditTime();
                    //如果欠费首次打到信用额度时间为null，则表示还没有达到信用额度
                    if (overCreditTime != null) {
                        long timeSpan = currentTime.getTime() - overCreditTime.getTime();
                        isBeyondRetentionTime =  timeSpan > (retentionTime * 60 * 60 * 1000) ? true : false;//这里是大于号

                        if(isBeyondRetentionTime){
                            //欠费超过信用额度，发送欠费超保留时长消息
                            sendOutRententionTimeMsg();
                        }
                    }
                }else{
                    //欠费未超过信用额度，则发送欠费保留时长内消息
                    sendInRententionTimeMsg();
                }

                //如果既欠费又超过保留时长，则未通过前置条件检查
                flag = !(isBelowCredit && isBeyondRetentionTime);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return flag;
    }

    private void sendInRententionTimeMsg() {
        JSONObject msg = new JSONObject();
        msg.put("customerId", customerId);
        log.info("欠费未超过保留时长消息发送开始");
        rabbitTemplate.send(EayunQueueConstant.QUEUE_ARREARAGE_IN_RENTENTIONTIME, msg.toJSONString());
    }

    private void sendOutRententionTimeMsg() {
        JSONObject msg = new JSONObject();
        msg.put("customerId", customerId);
        msg.put("time", currentTime.getTime());
        log.info("欠费超过保留时长消息发送开始");
        rabbitTemplate.send(EayunQueueConstant.QUEUE_ARREARAGE_OUT_RENTENTIONTIME, msg.toJSONString());
    }

    private boolean isBelowCredit(Customer customer, BigDecimal balance) {
        BigDecimal credit = customer.getCreditLines();
        BigDecimal negativeCredit = credit.negate();
        return balance.compareTo(negativeCredit) <= 0;
    }

}

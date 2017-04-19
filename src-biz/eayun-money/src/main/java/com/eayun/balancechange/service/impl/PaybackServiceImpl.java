package com.eayun.balancechange.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.balancechange.service.PaybackService;
import com.eayun.costcenter.model.MoneyRecord;
import com.eayun.costcenter.service.CostReportService;

/**
 * 欠费记录补齐Service
 *
 * @Filename: PaybackServiceImpl.java
 * @Description:
 * @Version: 1.0
 * @Author: zhangfan
 * @Email: fan.zhang@eayun.com
 * @History:<br> <li>Date: 2016年8月12日</li>
 * <li>Version: 1.0</li>
 * <li>Content: create</li>
 */
@Transactional
@Service
public class PaybackServiceImpl implements PaybackService {

    private static final Logger log = LoggerFactory.getLogger(PaybackServiceImpl.class);

    @Autowired
    private CostReportService costReportService;

    @Override
    public void doPayback(String cusId, BigDecimal revenue) {
        log.info("开始对客户["+cusId+"]执行欠费记录补齐操作");
        try {
            //1.获取该客户在交易记录中的全部欠费记录列表
            List<MoneyRecord> arrearageRecordList = costReportService.getArrearsListByCusId(cusId);
            for(MoneyRecord moneyRecord: arrearageRecordList){
                //应付金额
                BigDecimal money = moneyRecord.getMoney();
                //实付金额
                BigDecimal realPay = moneyRecord.getMonRealPay();
                //欠费金额(负数)
                BigDecimal arrears = realPay.subtract(money);
                //如果充值金额足以补齐欠费金额，则应付金额 = 实付金额，支付状态为已支付。
                if(revenue.compareTo(arrears.negate())>=0){
                    moneyRecord.setMonRealPay(money);
                    moneyRecord.setPayState("1");
                    revenue = revenue.subtract(arrears.negate());
                }else {
                    //如果充值金额不足以补齐欠费金额，则新应付金额 = 旧应付金额 + 充值金额，支付状态还是已欠费
                    BigDecimal originRealPay = moneyRecord.getMonRealPay();
                    if(originRealPay==null){
                        originRealPay = BigDecimal.ZERO;
                    }
                    BigDecimal currentRealPay = originRealPay.add(revenue);
                    moneyRecord.setMonRealPay(currentRealPay);
                    costReportService.updateMoneyRecord(moneyRecord);
                    break;
                }
                costReportService.updateMoneyRecord(moneyRecord);
            }
        } catch (Exception e) {
            log.error("客户["+cusId+"]执行欠费记录补齐失败", e);
        }


    }
}

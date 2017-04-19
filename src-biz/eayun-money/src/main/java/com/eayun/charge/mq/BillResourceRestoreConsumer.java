package com.eayun.charge.mq;

import com.eayun.charge.bean.ChargeConstant;
import com.eayun.charge.model.ChargeRecord;
import com.eayun.charge.service.ChargeRecordService;
import com.eayun.charge.util.ChargeRecordUtil;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 监听资源在回收站中还原消息的MQ Consumer。
 *
 * @Filename: BillResourceRestoreConsumer.java
 * @Description:
 * @Version: 1.0
 * @Author: zhangfan
 * @Email: fan.zhang@eayun.com
 * @History:<br> <li>Date: 2016年8月30日</li>
 * <li>Version: 1.0</li>
 * <li>Content: create</li>
 */
@Transactional
@Component
public class BillResourceRestoreConsumer implements ChannelAwareMessageListener {
    private static final Logger log = LoggerFactory.getLogger(BillResourceRestoreConsumer.class);

    @Autowired
    private ChargeRecordService chargeRecordService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ChargeRecordUtil chargeRecordUtil;

    @Autowired
    private SysDataTreeService sysDataTreeService;

    @Override
    public void onMessage(Message msg, Channel channel) throws Exception {
        log.info("监听到资源在回收站中还原的消息");
        try {
            ChargeRecord chargeRecord = chargeRecordUtil.parseToObject(msg);
            //资源恢复，则先查找计费清单中已放入回收站该资源的记录，将计费清单记录置为生效，同时更改资源状态
            List<ChargeRecord> theRecordList = chargeRecordService.getSpecifiedChargeRecord(chargeRecord.getDatecenterId(), chargeRecord.getCusId(), chargeRecord.getResourceId(),null,"3");
            for(ChargeRecord cr : theRecordList){
                String cusId = cr.getCusId();
                Customer customer = customerService.findCustomerById(cusId);
                if(customer.getOverCreditTime()!=null){
                    boolean isBeyondRetentionTime = isBeyondRetentionTime(customer);
                    if(isBeyondRetentionTime){
                        //如果在回收站中恢复的时候客户已经欠费超过保留时长，则恢复后的资源应当是不计费的，并且资源状态正常，即isValid=0，resourceStatus=0.
                        cr.setIsValid(ChargeConstant.RECORD_INVALID);
                    }else{
                        //如果在回收站中恢复的时候客户欠费但是没超过保留时长，或者像下面，客户根本没欠费达到信用额度（有可能欠费没达到，有可能没欠费），所以也要置isValid=1，即正常计费。
                        cr.setIsValid(ChargeConstant.RECORD_VALID);
                    }
                }else {
                    cr.setIsValid(ChargeConstant.RECORD_VALID);
                }
                cr.setResourceStatus(ChargeConstant.RES_STATUS.NORMAL);
                cr.setChargeFrom(chargeRecord.getOpTime());
                chargeRecordService.updateChargeRecord(cr);
            }
        } catch (Exception e) {
            log.error("资源在回收站中还原的消息处理失败", e);
            chargeRecordUtil.doLog(msg, e, ChargeConstant.CHARGE_RECORD_OP_TYPE.RESTORE);
//            channel.basicNack(msg.getMessageProperties().getDeliveryTag(), false, true);
        } finally {
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
        }
    }

    private boolean isBeyondRetentionTime(Customer customer) {
        String recoveryTime = sysDataTreeService.getRecoveryTime();
        int retentionTime = Integer.valueOf(recoveryTime);
        Date overCreditTime = customer.getOverCreditTime();
        Date currentTime = new Date();

        if (overCreditTime != null) {
            long timeSpan = currentTime.getTime() - overCreditTime.getTime();
            return timeSpan >= (retentionTime * 60 * 60 * 1000) ? true : false;
        }
        return false;
    }
}

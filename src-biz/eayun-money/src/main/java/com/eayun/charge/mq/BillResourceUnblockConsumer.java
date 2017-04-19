package com.eayun.charge.mq;

import com.eayun.charge.bean.ChargeConstant;
import com.eayun.charge.model.ChargeRecord;
import com.eayun.charge.service.ChargeRecordService;
import com.eayun.charge.util.ChargeRecordUtil;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 监听客户解冻消息的MQ Consumer。
 *
 * @Filename: BillResourceUnblockConsumer.java
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
public class BillResourceUnblockConsumer implements ChannelAwareMessageListener {
    private static final Logger log = LoggerFactory.getLogger(BillResourceUnblockConsumer.class);

    @Autowired
    private ChargeRecordService chargeRecordService;

    @Autowired
    private ChargeRecordUtil chargeRecordUtil;

    @Override
    public void onMessage(Message msg, Channel channel) throws Exception {
        log.info("监听到客户解冻消息");
        try {
            ChargeRecord chargeRecord = chargeRecordUtil.parseToObject(msg);
            String cusId = chargeRecord.getCusId();
            Date opTime = chargeRecord.getOpTime();
            chargeRecordService.updateCusAllChargeFromTime(cusId, opTime);
        } catch (Exception e) {
            log.error("客户解冻消息处理失败", e);
            chargeRecordUtil.doLog(msg, e, ChargeConstant.CHARGE_RECORD_OP_TYPE.UNBLOCK);
//            channel.basicNack(msg.getMessageProperties().getDeliveryTag(), false, true);
        } finally {
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
        }
    }
}

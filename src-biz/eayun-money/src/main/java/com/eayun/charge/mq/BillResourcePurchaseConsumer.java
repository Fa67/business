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

/**
 * 监听新购资源消息的MQ Consumer，触发创建计费清单功能。
 *
 * @Filename: BillResourcePurchaseConsumer.java
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
public class BillResourcePurchaseConsumer implements ChannelAwareMessageListener {
    private static final Logger log = LoggerFactory.getLogger(BillResourcePurchaseConsumer.class);

    @Autowired
    private ChargeRecordService chargeRecordService;

    @Autowired
    private ChargeRecordUtil chargeRecordUtil;

    @Override
    public void onMessage(Message msg, Channel channel) throws Exception {
        log.info("监听到新购资源消息");
        try {
            ChargeRecord chargeRecord = chargeRecordUtil.parseToObject(msg);

            chargeRecord.setResourceStatus(ChargeConstant.RES_STATUS.NORMAL);//资源状态置为0-正常
            chargeRecord.setIsValid(ChargeConstant.RECORD_VALID);//记录是否生效1-生效
            chargeRecordService.addChargeRecord(chargeRecord);
        } catch (Exception e) {
            log.error("新购资源消息处理失败", e);
            chargeRecordUtil.doLog(msg, e, ChargeConstant.CHARGE_RECORD_OP_TYPE.PURCHASE);
//            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        } finally {
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
        }
    }
}

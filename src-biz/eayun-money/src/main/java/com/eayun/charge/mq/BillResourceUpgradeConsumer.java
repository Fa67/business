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

import java.util.List;

/**
 * 监听变配资源消息的MQ Consumer，触发创建计费清单功能。
 *
 * @Filename: BillResourceUpgradeConsumer.java
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
public class BillResourceUpgradeConsumer implements ChannelAwareMessageListener {
    private static final Logger log = LoggerFactory.getLogger(BillResourceUpgradeConsumer.class);

    @Autowired
    private ChargeRecordService chargeRecordService;

    @Autowired
    private ChargeRecordUtil chargeRecordUtil;

    @Override
    public void onMessage(Message msg, Channel channel) throws Exception {
        log.info("监听到变配资源消息");
        try {
            ChargeRecord chargeRecord = chargeRecordUtil.parseToObject(msg);

            //获取变配前的计费清单记录并更新资源状态、资源状态变更时间
            List<ChargeRecord> oldRecordList = chargeRecordService.getSpecifiedChargeRecord(chargeRecord.getDatecenterId(), chargeRecord.getCusId(), chargeRecord.getResourceId(),"1", "0");
            for(ChargeRecord cr:oldRecordList){
                cr.setResourceStatus(ChargeConstant.RES_STATUS.UPGRADED);//2-已变配
                cr.setChangeTime(chargeRecord.getChargeFrom());//变配操作时间
                cr.setIsValid(ChargeConstant.RECORD_INVALID);//变配后，该记录失效，不在计费。
                chargeRecordService.updateChargeRecord(cr);
            }

            //将变配后的资源，插入一条新的计费清单记录
            chargeRecord.setResourceStatus(ChargeConstant.RES_STATUS.NORMAL);
            chargeRecord.setIsValid(ChargeConstant.RECORD_VALID);
            chargeRecordService.addChargeRecord(chargeRecord);
        } catch (Exception e) {
            log.error("变配资源消息处理失败", e);
            chargeRecordUtil.doLog(msg, e, ChargeConstant.CHARGE_RECORD_OP_TYPE.UPGRADE);
//            channel.basicNack(msg.getMessageProperties().getDeliveryTag(), false, true);
        } finally {
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
        }
    }
}

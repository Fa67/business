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
 * 监听恢复资源服务消息的MQ Consumer。
 *
 * @Filename: BillResourceRecoverConsumer.java
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
public class BillResourceRecoverConsumer implements ChannelAwareMessageListener {
    private static final Logger log = LoggerFactory.getLogger(BillResourceRecoverConsumer.class);

    @Autowired
    private ChargeRecordService chargeRecordService;

    @Autowired
    private ChargeRecordUtil chargeRecordUtil;

    @Override
    public void onMessage(Message msg, Channel channel) throws Exception {
        log.info("监听到恢复资源服务消息");
        try {
            ChargeRecord chargeRecord = chargeRecordUtil.parseToObject(msg);

            //获取记录无效的记录
            List<ChargeRecord> theRecordList = chargeRecordService.getSpecifiedChargeRecord(chargeRecord.getDatecenterId(), chargeRecord.getCusId(), chargeRecord.getResourceId(),"0","0");
            for(ChargeRecord cr : theRecordList){
                cr.setIsValid(ChargeConstant.RECORD_VALID);
                cr.setChargeFrom(chargeRecord.getOpTime());//资源恢复后，更新记录的开始计费时间=恢复操作的操作时间
                chargeRecordService.updateChargeRecord(cr);
            }
        } catch (Exception e) {
            log.error("恢复资源服务消息处理失败", e);
            chargeRecordUtil.doLog(msg, e, ChargeConstant.CHARGE_RECORD_OP_TYPE.RECOVER);
//            channel.basicNack(msg.getMessageProperties().getDeliveryTag(), false, true);
        } finally {
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
        }
    }
}

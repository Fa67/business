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
 * 监听资源放入回收站资源消息的MQ Consumer。
 *
 * @Filename: BillResourceRecycleConsumer.java
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
public class BillResourceRecycleConsumer implements ChannelAwareMessageListener {
    private static final Logger log = LoggerFactory.getLogger(BillResourceRecycleConsumer.class);

    @Autowired
    private ChargeRecordService chargeRecordService;

    @Autowired
    private ChargeRecordUtil chargeRecordUtil;

    @Override
    public void onMessage(Message msg, Channel channel) throws Exception {
        log.info("监听到资源放入回收站的消息");
        try {
            ChargeRecord chargeRecord = chargeRecordUtil.parseToObject(msg);
            //资源放入回收站，需要查询的计费状态从1.0，调整为*.0。
            List<ChargeRecord> theRecordList = chargeRecordService.getSpecifiedChargeRecord(chargeRecord.getDatecenterId(), chargeRecord.getCusId(), chargeRecord.getResourceId(), null, "0");
            for(ChargeRecord cr : theRecordList){
                cr.setResourceStatus(ChargeConstant.RES_STATUS.RECYCLED);
                cr.setChangeTime(chargeRecord.getOpTime());
                if(chargeRecord.getResourceName()!=null){
                    cr.setResourceName(chargeRecord.getResourceName());
                }
                chargeRecordService.updateChargeRecord(cr);
            }
        } catch (Exception e) {
            log.error("资源放入回收站的消息处理失败", e);
            chargeRecordUtil.doLog(msg, e, ChargeConstant.CHARGE_RECORD_OP_TYPE.RECYCLE);
//            channel.basicNack(msg.getMessageProperties().getDeliveryTag(), false, true);
        } finally {
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
        }
    }
}

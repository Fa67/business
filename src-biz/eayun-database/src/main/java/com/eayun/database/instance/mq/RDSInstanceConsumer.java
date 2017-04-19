package com.eayun.database.instance.mq;

import java.io.IOException;

import com.eayun.common.exception.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.OrderType;
import com.eayun.common.util.StringUtil;
import com.eayun.database.instance.model.CloudOrderRDSInstance;
import com.eayun.database.instance.service.CloudOrderRDSInstanceService;
import com.eayun.database.instance.service.RDSInstanceService;
import com.eayun.order.service.OrderService;
import com.rabbitmq.client.Channel;

@Service("rdsInstanceConsumer")
@Transactional
public class RDSInstanceConsumer implements ChannelAwareMessageListener {
	private static final Logger log = LoggerFactory
            .getLogger(RDSInstanceConsumer.class);
	
	@Autowired
    private OrderService orderService;
	@Autowired
	private RDSInstanceService rdsInstanceService;
	@Autowired
	private CloudOrderRDSInstanceService cloudOrderRDSInstanceService;
	
	@Override
    @Transactional(noRollbackFor=AppException.class)
	public void onMessage(Message message, Channel channel) throws IOException {
		try {
            String msgBody = new String(message.getBody());
            JSONObject json = JSONObject.parseObject(msgBody);
            String orderNo = json.getString("orderNo");
            log.info("orderNo:"+orderNo+"************");
            CloudOrderRDSInstance cloudOrderRdsInstance = new CloudOrderRDSInstance();
            /**
             * 1：校验消息队列是否获取订单编号
             * 2：校验资源order表中是否有对应的订单配置数据
             */
            if (!StringUtil.isEmpty(orderNo)) {
            	cloudOrderRdsInstance = cloudOrderRDSInstanceService.getRdsOrderByOrderNo(orderNo);
            	/**
                 * 是否获取了资源order配置参数
                 */
            	if(StringUtil.isEmpty(cloudOrderRdsInstance.getOrderRdsId())){
            		log.error("order.pay.rdsInstance 队列中订单编号为：" + orderNo + "的订单，在cloudorder_rdsinstance表中找不到对应数据");
                    orderService.completeOrder(orderNo, false, null);
            	}else{
            		if(OrderType.NEW.equals(cloudOrderRdsInstance.getOrderType())){
            			rdsInstanceService.createRdsInstance(cloudOrderRdsInstance);
                    }else if(OrderType.UPGRADE.equals(cloudOrderRdsInstance.getOrderType())){
                    	rdsInstanceService.resizeRdsInstance(cloudOrderRdsInstance);
    				}
            	}
            } else {
                log.error("order.pay.rdsInstance 队列中获取的订单编号为空");
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            log.error(e.toString(),e);
        }
	}

}

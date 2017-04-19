package com.eayun.virtualization.mq;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.util.StringUtil;
import com.eayun.order.service.OrderService;
import com.eayun.virtualization.model.CloudOrderFloatIp;
import com.eayun.virtualization.service.CloudFloatIpService;
import com.rabbitmq.client.Channel;

/**
 * Created by Administrator on 2016/8/18.
 */
@Service
public class FloatIpConsumer implements ChannelAwareMessageListener {
    private static final Logger log = LoggerFactory
            .getLogger(LdPoolConsumer.class);
    @Autowired
    private CloudFloatIpService floatIpService;
    @Autowired
    private OrderService orderService;
    @Override
    public void onMessage(Message msg, Channel channel) throws IOException {
        try {
            String msgBody = new String(msg.getBody());
            JSONObject json = JSONObject.parseObject(msgBody);
            String orderNo = json.getString("orderNo");
            log.info("orderNo:"+orderNo+"************");
            CloudOrderFloatIp cloudOrderFloatIp = new CloudOrderFloatIp();
            /**
             * 1：校验消息队列是否获取订单编号
             * 2：校验资源order表中是否有对应的订单配置数据
             */
            if (!StringUtil.isEmpty(orderNo)) {
            	cloudOrderFloatIp = floatIpService.getCloudOrderByOrderNo(orderNo);
            	/**
                 * 是否获取了资源order配置参数
                 */
            	if(StringUtil.isEmpty(cloudOrderFloatIp.getCofId())){
            		log.error("order.pay.floatip 队列中订单编号为：" + orderNo + "的订单，在cloudorder_floatip表中找不到对应数据");
                    orderService.completeOrder(orderNo, false, null);
            	}else{
            		floatIpService.addFloatIp(orderNo, true);
            	}
            } else {
                log.error("order.pay.floatip 队列中获取的订单编号为空");
                channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
                return;
            }
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
            log.error(e.toString(),e);
        }
    }
    
}

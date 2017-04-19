package com.eayun.virtualization.mq;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.EayunQueueConstant;
import com.eayun.common.constant.OrderType;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.util.StringUtil;
import com.eayun.order.service.OrderService;
import com.eayun.virtualization.model.CloudOrderLdPool;
import com.eayun.virtualization.service.CloudOrderLdPoolService;
import com.eayun.virtualization.service.PoolService;
import com.rabbitmq.client.Channel;

@Service
public class LdPoolConsumer implements ChannelAwareMessageListener {
    private static final Logger log = LoggerFactory
            .getLogger(LdPoolConsumer.class);
    @Autowired
    private PoolService poolService;
    @Autowired
    private CloudOrderLdPoolService orderPoolService;
    @Autowired
    private OrderService orderService;
    @Override
    public void onMessage(Message msg,Channel channel) throws IOException{
        try {
            String msgBody = new String(msg.getBody());
            JSONObject json = JSONObject.parseObject(msgBody);
            String orderNo = json.getString("orderNo");
            log.info("orderNo:"+orderNo+"************");
            CloudOrderLdPool cloudOrderPool = new CloudOrderLdPool();
            /**
             * 1：校验消息队列是否获取订单编号
             * 2：校验资源order表中是否有对应的订单配置数据
             */
            if (!StringUtil.isEmpty(orderNo)) {
            	cloudOrderPool = orderPoolService.getOrderLdPoolByOrderNo(orderNo);
            	/**
                 * 是否获取了资源order配置参数
                 */
                if(StringUtil.isEmpty(cloudOrderPool.getOrderPoolId())){
                	log.error("order.pay.balancer 队列中订单编号为：" + orderNo + "的订单，在cloudorder_ldpool表中找不到对应数据");
                    orderService.completeOrder(orderNo, false, null);
                }else {
                	SessionUserInfo sessionUser = new SessionUserInfo();
                    if(OrderType.NEW.equals(cloudOrderPool.getOrderType())){
                        poolService.createBalancer(cloudOrderPool, sessionUser);
                    }
                    else if(OrderType.UPGRADE.equals(cloudOrderPool.getOrderType())){
                        poolService.updateBalancer(cloudOrderPool);
                    }
                }
            } else {
                log.error("order.pay.balancer 队列中获取的订单编号为空");
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

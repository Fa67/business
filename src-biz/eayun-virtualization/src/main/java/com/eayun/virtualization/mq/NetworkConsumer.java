package com.eayun.virtualization.mq;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.EayunQueueConstant;
import com.eayun.common.constant.OrderType;
import com.eayun.common.util.StringUtil;
import com.eayun.order.service.OrderService;
import com.eayun.virtualization.model.CloudOrderNetWork;
import com.eayun.virtualization.service.CloudOrderNetWorkService;
import com.eayun.virtualization.service.NetWorkService;
import com.rabbitmq.client.Channel;

@Service
public class NetworkConsumer implements ChannelAwareMessageListener {
    private static final Logger log = LoggerFactory
            .getLogger(NetworkConsumer.class);
    @Autowired
    private NetWorkService networkService;
    @Autowired
    private CloudOrderNetWorkService orderNetworkService;
    @Autowired
    private OrderService orderService;
    @Override
    public void onMessage(Message msg,Channel channel) throws IOException{
        try{
            String msgBody = new String(msg.getBody());
            JSONObject json = JSONObject.parseObject(msgBody);
            String orderNo = json.getString("orderNo");
            log.info("orderNo:"+orderNo+"************");
            CloudOrderNetWork orderNetwork = new CloudOrderNetWork();
            /**
             * 1：校验消息队列是否获取订单编号
             * 2：校验资源order表中是否有对应的订单配置数据
             */
            if (!StringUtil.isEmpty(orderNo)) {
            	orderNetwork = orderNetworkService.getOrderNetWorkByOrderNo(orderNo);
            	/**
            	 * 是否获取了资源order配置参数
            	 */
            	if(StringUtil.isEmpty(orderNetwork.getOrderNetWorkId())){
            		log.error("order.pay.vpc 队列中订单编号为：" + orderNo + "的订单，在cloudorder_network表中找不到对应数据");
                    orderService.completeOrder(orderNo, false, null);
            	}else {
            		if(OrderType.NEW.equals(orderNetwork.getOrderType())){
                        networkService.addNetWork(orderNetwork);
                    }
                    else if(OrderType.UPGRADE.equals(orderNetwork.getOrderType())){
                        networkService.updateNetWork(orderNetwork);
                    }
            	}
            } else {
                log.error("order.pay.vpc 队列中获取的订单编号为空");
                channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
                return;
            }
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
        } catch(Exception e) {
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
            log.error(e.toString(),e);
        }
    }
}

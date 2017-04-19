package com.eayun.virtualization.mq;

import java.io.IOException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.EayunQueueConstant;
import com.eayun.virtualization.service.ResourceDisposeService;
import com.rabbitmq.client.Channel;
/**
 * 后付费资源欠费(未达到保留时长)Consumer
 * @author xiangyu.cao@eayun.com
 *
 */
@Component
public class ArrearageInRententionTimeConsumer  implements ChannelAwareMessageListener{
	private static final Logger log = LoggerFactory
			.getLogger(ArrearageInRententionTimeConsumer.class);
	@Autowired
	private ResourceDisposeService resourceDisposeService;
	
	@Override
	public void onMessage(Message msg, Channel channel) throws Exception {
		log.info("后付费资源欠费(未达到保留时长)Consumer");
		try {
			String msgBody = new String(msg.getBody());
			JSONObject json = JSONObject.parseObject(msgBody);
			String cusId = json.getString("customerId");
			resourceDisposeService.inRententionTime(cusId);
			channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
		} catch (Exception e) {
			log.error("后付费资源欠费(保留时长内)处理失败", e);
			channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
		}
	}
}

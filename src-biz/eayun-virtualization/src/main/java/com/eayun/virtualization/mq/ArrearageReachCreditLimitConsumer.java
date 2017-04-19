package com.eayun.virtualization.mq;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.virtualization.service.ResourceDisposeService;
import com.rabbitmq.client.Channel;

/**
 * 后付费资源达到信用额度Consumer
 * @author xiangyu.cao@eayun.com
 *
 */
@Component
public class ArrearageReachCreditLimitConsumer implements ChannelAwareMessageListener{
	private static final Logger log = LoggerFactory
			.getLogger(ArrearageReachCreditLimitConsumer.class);
	@Autowired
	private ResourceDisposeService resourceDisposeService;
	@Override
	public void onMessage(Message msg, Channel channel) throws Exception {
		try {
			log.info("后付费资源欠费达到信用额度发送消息");
			String msgBody = new String(msg.getBody());
			JSONObject json = JSONObject.parseObject(msgBody);
			String cusId = json.getString("customerId");
			long longTime=json.getLongValue("time");
			Date time=new Date(longTime);
			resourceDisposeService.sendMessageForReachCreditlimit(cusId,time);
			channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
		} catch (Exception e) {
			log.error("后付费资源欠费达到信用额度发送消息失败", e);
			channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
		}
	}

}

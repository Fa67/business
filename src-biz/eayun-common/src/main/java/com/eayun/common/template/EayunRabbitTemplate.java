package com.eayun.common.template;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ConfirmCallback;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

@Service
public class EayunRabbitTemplate {
	@Autowired
	private ConnectionFactory connectionFactory;
	@Autowired
	private RetryTemplate retryTemplate;
	@Autowired
	private MessageConverter jsonMessageConverter;
	
	public void send(String queue,String msg ,ConfirmCallback callback){
		RabbitTemplate rt = new RabbitTemplate(connectionFactory);
		rt.setRetryTemplate(retryTemplate);
		rt.setMessageConverter(jsonMessageConverter);
		rt.setRoutingKey(queue);
		rt.setQueue(queue);
		rt.setConfirmCallback(callback);
		
		rt.convertAndSend(queue, msg);
		
	} 
	
	public void send(String queue,String msg){
		RabbitTemplate rt = new RabbitTemplate(connectionFactory);
		rt.setRetryTemplate(retryTemplate);
//		rt.setmessageconverter(jsonmessageconverter);
		rt.setRoutingKey(queue);
		rt.setQueue(queue);
		
		rt.convertAndSend(queue, msg);
		
	} 
}
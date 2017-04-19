package com.eayun.order.mq;

import java.util.HashMap;
import java.util.Map;

import org.apache.xerces.dom.PSVIDOMImplementationImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.ResourceType;
import com.eayun.common.exception.AppException;
import com.eayun.common.template.EayunRabbitTemplate;
import com.eayun.common.util.StringUtil;

@Component
public class OrderToResourceProducer {
	
	private static final Logger log = LoggerFactory.getLogger(OrderToResourceProducer.class);
	
	public static final String QUEUE_PREFIX = "order.pay.";
	
	public static final String RESOURCE_VM = "vm";
	
	public static final String RESOURCE_VOLUME = "volume";
	
	public static final String RESOURCE_VPC = "vpc";
	
	public static final String RESOURCE_BALANCER = "balancer";
	
	public static final String RESOURCE_FLOATIP = "floatip";
	
	public static final String RESOURCE_VPN = "vpn";
	
	public static final String RESOURCE_RDS = "rdsInstance";
	
	public static final String QUEUE_RESOURCE_RENEWAL = "RESOURCE_RENEW";
	
	
	@Autowired
	private EayunRabbitTemplate eayunRabbitTemplate;
	
	/**
	 * 新建资源或者升级
	 * @param orderNo 订单编号
	 * @param orderType 订单类型
	 * @param resourceType 资源类型
	 */
	public void sendToNewOrUpgrade(String orderNo, String orderType, String resourceType) {
		String content = null;
		Map<String, String> dataMap = new HashMap<String, String>();
		dataMap.put("orderNo", orderNo);
		dataMap.put("orderType", orderType);
		content = JSON.toJSONString(dataMap);
		try {
			send(getQueueName(resourceType), content);
		} catch (AppException e) {
			throw e;
		}
	}
	
	/**
	 * 续费
	 * @param orderNo 订单编号
	 * @param cusId 客户ID
	 * @param actPerson 当前登录用户名
	 * @param params 业务参数
	 */
	public void sendToRenewal(String orderNo, String cusId, String actPerson, String params) {
		String content = null;
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("orderNo", orderNo);
		dataMap.put("cusId", cusId);
		dataMap.put("actPerson", actPerson);
		if (!StringUtil.isEmpty(params)) {
			dataMap.put("params", JSON.parseObject(params));
		}
		
		content = JSON.toJSONString(dataMap);
		try {
			send(QUEUE_RESOURCE_RENEWAL, content);
		} catch (AppException e) {
			throw e;
		}
	}
	
	public void send(String queueName, String content) {
		if (queueName != null && content != null) {
			log.info("发送消息队列【{}】：{}", queueName, content);
			eayunRabbitTemplate.send(queueName, content);
		} else {
			throw new AppException("发送消息队列"+queueName+"异常");
		}
	}
	
	private String getQueueName(String resourceType) {
		if (resourceType.equals(ResourceType.VM)) {
			return QUEUE_PREFIX + RESOURCE_VM;
		} else if (resourceType.equals(ResourceType.VDISK)) {
			return QUEUE_PREFIX + RESOURCE_VOLUME;
		} else if (resourceType.equals(ResourceType.NETWORK)) {
			return QUEUE_PREFIX + RESOURCE_VPC;
		} else if (resourceType.equals(ResourceType.QUOTAPOOL)) {
			return QUEUE_PREFIX + RESOURCE_BALANCER;
		} else if (resourceType.equals(ResourceType.FLOATIP)) {
			return QUEUE_PREFIX + RESOURCE_FLOATIP;
		} else if (resourceType.equals(ResourceType.VPN)) {
			return QUEUE_PREFIX + RESOURCE_VPN;
		} else if(resourceType.equals(ResourceType.RDS)){
			return QUEUE_PREFIX + RESOURCE_RDS;
		}else {
			return null;
		}
	}
	
}

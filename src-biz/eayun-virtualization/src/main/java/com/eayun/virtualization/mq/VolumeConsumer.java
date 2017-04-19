package com.eayun.virtualization.mq;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.OrderType;
import com.eayun.order.service.OrderService;
import com.eayun.virtualization.model.CloudOrderVolume;
import com.eayun.virtualization.service.SnapshotService;
import com.eayun.virtualization.service.VolumeOrderService;
import com.eayun.virtualization.service.VolumeService;
import com.rabbitmq.client.Channel;

@Transactional
@Component
public class VolumeConsumer implements ChannelAwareMessageListener{
	private static final Logger log = LoggerFactory.getLogger(VolumeConsumer.class);
	
	@Autowired
	private VolumeService volService;
	@Autowired
	private SnapshotService snapService;
	@Autowired
	private VolumeOrderService volumeOrderService;
	@Autowired
	private OrderService orderService;
	
	@Override
	public void onMessage(Message msg,Channel channel) throws IOException{
		String orderNo=null;
		CloudOrderVolume orderVol=null;
		try{
			String msgBody = new String(msg.getBody());
			JSONObject json = JSONObject.parseObject(msgBody);
			orderNo = json.getString("orderNo");
			log.info("orderNo:"+orderNo+"************");
			
			orderVol = volumeOrderService.getVolOrderByOrderNo(orderNo);
			
			if(null != orderVol && !StringUtils.isEmpty(orderVol.getOrderVolId())){
				if(OrderType.NEW.equals(orderVol.getOrderType())){
					if(null==orderVol.getFromSnapId()){
						volService.addVolume(orderVol);
					}else{
						volService.addVolumeBySnapshot(orderVol);
					}
				}else if(OrderType.UPGRADE.equals(orderVol.getOrderType())){
					volService.largeVolume(orderVol);
				}
				
			}
			channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
		}catch(Exception e){
			log.error("order.pay.volume队列中订单编号为："+orderNo+e.getMessage(),e);
			channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
		}
	}
}

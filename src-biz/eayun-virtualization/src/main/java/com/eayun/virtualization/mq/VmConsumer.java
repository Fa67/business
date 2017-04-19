package com.eayun.virtualization.mq;

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
import com.eayun.common.exception.AppException;
import com.eayun.virtualization.model.CloudOrderVm;
import com.eayun.virtualization.model.CloudVm;
import com.eayun.virtualization.service.CloudOrderVmService;
import com.eayun.virtualization.service.VmService;
import com.rabbitmq.client.Channel;

@Transactional
@Component
public class VmConsumer implements ChannelAwareMessageListener{
	
	private static final Logger log = LoggerFactory.getLogger(VmConsumer.class);
	
	@Autowired
	private VmService vmService;
	@Autowired
	private CloudOrderVmService cloudOrderVmService;
	
	@Override
	@Transactional(noRollbackFor=AppException.class)
	public void onMessage(Message msg,Channel channel) throws Exception{
		String orderNo =null;
		CloudOrderVm orderVm = null;
		try{
			String msgBody = new String(msg.getBody());
			JSONObject json = JSONObject.parseObject(msgBody);
			orderNo = json.getString("orderNo");
			log.info("orderNo:"+orderNo+"************");
			orderVm = cloudOrderVmService.getByOrder(orderNo);
			
			if(null != orderVm && !StringUtils.isEmpty(orderVm.getOrdervmId())){
				if(OrderType.NEW.equals(orderVm.getOrderType())){
					vmService.createVm(orderVm);
				}
				else if(OrderType.UPGRADE.equals(orderVm.getOrderType())){
					CloudVm cloudVm = new CloudVm();
					
					cloudVm.setDcId(orderVm.getDcId());
					cloudVm.setPrjId(orderVm.getPrjId());
					cloudVm.setVmId(orderVm.getVmId());
					cloudVm.setCpus(orderVm.getCpu());
					cloudVm.setRams(orderVm.getRam());
					cloudVm.setDisks(orderVm.getDisk());
					cloudVm.setOrderNo(orderVm.getOrderNo());
					cloudVm.setCusId(orderVm.getCusId());
					cloudVm.setSysType(orderVm.getSysType());
					cloudVm.setPayType(orderVm.getPayType());
					cloudVm.setVmName(orderVm.getVmName());
					
					vmService.resizeVm(cloudVm);
				}
			}
			channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
		}catch(Exception e){
			log.error("order.pay.vm队列中订单编号为："+orderNo+e.getMessage(),e);
			channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
			throw e;
		}
	}
}

package com.eayun.virtualization.mq;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.zk.DistributedLockBean;
import com.eayun.common.zk.DistributedLockService;
import com.eayun.common.zk.LockService;
import com.eayun.virtualization.service.ResourceDisposeService;
import com.rabbitmq.client.Channel;

/**
 * 后付费资源欠费(达到保留时长)Consumer
 * @author xiangyu.cao@eayun.com
 *
 */
@Component
public class ArrearageOutRententionTimeConsumer implements ChannelAwareMessageListener{
	private static final Logger log = LoggerFactory
			.getLogger(ArrearageOutRententionTimeConsumer.class);
	@Autowired
	private ResourceDisposeService resourceDisposeService;
	@Autowired
	private DistributedLockService distributedLockService;
	@Override
	public void onMessage(Message msg, Channel channel) throws Exception {
		try {
			log.info("后付费资源欠费(达到保留时长)Consumer");
			String msgBody = new String(msg.getBody());
			JSONObject json = JSONObject.parseObject(msgBody);
			final String cusId = json.getString("customerId");
			long longTime=json.getLongValue("time");
			final Date time=new Date(longTime);
			DistributedLockBean dlBean=new DistributedLockBean();
		    dlBean.setGranularity("doOutRententionTime-"+cusId);
		    dlBean.setLockService(new LockService() {
			
				@Override
				public Object doService() throws Exception {
					resourceDisposeService.outRententionTime(cusId,time);
					return null;
				}
		    });
		    distributedLockService.doServiceByLock(dlBean);
//			channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
		} catch (Exception e) {
			log.error("后付费资源欠费超时(超过保留时长)处理失败", e);
//			channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
		} finally{
			channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
		}
		
	}

}

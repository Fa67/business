package com.eayun.order.mq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.constant.OrderStateType;
import com.eayun.common.constant.OrderType;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.util.SessionUtil;
import com.eayun.common.util.TransactionHookUtil;
import com.eayun.common.util.TransactionHookUtil.Hook;
import com.eayun.customer.model.BaseUser;
import com.eayun.customer.model.User;
import com.eayun.customer.serivce.UserService;
import com.eayun.log.service.LogService;
import com.eayun.order.model.BaseOrder;
import com.eayun.order.service.OrderService;
import com.rabbitmq.client.Channel;

@Component
@Transactional
public class PayToOrderConsumer implements ChannelAwareMessageListener {

	private static final Logger log = LoggerFactory.getLogger(PayToOrderConsumer.class);

	@Autowired
	private OrderToResourceProducer orderToResourceProducer;
	@Autowired
	private OrderService orderService;
	@Autowired
	private LogService logService;
	@Autowired
	private UserService userService;

	@Override
	public void onMessage(Message msg, Channel channel) throws IOException {
		log.info("订单消费者开始处理……");
		try {
			JSONObject jsonObject = JSON.parseObject(new String(msg.getBody()));
			log.info("接收到的消息为{}", jsonObject);
			ListIterator<Object> iter = jsonObject.getJSONArray("ordersNo").listIterator();
			List<String> ordersNo = new ArrayList<String>();
			while (iter.hasNext()) {
				String orderNo = (String) iter.next();
				ordersNo.add(orderNo);
			}
			boolean isPaid = false;
			isPaid = jsonObject.getBoolean("isPaid");
			String userId = jsonObject.getString("userId");
			User baseUser =null;
			if(userId != null){
				baseUser = userService.findUserById(userId);
			}
			final String userAccount = baseUser == null ? null : baseUser.getUserAccount();
			if (isPaid && ordersNo != null && ordersNo.size() > 0) {
				// 记录日志
				addPayOrderLog(ordersNo, null, userAccount);

				List<BaseOrder> baseOrders = orderService.getByOrdersNo(ordersNo);
				if(baseOrders != null && baseOrders.size()>0){
					log.info("订单消费者 - {} - {}", baseOrders.size() == 1 ? "单笔支付" : "合并支付", baseOrders.size());
					for (final BaseOrder baseOrder : baseOrders) {
						log.info("处理订单：{}。", baseOrder.getOrderNo());
						if (baseOrder.getOrderState().equals(OrderStateType.TO_BE_PAID)) {
							TransactionHookUtil.registAfterCommitHook(new Hook() {
								@Override
								public void execute() {
									if (baseOrder.getOrderType().equals(OrderType.RENEW)) {
										// 续费
										orderToResourceProducer.sendToRenewal(baseOrder.getOrderNo(), baseOrder.getCusId(),
												userAccount, baseOrder.getParams());
									} else {
										// 新购或者升级
										orderToResourceProducer.sendToNewOrUpgrade(baseOrder.getOrderNo(),
												baseOrder.getOrderType(), baseOrder.getResourceType());
									}
								}
							});
							// 更改订单状态
							log.info("更改订单：{}状态为处理中。", baseOrder.getOrderNo());
							orderService.toBuildingResourceState(baseOrder);
						} else {
							log.info("订单：{}，状态为非待支付状态，不发送创建资源消息。", baseOrder.getOrderNo());
							continue;
						}
					}
				}else{
					log.info("消息中的订单号为空！");
				}
			} else {
				// 记录异常日志
				addPayOrderLog(ordersNo, new AppException("支付失败"), userAccount);
			}
		} catch (Exception e) {
			log.error("消费者消费支付订单消息失败，原因：{}" + e.getMessage());
			throw e;
		} finally {
			// 消息反馈
			channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
		}
		log.info("消费处理完成");
	}

	public void addPayOrderLog(List<String> ordersNo, Exception e, String userAccount) {
		String log_statu = (e == null ? ConstantClazz.LOG_STATU_SUCCESS : ConstantClazz.LOG_STATU_ERROR);

		// 添加日志
		BaseOrder baseOrder = null;
		if (ordersNo.size() > 1) {
			for (String orderNo : ordersNo) {
				baseOrder = orderService.getOrderByNo(orderNo);
				logService.addLog("合并支付订单", userAccount, ConstantClazz.LOG_TYPE_ORDER,
						baseOrder.getOrderNo() + "-" + baseOrder.getProdName(), null, baseOrder.getCusId(), log_statu,
						e);
				baseOrder = null;
			}
		} else {
			baseOrder = orderService.getOrderByNo(ordersNo.get(0));
			logService.addLog("支付订单", userAccount, ConstantClazz.LOG_TYPE_ORDER,
					baseOrder.getOrderNo() + "-" + baseOrder.getProdName(), null, baseOrder.getCusId(), log_statu, e);
		}
	}
}

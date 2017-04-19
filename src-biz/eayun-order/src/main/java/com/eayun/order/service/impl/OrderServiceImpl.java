package com.eayun.order.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.constant.OrderStateType;
import com.eayun.common.constant.OrderType;
import com.eayun.common.constant.PayType;
import com.eayun.common.constant.ResourceType;
import com.eayun.common.constant.ThirdPartType;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.service.SerialNumService;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.SessionUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.common.util.TransactionHookUtil;
import com.eayun.common.util.TransactionHookUtil.Hook;
import com.eayun.costcenter.service.CostReportService;
import com.eayun.log.service.LogService;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.order.dao.OrderDao;
import com.eayun.order.model.BaseOrder;
import com.eayun.order.model.BaseOrderResource;
import com.eayun.order.model.BaseOrderStateRecord;
import com.eayun.order.model.Order;
import com.eayun.order.mq.OrderToResourceProducer;
import com.eayun.order.service.OrderResourceService;
import com.eayun.order.service.OrderService;
import com.eayun.order.service.OrderStateRecordService;
import com.eayun.pay.service.EayunPaymentService;
import com.eayun.virtualization.service.CloudFloatIpService;
import com.eayun.virtualization.service.NetWorkService;
import com.eayun.virtualization.service.PoolService;
import com.eayun.virtualization.service.VmService;
import com.eayun.virtualization.service.VolumeService;
import com.eayun.virtualization.service.VpnService;

/**
 *                       
 * @Filename: OrderServiceImpl.java
 * @Description: 
 * @Version: 1.0
 * @Author: bo.zeng
 * @Email: bo.zeng@eayun.com
 * @History:<br>
 *<li>Date: 2016年8月23日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

	private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

	@Autowired
	private OrderDao orderDao;
	@Autowired
	private OrderStateRecordService orderStateRecordService;
	@Autowired
	private EayunPaymentService eayunPaymentService;
	@Autowired
	private OrderToResourceProducer orderToResourceProducer;
	@Autowired
	private SerialNumService serialNumService;
	@Autowired
	private OrderResourceService orderResourceService;
	@Autowired
	private MessageCenterService messageCenterService;
	@Autowired
	private CostReportService costReportService;
	@Autowired
	private LogService logService;
	@Autowired
	private NetWorkService netWorkService;
	@Autowired
	private PoolService poolService;
	@Autowired
	private VpnService vpnService;
	@Autowired
	private CloudFloatIpService floatIpService;
	@Autowired
	private VmService  vmService;
	@Autowired
	private VolumeService  volumeService;

	private static final String EAYUN_PUBLIC_CLOUD_SERVICE = "易云公有云服务";

	private static final String BATCH_PAYMENT = "批量支付";
	
	private static final String ACT_PERSON_API = "API";
	

	@Override
	public Order createOrder(final Order order) throws Exception {
		log.info("开始生成订单。");
		// 是否记录支付日志标识
		boolean addPayLogFlag = false;
		Date date = new Date();
		try {
			//验证订单参数
			validateOrder(order);
				// 生成订单编号
				order.setOrderNo(getOrderNo(date));
				order.setCreateTime(date);
				order.setPayExpireTime(DateUtil.addDay(date, new int[] { 0, 0, 1 })); // 设置超时时间24小时
				order.setThirdPartType(ThirdPartType.ALIPAY);
				order.setVersion(0);

				// 余额支付
				String tradeNo = null;
				if (order.getPayType().equals(PayType.PAYBEFORE)
						&& (order.getAccountPayment().compareTo(BigDecimal.ZERO) == 1 
						|| order.getPaymentAmount().compareTo(BigDecimal.ZERO) == 0)) {
					try {
						log.info("调用余额支付接口。");
						tradeNo = eayunPaymentService.balancePay(order.getOrderNo(), order.getCusId(),
								order.getUserId(), order.getProdName(), order.getAccountPayment(),
								order.getResourceType());
					} catch (Exception e) {
						throw e;
					}
				}
				// 设置订单状态
				if (order.getPayType().equals(PayType.PAYBEFORE)
						&& order.getThirdPartPayment().compareTo(BigDecimal.ZERO) == 1) {
					// 订单为预付费，且第三方支付金额大于0，订单状态为待支付
					order.setOrderState(OrderStateType.TO_BE_PAID);
				} else if (order.getPayType().equals(PayType.PAYAFTER) || (order.getPayType().equals(PayType.PAYBEFORE)
						&& (tradeNo != null || order.getAccountPayment().compareTo(BigDecimal.ZERO) == 0) 
						&& order.getThirdPartPayment().compareTo(BigDecimal.ZERO) == 0)) {
					// 订单为后付费，或者订单为预付费且余额支付所有费用，订单状态为资源创建中
					order.setOrderState(OrderStateType.BUILDING_RESOURCE);
					// 修改标识
					addPayLogFlag = !(order.getPayType().equals(PayType.PAYAFTER));
				}
				BaseOrder baseOrder = new BaseOrder();
				try {
					log.info("订单信息：{}", JSON.toJSONString(order));
					BeanUtils.copyPropertiesByModel(baseOrder, order);
					// 保存订单信息
					baseOrder = orderDao.save(baseOrder);
					order.setOrderId(baseOrder.getOrderId());

				} catch (Exception e) {
					throw e;
				}

				// 如果订单状态为资源创建中，则放消息队列开通资源
			if (order.getPayType().equals(PayType.PAYBEFORE)
					&& order.getOrderState().equals(OrderStateType.BUILDING_RESOURCE)) {
				TransactionHookUtil.registAfterCommitHook(new Hook() {
					@Override
					public void execute() {
						if (order.getOrderType().equals(OrderType.NEW)
								|| order.getOrderType().equals(OrderType.UPGRADE)) {
							log.info("发送新购或升级资源的消息队列。");
							orderToResourceProducer.sendToNewOrUpgrade(order.getOrderNo(), order.getOrderType(),
									order.getResourceType());
						} else if (order.getOrderType().equals(OrderType.RENEW)) {
							log.info("发送续费资源的消息队列。");
							SessionUserInfo sessionUser = (SessionUserInfo) SessionUtil.getSession()
									.getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
							orderToResourceProducer.sendToRenewal(order.getOrderNo(), order.getCusId(),
									sessionUser.getUserName(), order.getParams());
						}
					}
				});
			}
			

			// 添加订单变更记录
			BaseOrderStateRecord record = new BaseOrderStateRecord(order.getOrderNo(), null, order.getOrderState());
			log.info("添加订单状态变更记录：{}", JSON.toJSONString(record));
			orderStateRecordService.addOrderStateRecord(record);
			// 创建订单日志
			logService.addLog("提交订单", ConstantClazz.LOG_TYPE_ORDER, order.getOrderNo() + "-" + order.getProdName(),
					null, ConstantClazz.LOG_STATU_SUCCESS, null);
			// 记录支付日志
			if (addPayLogFlag) {
				logService.addLog("支付订单", ConstantClazz.LOG_TYPE_ORDER, order.getOrderNo() + "-" + order.getProdName(),
						null, ConstantClazz.LOG_STATU_SUCCESS, null);
			}
			return order;
		} catch (AppException e) {
			logService.addLog("提交订单", ConstantClazz.LOG_TYPE_ORDER, order.getProdName(),
					null, ConstantClazz.LOG_STATU_ERROR, e);
			throw e;
		}
	}
	
	@Override
	public Order createOrderForVmAPI(final Order order) throws Exception {
		log.info("开始生成订单（API）。");
		// 是否记录支付日志标识
		boolean addPayLogFlag = false;
		Date date = new Date();
		try {
			// 验证订单参数
			validateOrder(order);
			// 生成订单编号
			order.setOrderNo(getOrderNo(date));
			order.setCreateTime(date);
			order.setPayExpireTime(DateUtil.addDay(date, new int[] { 0, 0, 1 })); // 设置超时时间24小时
			order.setThirdPartType(ThirdPartType.ALIPAY);
			order.setVersion(0);
			// 余额支付
			String tradeNo = null;
			log.info("调用余额支付接口。");
			if (order.getPayType().equals(PayType.PAYBEFORE)
					&& (order.getAccountPayment().compareTo(BigDecimal.ZERO) == 1
							|| order.getPaymentAmount().compareTo(BigDecimal.ZERO) == 0)) {
				tradeNo = eayunPaymentService.balancePay(order.getOrderNo(), order.getCusId(), order.getUserId(),
						order.getProdName(), order.getAccountPayment(), order.getResourceType());
			}
			if (order.getPayType().equals(PayType.PAYAFTER) || (order.getPayType().equals(PayType.PAYBEFORE)
					&& (tradeNo != null || order.getAccountPayment().compareTo(BigDecimal.ZERO) == 0))) {
				order.setOrderState(OrderStateType.BUILDING_RESOURCE);
				addPayLogFlag = true;
			}
			BaseOrder baseOrder = new BaseOrder();
			BeanUtils.copyPropertiesByModel(baseOrder, order);
			// 保存订单信息
			baseOrder = orderDao.save(baseOrder);
			order.setOrderId(baseOrder.getOrderId());
			// 添加订单变更记录
			BaseOrderStateRecord record = new BaseOrderStateRecord(order.getOrderNo(), null, order.getOrderState());
			log.info("添加订单状态变更记录：{}", JSON.toJSONString(record));
			orderStateRecordService.addOrderStateRecord(record);
			// 发送消息
			log.info("发送创建订单的邮件和短信消息。");
			messageCenterService.newOrderMessage(order);
			// 创建订单日志
			logService.addLog("提交订单", ACT_PERSON_API, ConstantClazz.LOG_TYPE_ORDER, order.getOrderNo() + "-" + order.getProdName(), null,
					order.getCusId(), ConstantClazz.LOG_STATU_SUCCESS, null);
			// 记录支付日志
			if (addPayLogFlag) {
				logService.addLog("支付订单", ACT_PERSON_API, ConstantClazz.LOG_TYPE_ORDER, order.getOrderNo() + "-" + order.getProdName(), 
						null,order.getCusId(), ConstantClazz.LOG_STATU_SUCCESS, null);
			}
			return order;
		} catch (AppException e) {
			logService.addLog("提交订单", ACT_PERSON_API, ConstantClazz.LOG_TYPE_ORDER, order.getProdName(), null, order.getCusId(),
					ConstantClazz.LOG_STATU_ERROR, e);
			throw e;
		}
	}

	@Override
	public Page getOrderList(QueryMap queryMap, String startTime, String endTime, BaseOrder order) {
		StringBuffer hqlBuffer = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		hqlBuffer.append("from BaseOrder o where 1 = 1 ");
		if (!StringUtil.isEmpty(startTime)) {
			hqlBuffer.append("and o.createTime >= ? ");
			params.add(DateUtil.timestampToDate(startTime));
		}
		if (!StringUtil.isEmpty(endTime)) {
			hqlBuffer.append("and o.createTime <= ? ");
			params.add(DateUtil.addDay(DateUtil.timestampToDate(endTime), new int[]{0, 0, 1}));
		}
		if (!StringUtil.isEmpty(order.getCusId())) {
			hqlBuffer.append("and o.cusId = ? ");
			params.add(order.getCusId());
		}
		if (!StringUtil.isEmpty(order.getProdName())) {
			hqlBuffer.append("and o.prodName like ? escape '/'");
			params.add("%" + escapeSpecialChar(order.getProdName()) + "%");
		}
		if (!StringUtil.isEmpty(order.getOrderType())) {
			hqlBuffer.append("and o.orderType = ? ");
			params.add(order.getOrderType());
		}
		if (!StringUtil.isEmpty(order.getOrderState())) {
			hqlBuffer.append("and o.orderState = ? ");
			params.add(order.getOrderState());
		}
		if (!StringUtil.isEmpty(order.getOrderNo())) {
			hqlBuffer.append("and o.orderNo like ? escape '/'");
			params.add("%" + escapeSpecialChar(order.getOrderNo()) + "%");
		}
		hqlBuffer.append("order by o.createTime desc");
		Page page = orderDao.pagedQuery(hqlBuffer.toString(), queryMap, params.toArray());
		List<BaseOrder> baseOrders = (List<BaseOrder>) page.getResult();
		List<Order> orders = new ArrayList<Order>();
		if (baseOrders != null && baseOrders.size() > 0) {
			for (BaseOrder baseOrder : baseOrders) {
				Order newOrder = new Order();
				BeanUtils.copyPropertiesByModel(newOrder, baseOrder);
				newOrder.getTypeName();
				orders.add(newOrder);
			}
			page.setResult(orders);
		}
		return page;
	}

	@Override
	public Order getOrderById(String orderId) {
		BaseOrder baseOrder = orderDao.findOne(orderId);
		if (isOrderBelongsToCurrCus(baseOrder)) {
			Order order = new Order();
			BeanUtils.copyPropertiesByModel(order, baseOrder);
			order.getTypeName();
			return order;
		} else {
			throw new AppException("很抱歉，您访问的页面出现错误！");
		}
	}

	@Override
	public Order getOrderByNo(String orderNo) {
		BaseOrder baseOrder = orderDao.findByOrderNo(orderNo);
		if (isOrderBelongsToCurrCus(baseOrder)) {
			Order order = new Order();
			BeanUtils.copyPropertiesByModel(order, baseOrder);
			order.getTypeName();
			return order;
		}else {
			throw new AppException("很抱歉，您访问的页面出现错误！");
		}
	}
	
	@Override
	public Order getOrderWithoutValidate(String orderNo) {
		BaseOrder baseOrder = orderDao.findByOrderNo(orderNo);
		Order order = new Order();
		BeanUtils.copyPropertiesByModel(order, baseOrder);
		order.getTypeName();
		return order;
	}

	@Override
	public BaseOrder cancelOrder(String orderId) {
		try {
			BaseOrder baseOrder = orderDao.findOne(orderId);
			if(OrderStateType.TO_BE_PAID.equals(baseOrder.getOrderState())){
				if (baseOrder.getAccountPayment().compareTo(BigDecimal.ZERO) == 1) {
					log.info("取消订单{}，退费。", baseOrder.getOrderNo());
					eayunPaymentService.orderRefund(baseOrder.getOrderNo(), baseOrder.getCusId(),
							baseOrder.getAccountPayment(), baseOrder.getProdName());
				}
				baseOrder.setCanceledTime(new Date());
				return this.changeOrderState(baseOrder, OrderStateType.CANCELED);
			}else{
				throw new AppException("订单状态已更新，请确认后重新操作！");
			}
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public Map<String, Object> payOrder(List<String> ordersNo) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		BigDecimal paymentAmount = BigDecimal.ZERO; // 应付金额
		BigDecimal accountPayment = BigDecimal.ZERO; // 余额支付金额
		BigDecimal thirdPartPayment = BigDecimal.ZERO; // 第三方支付金额
		List<Order> orders = new ArrayList<Order>();
		if (!CollectionUtils.isEmpty(ordersNo)) {
			//判断订单是否为当前客户的订单
			List<BaseOrder> orderList = orderDao.findByOrdersNo(ordersNo);
			if(!CollectionUtils.isEmpty(orderList)){
				for (BaseOrder baseOrder : orderList) {
					if (!isOrderBelongsToCurrCus(baseOrder) || !baseOrder.getOrderState().equals(OrderStateType.TO_BE_PAID)) {
						log.info("订单 {} 不属于当前客户或者该订单状态为待支付。", baseOrder.getOrderNo());
						throw new AppException("很抱歉，您访问的页面出现错误！");
					}
						paymentAmount = paymentAmount.add(baseOrder.getPaymentAmount());
						accountPayment = accountPayment.add(baseOrder.getAccountPayment());
						thirdPartPayment = thirdPartPayment.add(baseOrder.getThirdPartPayment());

						Order order = new Order();
						BeanUtils.copyPropertiesByModel(order, baseOrder);
						order.getTypeName();
						orders.add(order);
				}
			}else{
				log.info("支付订单号为空。");
				throw new AppException("很抱歉，您访问的页面出现错误！");
			}
		}
		resultMap.put("orders", orders);
		resultMap.put("paymentAmount", paymentAmount);
		resultMap.put("accountPayment", accountPayment);
		resultMap.put("thirdPartPayment", thirdPartPayment);
		return resultMap;
	}

	@Override
	public BaseOrder completeOrder(String orderNo, boolean isResourceOpened, List<BaseOrderResource> orderResources)
			throws Exception {
		return completeOrder(orderNo, isResourceOpened, orderResources, false, null);
	}
	
	@Override
	public BaseOrder completeOrder(String orderNo, boolean isResourceOpened, List<BaseOrderResource> orderResources,
			boolean isResourceInKeep, Date origExpireTime) throws Exception {
		try {
			log.info("执行完成订单接口，订单号：{}", orderNo);
			BaseOrder baseOrder = orderDao.findByOrderNo(orderNo);
			if (baseOrder != null) {
				// 资源开通成功
				if (isResourceOpened) {
					// 设置完成时间
					log.info("订单：{} 资源创建成功！", orderNo);
					baseOrder.setCompleteTime(new Date());
					// 记录订单资源
					if (orderResources != null && orderResources.size() > 0) {
						log.info("记录订单：{} 资源信息。", orderNo);
						orderResourceService.addOrderResource(orderResources);
					}
					if(baseOrder.getPayType().equals(PayType.PAYBEFORE)){
						// 预付费订单计算资源起始时间和到期时间
						Date resourceBeginTime = null;
						Date resourceExpireTime = null;
						if (baseOrder.getOrderType().equals(OrderType.NEW)) {
							resourceBeginTime = baseOrder.getCompleteTime();
							resourceExpireTime = DateUtil.getExpirationDate(baseOrder.getCompleteTime(),
									baseOrder.getBuyCycle(), DateUtil.PURCHASE);
						} else if (baseOrder.getOrderType().equals(OrderType.RENEW) && origExpireTime != null) {
							// 续费资源在保留时长内
							if (isResourceInKeep) {
								resourceBeginTime = origExpireTime;
								resourceExpireTime = DateUtil.getExpirationDate(origExpireTime, baseOrder.getBuyCycle(),
										DateUtil.RENEWAL);
							} else { // 续费资源在保留时长外
								resourceBeginTime = baseOrder.getCompleteTime();
								resourceExpireTime = DateUtil.getExpirationDate(baseOrder.getCompleteTime(),
										baseOrder.getBuyCycle(), DateUtil.RENEWAL);
							}
						} else if (baseOrder.getOrderType().equals(OrderType.UPGRADE)) {
							resourceBeginTime = baseOrder.getCompleteTime();
							resourceExpireTime = origExpireTime;
						}
						log.info("订单：{} 资源起始时间【{}】，截至时间【{}】！", orderNo, resourceExpireTime, resourceBeginTime);
						if (resourceExpireTime != null || resourceBeginTime != null) {
							baseOrder.setResourceBeginTime(resourceBeginTime);
							baseOrder.setResourceExpireTime(resourceExpireTime);
						}
					}
					
					// 预付费订单修改交易记录状态
					if (baseOrder.getPayType().equals(PayType.PAYBEFORE)) {
						costReportService.changePrepaymentState(orderNo);
					}
					log.info("修改订单：{} 为完成状态。", orderNo);
					return this.changeOrderState(baseOrder, OrderStateType.COMPLETE);
				} else { // 资源开通失败
					try {
						log.info("订单：{} ，资源创建失败！", orderNo);
						if (baseOrder.getPayType().equals(PayType.PAYBEFORE)){
							// 退费处理
							log.info("订单：{} ，为预付费订单，退费。", orderNo);
							eayunPaymentService.orderRefund(baseOrder.getOrderNo(), baseOrder.getCusId(),
									baseOrder.getPaymentAmount(), baseOrder.getProdName());
						}
						
						baseOrder.setCanceledTime(new Date());
						// 更改订单状态
						log.info("修改订单：{} 为已取消-处理失败状态。", orderNo);
						baseOrder = this.changeOrderState(baseOrder, OrderStateType.CANCELED_BY_RESOURCE);
					} catch (Exception e) {
						log.info("完成订单 {} 接口异常，异常信息：", orderNo, e.getMessage());
					}
					return baseOrder;
				}
			} else {
				throw new AppException("未查询到订单信息！");
			}
		} catch (AppException e) {
			throw e;
		}
	}

	@Override
	public JSONObject doOrderPay(List<String> ordersNo, String thirdPartType, String userId, String cusId) {
		JSONObject reJson = new JSONObject();
		// 验证订单状态是否为待支付状态
		if (!checkOrderState(ordersNo, OrderStateType.TO_BE_PAID) || eayunPaymentService.containsThirdPartPaidOrder(ordersNo)) {
			log.info("订单状态为非待支付状态或者订单有支付记录！");
			reJson.put("code", "1");
			return reJson;
		}
		// 验证订单中是否包含升级订单且升级资源是否存在
		BaseOrder resourceNotExistsOrder = getResourceNotExistsOrder(ordersNo);
		if (resourceNotExistsOrder != null) {
			log.info("订单{}为升级订单，且升级资源不存在！", resourceNotExistsOrder.getOrderNo());
			if (resourceNotExistsOrder.getAccountPayment().compareTo(BigDecimal.ZERO) == 1) {
				log.info("取消订单{}，退费。", resourceNotExistsOrder.getOrderNo());
				eayunPaymentService.orderRefund(resourceNotExistsOrder.getOrderNo(), resourceNotExistsOrder.getCusId(),
						resourceNotExistsOrder.getAccountPayment(), resourceNotExistsOrder.getProdName());
			}
			resourceNotExistsOrder.setCanceledTime(new Date());
			changeOrderState(resourceNotExistsOrder, OrderStateType.CANCELED_BY_RESOURCE);
			reJson.put("code", "2");
			reJson.put("data", resourceNotExistsOrder.getResourceType());
			return reJson;
		}
		log.info("订单 <{}> 状态为待支付状态，校验通过。", ordersNo.toArray());
		BigDecimal thirdPartPayment = orderDao.getThirdPartPayment(ordersNo);
		String formData = "";
		// 调用第三方支付接口
		if (thirdPartType.equals(ThirdPartType.ALIPAY)) {
			String prodNameForThirdPart = null;
			if (ordersNo.size() == 1) {
				BaseOrder order = orderDao.findByOrderNo(ordersNo.get(0));
				prodNameForThirdPart = EAYUN_PUBLIC_CLOUD_SERVICE + "-" + order.getProdName();
			} else {
				prodNameForThirdPart = EAYUN_PUBLIC_CLOUD_SERVICE + "-" + BATCH_PAYMENT;
			}

			// 获取支付表单（支付宝）
			log.info("调用支付宝接口：订单号【{}】，客户ID【{}】，用户ID【{}】，第三方支付金额【{}】，产品名称【{}】", ordersNo.toArray(), cusId, userId,
					thirdPartPayment, prodNameForThirdPart);
			formData = eayunPaymentService.getOrderAlipayForm(ordersNo, cusId, userId, thirdPartPayment,
					prodNameForThirdPart, null);
		}
		reJson.put("code", "0");
		reJson.put("data", formData);
		return reJson;
	}
	
	private BaseOrder getResourceNotExistsOrder(List<String> ordersNo) {
		if (ordersNo == null) {
			return null;
		}
		List<BaseOrder> orderList = orderDao.findByOrdersNo(ordersNo);
		for (BaseOrder order : orderList) {
			if (order.getOrderType().equals(OrderType.UPGRADE) || order.getOrderType().equals(OrderType.RENEW)) {
				String orderNo = order.getOrderNo();
				boolean isExists = true;
				if (order.getResourceType().equals(ResourceType.NETWORK)) {
					isExists = netWorkService.isExistsByOrderNo(orderNo);
				} else if (order.getResourceType().equals(ResourceType.QUOTAPOOL)) {
					isExists = poolService.isExistsByOrderNo(orderNo);
				} else if (order.getResourceType().equals(ResourceType.VDISK)) {
					isExists = volumeService.isExistsByOrderNo(orderNo);
				} else if (order.getResourceType().equals(ResourceType.VM)) {
					isExists = vmService.isExistsByOrderNo(orderNo);
				}else if (order.getResourceType().equals(ResourceType.FLOATIP)) {
					isExists = floatIpService.isExistsByOrderNo(orderNo);
				}else if (order.getResourceType().equals(ResourceType.VPN)) {
					isExists = vpnService.isExistsByOrderNo(orderNo);
				}
				
				if (!isExists) {
					return order;
				}
			}
		}
		return null;
	}

	@Override
	public List<BaseOrder> getByOrdersNo(List<String> ordersNo) {
		if (ordersNo != null && ordersNo.size() > 0) {
			return orderDao.findByOrdersNo(ordersNo);
		}
		return null;
	}

	private void validateOrder(Order order) throws Exception {
		String errMsg = null;
		// 判断参数orderType是否为空
		if (StringUtil.isEmpty(order.getOrderType())) {
			errMsg = "Pramater orderType invalidate!";
		} else if (StringUtil.isEmpty(order.getProdName())) {
			errMsg = "Pramater prodName invalidate!";
		} else if (order.getProdCount() <= 0) {
			errMsg = "Pramater prodCount invalidate!";
		} else if (StringUtil.isEmpty(order.getProdConfig())) {
			errMsg = "Pramater prodConfig invalidate!";
		} else if (StringUtil.isEmpty(order.getPayType())) {
			errMsg = "Pramater payType invalidate!";
		} else if (order.getPayType().equals(PayType.PAYBEFORE) && !order.getOrderType().equals(OrderType.UPGRADE) && order.getBuyCycle() <= 0) {
			errMsg = "Pramater buyCycle invalidate!";
		} else if (order.getPayType().equals(PayType.PAYBEFORE)
				&& order.getUnitPrice().compareTo(BigDecimal.ZERO) == -1) {
			errMsg = "Pramater unitPrice invalidate!";
		} else if (order.getPayType().equals(PayType.PAYAFTER) && StringUtil.isEmpty(order.getBillingCycle())) {
			errMsg = "Pramater billingCycle invalidate!";
		} else if (StringUtil.isEmpty(order.getResourceType())) {
			errMsg = "Pramater resourceType invalidate!";
		} else if (order.getPayType().equals(PayType.PAYBEFORE)
				&& order.getPaymentAmount().compareTo(BigDecimal.ZERO) == -1) {
			errMsg = "Pramater paymentAmount invalidate!";
		} else if (order.getPayType().equals(PayType.PAYBEFORE) && order.getPaymentAmount()
				.compareTo(order.getThirdPartPayment().add(order.getAccountPayment())) != 0) {
			errMsg = "Pramater paymentAmount not equals accountPayment + thirdPartPayment!";
		} else if (order.getPayType().equals(PayType.PAYBEFORE) && order.getOrderType().equals(OrderType.UPGRADE) && order.getResourceExpireTime() == null){
			errMsg = "Pramater resourceExpireTime invalidate!";
		}
		if (errMsg != null) {
			throw new AppException(errMsg);
		}
	}

	private String getOrderNo(Date date) {
		if (date == null) {
			date = new Date();
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String dateStr = sdf.format(date);
		String jedisKey = "01" + dateStr;
		return serialNumService.getSerialNum(jedisKey, 8);
	}

	@Override
	public void toBuildingResourceState(BaseOrder baseOrder) {
		try {
			changeOrderState(baseOrder, OrderStateType.BUILDING_RESOURCE);
		} catch (AppException e) {
			throw e;
		}
	}

	@Override
	public String getObsOrderNumberByCusId(String cusId) {
		StringBuffer sb = new StringBuffer();
		sb.append("select order_no from order_info where cus_id=?");
		Query query = orderDao.createSQLNativeQuery(sb.toString(), cusId);
		List list = query.getResultList();
		String orderNo = "";
		for (int i = 0; i < list.size(); i++) {
			orderNo = (String) query.getResultList().get(0);
		}
		return orderNo;
	}

	private BaseOrder changeOrderState(BaseOrder baseOrder, String toState) {
		log.info("更改订单 {} 状态为 {}。", baseOrder.getOrderNo(), toState);
		BaseOrderStateRecord record = new BaseOrderStateRecord();
		record.setOriginState(baseOrder.getOrderState());
		baseOrder.setOrderState(toState);
		orderDao.saveOrUpdate(baseOrder);
		// 添加订单状态变更日志
		record.setChangeTime(new Date());
		record.setOrderNo(baseOrder.getOrderNo());
		record.setToState(toState);
		log.info("添加订单 {} 状态变更记录。", baseOrder.getOrderNo());
		orderStateRecordService.addOrderStateRecord(record);
		return baseOrder;
	}

	private String escapeSpecialChar(String str) {
		if (StringUtils.isNotBlank(str)) {
			String[] specialChars = { "\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|", "%" };
			for (String key : specialChars) {
				if (str.contains(key)) {
					str = str.replace(key, "/" + key);
				}
			}
		}
		return str;
	}

	@Override
	public Page getResourceByOrderNo(QueryMap queryMap, String orderNo) {
		return orderResourceService.getResourceByOrderNo(queryMap, orderNo);
	}
	
	@Override
	public boolean isOrderBelongsToCurrCus(String orderNo) {
		if (StringUtil.isEmpty(orderNo)) {
			return false;
		}
		BaseOrder baseOrder = orderDao.findByOrderNo(orderNo);
		return isOrderBelongsToCurrCus(baseOrder);
	}
	

	public boolean isOrderBelongsToCurrCus(BaseOrder baseOrder) {
		HttpSession httpSession = SessionUtil.getSession();
		if(httpSession == null){
			return true;
		}else{
			SessionUserInfo sessionUser = (SessionUserInfo) httpSession
					.getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
			if (baseOrder != null && baseOrder.getCusId().equals(sessionUser.getCusId())) {
				return true;
			}
			return false;
		}
	}

	@Override
	public void updatePayExpireOrder() {
		log.info("更新支付超时订单状态");
		StringBuffer strBuff = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		strBuff.append("from BaseOrder where orderState = ? and payExpireTime < ? ");
		params.add(OrderStateType.TO_BE_PAID);
		params.add(new Date());
		try {
			List<BaseOrder> orders = orderDao.find(strBuff.toString(), params.toArray());
			if(orders!=null && orders.size()>0){
				for (BaseOrder order : orders) {
					order.setCanceledTime(new Date());
					changeOrderState(order, OrderStateType.CANCELED);
				}
			}
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public boolean checkOrderState(List<String> ordersNo, String orderState) {
		List<BaseOrder> orderList = orderDao.findByOrdersNo(ordersNo);
//		containsThirdPartPaidOrder
		for (BaseOrder baseOrder : orderList) {
			if (!orderState.equals(baseOrder.getOrderState())) {
				return false;
			}
		}
		return true;
	}
	
}

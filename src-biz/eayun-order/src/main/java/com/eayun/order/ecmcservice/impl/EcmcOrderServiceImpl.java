package com.eayun.order.ecmcservice.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.customer.ecmcservice.EcmcCustomerService;
import com.eayun.customer.model.Customer;
import com.eayun.customer.serivce.CustomerService;
import com.eayun.order.dao.OrderDao;
import com.eayun.order.ecmcservice.EcmcOrderService;
import com.eayun.order.model.BaseOrder;
import com.eayun.order.model.Order;
import com.eayun.order.service.OrderResourceService;

@Service
@Transactional
public class EcmcOrderServiceImpl implements EcmcOrderService {
	@Autowired
	private EcmcCustomerService ecmcCustomerService;
	
	@Autowired
	private OrderResourceService orderResourceService;
	
	@Autowired
	private OrderDao orderDao;

	@Override
	public Page getOrderList(QueryMap queryMap, String startTime, String endTime, Order queryCriteria) {
		StringBuffer sqlBuffer = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		sqlBuffer.append("SELECT i.order_id, i.order_no, i.prod_name, c.cus_org, i.order_type, i.create_time, i.order_state, i.payment_amount, i.complete_time FROM order_info i left join sys_selfcustomer c on i.cus_id = c.cus_id WHERE 1 = 1 ");
		if (!StringUtil.isEmpty(startTime)) {
			sqlBuffer.append("AND i.create_time >= ? ");
			params.add(DateUtil.timestampToDate(startTime));
		}
		if (!StringUtil.isEmpty(endTime)) {
			sqlBuffer.append("AND i.create_time <= ? ");
			params.add(DateUtil.addDay(DateUtil.timestampToDate(endTime), new int[]{0, 0, 1}));
		}
		if (!StringUtil.isEmpty(queryCriteria.getProdName())) {
			sqlBuffer.append("AND i.prod_name like ? escape '/' ");
			params.add("%" + escapeSpecialChar(queryCriteria.getProdName()) + "%");
		}
		if (!StringUtil.isEmpty(queryCriteria.getCusName())) {
			sqlBuffer.append("AND c.cus_org like ? escape '/' ");
			params.add("%" + escapeSpecialChar(queryCriteria.getCusName()) + "%");
		}
		if (!StringUtil.isEmpty(queryCriteria.getOrderType())) {
			sqlBuffer.append("AND i.order_type = ? ");
			params.add(queryCriteria.getOrderType());
		}
		if (!StringUtil.isEmpty(queryCriteria.getOrderState())) {
			sqlBuffer.append("AND i.order_state = ? ");
			params.add(queryCriteria.getOrderState());
		}
		if (!StringUtil.isEmpty(queryCriteria.getOrderNo())) {
			sqlBuffer.append("AND i.order_no like ? escape '/' ");
			params.add("%" + escapeSpecialChar(queryCriteria.getOrderNo()) + "%");
		}
		sqlBuffer.append("order by i.create_time desc");
		Page page = orderDao.pagedNativeQuery(sqlBuffer.toString(), queryMap, params.toArray());
		@SuppressWarnings("unchecked")
		List<Object[]> result = (List<Object[]>) page.getResult();
		List<Order> orderList = new ArrayList<Order>();
		if (result != null && result.size() > 0) {
			for (Object[] objects : result) {
				Order order = new Order();
				order.setOrderId(ObjectUtils.toString(objects[0], null));
				order.setOrderNo(ObjectUtils.toString(objects[1], null));
				order.setProdName(ObjectUtils.toString(objects[2], null));
				order.setCusName(ObjectUtils.toString(objects[3], null));
				order.setOrderType(ObjectUtils.toString(objects[4], null));
				order.setCreateTime(DateUtil.stringToDate(ObjectUtils.toString(objects[5], null)));
				order.setOrderState(ObjectUtils.toString(objects[6], null));
				order.setPaymentAmount(new BigDecimal(ObjectUtils.toString(objects[7], null)));
				order.setCompleteTime(DateUtil.stringToDate(ObjectUtils.toString(objects[8], null)));
				order.getTypeName();
				orderList.add(order);
			}
		}
		page.setResult(orderList);
		return page;
	}

	@Override
	public Order getOrderDetail(String orderId) {
		BaseOrder baseOrder = orderDao.findOne(orderId);
		Order order = new Order();
		if(baseOrder!=null){
			BeanUtils.copyPropertiesByModel(order, baseOrder);
			order.getTypeName();
			Customer customer = ecmcCustomerService.getCustomerById(baseOrder.getCusId());
			order.setCusName(customer == null ? null : customer.getCusOrg());
		}
		return order;
	}
	
	private String escapeSpecialChar(String str){
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
	public Order getOrderByNO(String orderNo) {
		BaseOrder baseOrder = orderDao.findByOrderNo(orderNo);
		Order order = new Order();
		BeanUtils.copyPropertiesByModel(order, baseOrder);
		order.getTypeName();
		return order;
	}
	
	@Override
	public Page getResourceByOrderNo(QueryMap queryMap, String orderNo) {
		return orderResourceService.getResourceByOrderNo(queryMap, orderNo);
	}

}

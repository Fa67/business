package com.eayun.order.ecmcservice;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.order.model.Order;

public interface EcmcOrderService {
	
	/**
	 * 分页获取ECMC获取订单列表
	 * @param queryMap 分页对象
	 * @param startTime 开始时间
	 * @param endTime 结束时间
	 * @param order 封装查询条件
	 * @return 订单分页集合
	 */
	public Page getOrderList(QueryMap queryMap, String startTime, String endTime, Order order);
	
	/**
	 * 根据订单ID获取订单详情
	 * @param orderId 订单ID
	 * @return 订单实体
	 */
	public Order getOrderDetail(String orderId);
	
	/**
	 * 根据订单编号获取订单详情
	 * @param orderNo 订单编号
	 * @return订单实体
	 */
	public Order getOrderByNO(String orderNo);
	
	/**
	 * 根据订单编号查询资源列表
	 * @param queryMap 分页信息
	 * @param orderNo 订单编号
	 * @return 分页资源列表
	 */
	public Page getResourceByOrderNo(QueryMap queryMap, String orderNo);

}

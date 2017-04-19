package com.eayun.order.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.order.model.BaseOrder;
import com.eayun.order.model.BaseOrderResource;
import com.eayun.order.model.Order;

/**
 *                       
 * @Filename: OrderService.java
 * @Description: 订单服务接口
 * @Version: 1.0
 * @Author: bo.zeng
 * @Email: bo.zeng@eayun.com
 * @History:<br>
 *<li>Date: 2016年7月27日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public interface OrderService {
	
	/**
	 * 创建订单
	 * @param order 订单扩展类
	 * @return 订单对象
	 * @throws Exception 
	 */
	public Order createOrder(Order order) throws Exception;
	
	/**
	 * API云主机创建订单接口
	 * @param order 订单扩展类
	 * @return 订单对象
	 * @throws Exception
	 */
	public Order createOrderForVmAPI(Order order) throws Exception;
	
	/**
	 * 分页获取订单列表
	 * @param queryMap 分页信息
	 * @param startTime 查询开始时间
	 * @param endTime 查询结束时间
	 * @param order 封装订单查询参数
	 * @return 分页订单列表
	 */
	public Page getOrderList(QueryMap queryMap, String startTime, String endTime, BaseOrder order);
	
	/**
	 * 根据订单ID获取订单详情
	 * @param orderId 订单ID
	 * @return 订单实体
	 */
	public Order getOrderById(String orderId);
	
	/**
	 * 根据订单编号获取订单详情
	 * @param orderNo 订单编号
	 * @return 订单实体
	 */
	public Order getOrderByNo(String orderNo);
	
	/**
	 * 根据订单编号获取订单详情（不验证订单所属客户是否为当前登录客户）
	 * @param orderNo 订单编号
	 * @return
	 */
	public Order getOrderWithoutValidate(String orderNo);
	
	/**
	 * 取消订单
	 * @param orderId 订单编号
	 */
	public BaseOrder cancelOrder(String orderId);
	
	/**
	 * 支付订单（to 支付订单页面）
	 * @param orderIds 订单编号（一个或者多个）
	 * @throws Exception 
	 */
	public Map<String, Object> payOrder(List<String> ordersNo);
	
	/**
	 * 完成订单，由资源模块开通资源操作后调用(新购)
	 * @param orderNo 订单编号
	 * @param isResourceOpened 资源开通是否成功
	 * @param orderResources 订单资源列表
	 * @return 订单实体
	 * @throws Exception 
	 */
	public BaseOrder completeOrder(String orderNo, boolean isResourceOpened, List<BaseOrderResource> orderResources) throws Exception;
	
	/**
	 * 完成订单，由资源模块开通资源操作后调用(续费和升级)
	 * @param orderNo 订单编号
	 * @param isResourceOpened 资源开通是否成功
	 * @param orderResources 订单资源列表
	 * @param isResourceInKeep 续费资源是否在保留时长内（true：在保留时长内；false：不在保留时长内），升级类订单传false；
	 * @param origExpireTime 原资源到期时间
	 * @return
	 * @throws Exception 
	 */
	public BaseOrder completeOrder(String orderNo, boolean isResourceOpened, List<BaseOrderResource> orderResources,
			boolean isResourceInKeep, Date origExpireTime) throws Exception;
	
	/**
	 * 支付订单（立即支付）
	 * @param orderIds 订单编号（一个或者多个）
	 * @param thirdPartType 第三方支付类型：如支付宝、微信等
	 * @param userId 支付用户ID
	 * @param cusId 客户ID
	 * @return 支付表单
	 */
	public JSONObject doOrderPay(List<String> ordersNo, String thirdPartType, String userId, String cusId);
	
	/**
	 * 查询多条订单编号查询订单集合
	 * @param ordersNo
	 * @return 订单集合
	 */
	public List<BaseOrder> getByOrdersNo(List<String> ordersNo);
	
	/**
	 * 更改订单状态为资源创建中
	 * @param baseOrder
	 */
	public void toBuildingResourceState(BaseOrder baseOrder);

	/**
	 * 根据客户ID查询对象存储订单号
	 * @param cusId
	 * @return
     */
	String getObsOrderNumberByCusId(String cusId);
	
	/**
	 * 根据订单编号查询资源列表
	 * @param queryMap 分页信息
	 * @param orderNo 订单编号
	 * @return 分页资源列表
	 */
	public Page getResourceByOrderNo(QueryMap queryMap, String orderNo);
	
	/**
	 * 判断订单编号是否属于当前客户
	 * @param orderNo 订单编号
	 * @return true：属于；false：不属于
	 */
	public boolean isOrderBelongsToCurrCus(String orderNo);
	
	/**
	 * 更新支付超时订单状态
	 */
	public void updatePayExpireOrder();
	
	/**
	 * 校验订单是否全都是一种状态的订单
	 * @param ordersNo 订单编号
	 * @param orderState 订单状态
	 * @return
	 */
	public boolean checkOrderState(List<String> ordersNo, String orderState);
}

package com.eayun.order.ecmccontroller;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eayun.common.ConstantClazz;
import com.eayun.common.controller.BaseController;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.order.controller.OrderController;
import com.eayun.order.ecmcservice.EcmcOrderService;
import com.eayun.order.model.BaseOrder;
import com.eayun.order.model.Order;

/**
 *                       
 * @Filename: EcmcOrderController.java
 * @Description: Ecmc订单管理Controller
 * @Version: 1.0
 * @Author: bo.zeng
 * @Email: bo.zeng@eayun.com
 * @History:<br>
 *<li>Date: 2016年7月27日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Controller
@RequestMapping("/ecmc/order")
public class EcmcOrderController extends BaseController {
	
	private static final Logger log = LoggerFactory.getLogger(EcmcOrderController.class);
	
	@Autowired
	private EcmcOrderService ecmcOrderService;
	
	/**
	 * 分页获取订单列表
	 * @param paramsMap 分页、参数封装对象
	 * @return 订单集合
	 */
	@RequestMapping(value = "/getorderlist", method = RequestMethod.POST)
	@ResponseBody
	public Object getOrderList(@RequestBody ParamsMap paramsMap) {
		QueryMap queryMap = new QueryMap();
		Map<String, Object> params = paramsMap.getParams();
		queryMap.setPageNum(paramsMap.getPageNumber() == null ? 1 : paramsMap.getPageNumber());
		if (paramsMap.getPageSize() != null) {
			queryMap.setCURRENT_ROWS_SIZE(paramsMap.getPageSize());
		}
		Order order = new Order();
		order.setCusName(MapUtils.getString(params, "cusName"));
		String startTime = MapUtils.getString(params, "startTime");
		String endTime = MapUtils.getString(params, "endTime");
		order.setOrderType(MapUtils.getString(params, "orderType"));
		order.setProdName(MapUtils.getString(params, "prodName"));
		order.setOrderState(MapUtils.getString(params, "orderState"));
		order.setOrderNo(MapUtils.getString(params, "orderNo"));
		try {
			return ecmcOrderService.getOrderList(queryMap, startTime, endTime, order);
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * 根据订单ID获取订单详情
	 * @param params 参数对象
	 * @return 订单实体
	 */
	@RequestMapping(value = "/getorder", method = RequestMethod.POST)
	@ResponseBody
	public Object getOrder(@RequestBody Map<String, Object> params){
		EayunResponseJson reJson = new EayunResponseJson();
		String orderId = MapUtils.getString(params, "orderId");
		try {
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			reJson.setData(ecmcOrderService.getOrderDetail(orderId));
			return reJson;
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * 根据订单编号获取订单详情
	 * @param params 参数对象
	 * @return 订单实体
	 */
	@RequestMapping(value = "/getorderbyno", method = RequestMethod.POST)
	@ResponseBody
	public Object getOrderByNo(@RequestBody Map<String, Object> params){
		EayunResponseJson reJson = new EayunResponseJson();
		String orderNo = MapUtils.getString(params, "orderNo");
		try {
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			reJson.setData(ecmcOrderService.getOrderByNO(orderNo));
			return reJson;
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * 已完成订单资源
	 * 
	 * @param params
	 *            参数对象
	 * @return 订单资源分页列表
	 */
	@RequestMapping(value = "/getorderresource", method = RequestMethod.POST)
	@ResponseBody
	public Object getOrderResource(@RequestBody ParamsMap paramsMap) {
		log.info("已完成订单资源");
		QueryMap queryMap = new QueryMap();
		Map<String, Object> params = paramsMap.getParams();
		queryMap.setPageNum(paramsMap.getPageNumber() == null ? 1 : paramsMap.getPageNumber());
		if (paramsMap.getPageSize() != null) {
			queryMap.setCURRENT_ROWS_SIZE(paramsMap.getPageSize());
		}
		String orderNo = MapUtils.getString(params, "orderNo");
		try {
			return ecmcOrderService.getResourceByOrderNo(queryMap, orderNo);
		} catch (Exception e) {
			throw e;
		}
	}

}

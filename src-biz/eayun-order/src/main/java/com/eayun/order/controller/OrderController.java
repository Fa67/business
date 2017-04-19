package com.eayun.order.controller;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.constant.OrderStateType;
import com.eayun.common.controller.BaseController;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.util.DateUtil;
import com.eayun.log.service.LogService;
import com.eayun.order.model.BaseOrder;
import com.eayun.order.service.OrderService;
import com.eayun.syssetup.service.SysDataTreeService;

/**
 * 
 * @Filename: OrderController.java
 * @Description: 订单Controller
 * @Version: 1.0
 * @Author: bo.zeng
 * @Email: bo.zeng@eayun.com
 * @History:<br>
 * 				<li>Date: 2016年7月27日</li>
 *               <li>Version: 1.0</li>
 *               <li>Content: create</li>
 *
 */
@Controller
@RequestMapping("/order")
public class OrderController extends BaseController {
	
	private static final Logger log = LoggerFactory.getLogger(OrderController.class);

	@Autowired
	private OrderService orderService;
	
	@Autowired
	private LogService logService;
	@Autowired
	private SysDataTreeService sysDataTreeService;
	/**
	 * 分页获取订单列表
	 * 
	 * @param paramsMap
	 *            分页、参数封装对象
	 * @return 订单集合
	 */
	@RequestMapping(value = "/getorderlist", method = RequestMethod.POST)
	@ResponseBody
	public Object getOrderList(HttpServletRequest request, @RequestBody ParamsMap paramsMap) {
		log.info("分页获取订单列表");
		QueryMap queryMap = new QueryMap();
		Map<String, Object> params = paramsMap.getParams();
		queryMap.setPageNum(paramsMap.getPageNumber() == null ? 1 : paramsMap.getPageNumber());
		if (paramsMap.getPageSize() != null) {
			queryMap.setCURRENT_ROWS_SIZE(paramsMap.getPageSize());
		}
		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession()
				.getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
		BaseOrder order = new BaseOrder();
		order.setCusId(sessionUser.getCusId());
		String startTime = MapUtils.getString(params, "startTime");
		String endTime = MapUtils.getString(params, "endTime");
		order.setOrderType(MapUtils.getString(params, "orderType"));
		order.setProdName(MapUtils.getString(params, "prodName"));
		order.setOrderState(MapUtils.getString(params, "orderState"));
		order.setOrderNo(MapUtils.getString(params, "orderNo"));
		try {
			return orderService.getOrderList(queryMap, startTime, endTime, order);
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 获取订单详情
	 * 
	 * @param params
	 *            参数对象
	 * @return 订单实体
	 */
	@RequestMapping(value = "/getorderbyid", method = RequestMethod.POST)
	@ResponseBody
	public Object getOrderById(@RequestBody Map<String, Object> params) {
		log.info("获取订单详情");
		EayunResponseJson reJson = new EayunResponseJson();
		String orderId = MapUtils.getString(params, "orderId");
		try {
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			reJson.setData(orderService.getOrderById(orderId));
			return reJson;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 获取订单详情
	 * 
	 * @param params
	 *            参数对象
	 * @return 订单实体
	 */
	@RequestMapping(value = "/getorderbyno", method = RequestMethod.POST)
	@ResponseBody
	public Object getOrderByNo(@RequestBody Map<String, Object> params) {
		log.info("获取订单详情");
		EayunResponseJson reJson = new EayunResponseJson();
		String orderNo = MapUtils.getString(params, "orderNo");
		try {
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			reJson.setData(orderService.getOrderByNo(orderNo));
			return reJson;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 取消订单
	 * 
	 * @param params
	 *            参数对象
	 * @return 成功、失败代码
	 */
	@RequestMapping(value = "/cancelorder", method = RequestMethod.POST)
	@ResponseBody
	public Object cancelOrder(@RequestBody Map<String, Object> params) {
		log.info("取消订单");
		EayunResponseJson reJson = new EayunResponseJson();
		String orderId = MapUtils.getString(params, "orderId");
		BaseOrder baseOrder = orderService.getOrderById(orderId);
		try {
			orderService.cancelOrder(orderId);
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			logService.addLog("取消订单", ConstantClazz.LOG_TYPE_ORDER, baseOrder.getOrderNo()+"-"+baseOrder.getProdName(), null, ConstantClazz.LOG_STATU_SUCCESS, null);
			return reJson;
		} catch (Exception e) {
			logService.addLog("取消订单", ConstantClazz.LOG_TYPE_ORDER, baseOrder.getOrderNo()+"-"+baseOrder.getProdName(), null, ConstantClazz.LOG_STATU_ERROR, e);
			throw e;
		}
	}

	/**
	 * 支付订单（to 支付订单页面）
	 * 
	 * @param params
	 *            参数对象
	 * @return 支付表单
	 * @throws Exception 
	 */
	@RequestMapping(value = "/payorder", method = RequestMethod.POST)
	@ResponseBody
	public Object payOrder(@RequestBody Map<String, Object> params) {
		log.info("支付订单（to 支付订单页面）");
		List<String> ordersNo = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(MapUtils.getString(params, "ordersNo"), ",");
		while (st.hasMoreTokens()) {
			ordersNo.add(st.nextToken());
		}
		try {
			return orderService.payOrder(ordersNo);
		} catch (AppException e) {
			throw e;
		}
	}

	/**
	 * 支付
	 * 
	 * @return 支付表单
	 * @throws Exception
	 */
	@RequestMapping(value = "/doorderpay", method = RequestMethod.POST)
    public void doOrderPay(HttpServletRequest request, HttpServletResponse response, String ordersNo,
            String thirdPartType) throws Exception {
		log.info("支付");
        List<String> orderNoList = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(ordersNo, ",");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");
        while (st.hasMoreTokens()) {
            orderNoList.add(st.nextToken());
        }
        try {
            SessionUserInfo sessionUser = (SessionUserInfo) request.getSession()
                    .getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
            JSONObject reJson = orderService.doOrderPay(orderNoList, thirdPartType, sessionUser.getUserId(),
                    sessionUser.getCusId());
            String code = reJson.getString("code");
			if ("1".equals(code)) {		//订单已经被支付或者订单为非待支付状态
				log.info("订单已经被支付或者订单为非待支付状态。");
				response.sendRedirect(request.getContextPath() + "/#/pay/repeatpayerr");
			}else if("2".equals(code)){	//订单中包含升级订单，且升级资源不存在
				log.info("订单中包含升级订单，且升级资源不存在。");
				response.sendRedirect(request.getContextPath() + "/#/pay/resourcenotexistserr/"+reJson.getString("data"));
			}else if("0".equals(code)){
				response.getWriter().write(reJson.getString("data"));
	            response.getWriter().close();
			}
        } catch (AppException e) {
            response.sendRedirect(request.getContextPath() + "/#/error");
        }
    }

	/**
	 * 已完成订单资源
	 * 
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
			return orderService.getResourceByOrderNo(queryMap, orderNo);
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * 根据资源原始到期时间与续费时长，计算最终的到期时间
	 * required by liyanchao
	 * @return Date
	 * 
	 */
	@RequestMapping(value = "/computeRenewEndTime", method = RequestMethod.POST)
	@ResponseBody
	public Date computeRenewEndTime(@RequestBody Map<String,String> map) {
		String timestamp = map.get("original");
		Date originalTime = DateUtil.timestampToDate(timestamp);
		String duration =  map.get("duration");
		
		Date newDate = new Date();
		int recoveryTime = Integer.parseInt(sysDataTreeService.getRecoveryTime());
		Date expiration = DateUtil.addDay(originalTime, new int[]{0, 0, 0, recoveryTime});//加小时
		Date endTime = null;
		
		if(expiration.compareTo(newDate) >= 0){//原始到期时间>当前续费时间
			endTime =  DateUtil.getExpirationDate(originalTime, Integer.parseInt(duration), "RENEWAL");
		}else if(expiration.compareTo(newDate) < 0){
			endTime =  DateUtil.getExpirationDate(newDate, Integer.parseInt(duration), "RENEWAL");
		}
		return endTime;
		
	}
	
	/**
	 * 支付订单时校验订单状态
	 * @param map
	 * @return
	 */
	@RequestMapping(value = "/checkorderstate", method = RequestMethod.POST)
	@ResponseBody
	public Object checkOrderState(@RequestBody Map<String, String> map) {
		List<String> ordersNo = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(MapUtils.getString(map, "ordersNo"), ",");
		while (st.hasMoreTokens()) {
			ordersNo.add(st.nextToken());
		}
		try {
			if (!orderService.checkOrderState(ordersNo, OrderStateType.TO_BE_PAID)) {
				throw new AppException("订单已支付成功，请避免重复操作！");
			}
		} catch (Exception e) {
			throw e;
		}
		return null;
	}
}

package com.eayun.obs.controller;


import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.accesskey.service.AccessKeyService;
import com.eayun.common.ConstantClazz;
import com.eayun.common.controller.BaseController;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.customer.model.CusServiceState;
import com.eayun.customer.serivce.CustomerService;
import com.eayun.obs.service.ObsOpenService;
@Controller
@RequestMapping("/obs/obsOpen")
public class ObsOpenController extends BaseController{
	private static final Logger log = LoggerFactory
			.getLogger(ObsOpenController.class);
	@Autowired
	private ObsOpenService obsOpenService;
	@Autowired
	private CustomerService customerService;
	@Autowired
	private AccessKeyService accessKeyService;
	/**
	 * 开通对象存储服务
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/addObsOpen")
	public String addObsOpen(HttpServletRequest request){
	    log.info("开通对象存储服务开始");
		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
		String userName=sessionUser.getUserName();
		JSONObject json=new JSONObject();
		String cusId=sessionUser.getCusId();
		String userId=sessionUser.getUserId();
		boolean isAdmin=sessionUser.getIsAdmin();
		try {
			json=obsOpenService.openObs(userName, userId, cusId,isAdmin);
		} catch (Exception e) {
			//异常不抛出,是为了当网络异常时,只展示前台的"由于网络异常，开通失败，请稍后重试",而不展示"系统繁忙，请稍后重试"
			log.error(e.getMessage(),e);
		}
		return JSONObject.toJSONString(json);
	}
	
	/**
	 * 查询当前用户开通服务状态
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getObsState")
	public String getObsState(HttpServletRequest request) {
		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession()
				.getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
		String cusId = sessionUser.getCusId();
		CusServiceState css=obsOpenService.getObsByCusId(cusId);
		
		return JSONObject.toJSONString(css);
	}
	/**
	 * 获取余额限定值
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unused")
    @ResponseBody
	@RequestMapping("/obsIsAllowOpen")
	public String obsIsAllowOpen(HttpServletRequest request) throws Exception{
		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
		String userName=sessionUser.getUserName();
		boolean isAdmin=sessionUser.getIsAdmin();
		JSONObject json=new JSONObject();
		String cusId=sessionUser.getCusId();
		String userId=sessionUser.getUserId();
		if(isAdmin){
			json=obsOpenService.isAllowOpen(userName, cusId);
		}else{
			json.put("state", "isNotAdmin");
		}
		return JSONObject.toJSONString(json);
	}
}

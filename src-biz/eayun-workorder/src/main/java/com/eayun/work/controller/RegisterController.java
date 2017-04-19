package com.eayun.work.controller;


import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.controller.BaseController;
import com.eayun.customer.model.Customer;
import com.eayun.work.service.RegisterService;

@Controller
@RequestMapping("/sys/register")
@Scope("prototype")
public class RegisterController extends BaseController{
	private static final Logger log = LoggerFactory.getLogger(RegisterController.class);
	@Autowired
	private RegisterService registerService;
	
	/**
	 * 注册用户
	 * ------------------
	 * @author zhouhaitao
	 * @param request
	 * @param customer
	 * @return
	 */
	@RequestMapping(value = "/register", method = RequestMethod.POST)
	@ResponseBody
	public String register(HttpServletRequest request,@RequestBody Customer customer) throws Exception{
		log.info("注册用户");
		JSONObject json = new JSONObject ();
		try{
			String str = registerService.register(request,customer);
			json.put("respCode", str);
		}catch(Exception e){
			json.put("respCode", "");
			throw e;
		}
		
		return json.toJSONString();
	}
	
	
	/**
	 * 根据客户邮箱或手机号码校验是否存在未审核或注册成功的用户<br>
	 * ------------------
	 * @author zhouhaitao
	 * 
	 * @param request
	 * @param customer
	 * @return
	 */
	@RequestMapping(value = "/checkCondition", method = RequestMethod.POST)
	@ResponseBody
	public String checkCustomerByCondition(HttpServletRequest request,@RequestBody Customer customer) throws Exception{
		log.info("校验注册客户的电子邮箱或手机或公司中文名");
		boolean flag = false; 
		JSONObject json = new JSONObject ();
		try{
			flag = registerService.checkCustomerByCondition(customer);
			json.put("flag", flag);
		}catch(Exception e){
			throw e;
		}
		
		return json.toJSONString();
	}
	
	/**
	 * 获取手机验证码<br>
	 * ------------------
	 * @author zhouhaitao
	 * 
	 * @param request
	 * @param customer
	 * @return
	 */
	@RequestMapping(value = "/getTeleCode", method = RequestMethod.POST)
	@ResponseBody
	public String getTeleCode(HttpServletRequest request,@RequestBody Map<String, String> map) throws Exception{
		log.info("获取注册手机验证码");
		JSONObject json = new JSONObject ();
		try{
			String telephone = map.get("telephone");
			String type =map.get("type");
			JSONObject teleJson = registerService.getTeleCode(telephone);
			request.getSession().setAttribute(telephone+"_"+type, teleJson.toJSONString());
			json.put("respCode", ConstantClazz.SUCCESS_CODE_OP);
		}catch(Exception e){
			json.put("respCode", ConstantClazz.ERROR_CODE);
			throw e;
		}
		
		return json.toJSONString();
	}
	
	
}

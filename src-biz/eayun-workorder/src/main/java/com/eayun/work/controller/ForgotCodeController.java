package com.eayun.work.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.controller.BaseController;
import com.eayun.customer.serivce.UserService;
import com.eayun.log.service.LogService;
import com.eayun.work.service.ForgotCodeService;

@Controller
@RequestMapping("/sys/forgotcode")
@Scope("prototype")
public class ForgotCodeController extends BaseController{
	
	@Autowired
	private ForgotCodeService forgotCodeService;
	@Autowired
	private UserService userService;
	@Autowired
	private LogService logService;
	
	@RequestMapping(value = "/firstCheck", method = RequestMethod.POST)
	@ResponseBody
	public String firstCheck(HttpServletRequest request,@RequestBody Map<String, String> map) throws Exception {
		String userAccount = map.get("userAccount");
		String idCode = map.get("idCode");
		String type = map.get("type");
		String rightIdCode = (String) request.getSession().getAttribute(type);
		request.getSession().setAttribute("userAccount_" + type, userAccount);
		JSONObject object = new JSONObject();
		try {
			forgotCodeService.firstCheck(userAccount,idCode,rightIdCode,object);
		} catch (Exception e) {
			throw e;
		}
		return JSON.toJSONString(object);
	}
	
	@RequestMapping(value = "/secondCheck", method = RequestMethod.POST)
	@ResponseBody
	public String secondCheck(HttpServletRequest request,@RequestBody Map<String, String> map) throws Exception{
		String userAccount = map.get("userAccount");
		String telephone = map.get("userPhone");
		String phoneCode = map.get("phoneCode");
		String type = map.get("type");
		String teleJsonStr = request.getSession().getAttribute(telephone + "_" + type)+"";
		JSONObject object = new JSONObject();
		try {
			forgotCodeService.secondCheck(userAccount, phoneCode, teleJsonStr, object);
		} catch (Exception e) {
			throw e;
		}
		return JSON.toJSONString(object);
	}
	
	@SuppressWarnings("rawtypes")
    @RequestMapping(value = "/modifyPassword", method = RequestMethod.POST)
	@ResponseBody
	public String modifyPassword(HttpServletRequest request,@RequestBody Map map) throws Exception {
		String userAccount = request.getSession().getAttribute("userAccount_forgotcode").toString();
		request.getSession().removeAttribute("userAccount_forgotcode");
		String password = map.get("password").toString();
		JSONObject object = new JSONObject();
		try {
			forgotCodeService.modifyPassword(userAccount,password,object);
		} catch (Exception e) {
			throw e;
		}
		if ("true".equals(object.get("done").toString())) {
			logService.addLog("忘记密码修改密码", userAccount, null, null, null, object.get("cusId").toString(), ConstantClazz.LOG_STATU_SUCCESS, null);
		} else {
			logService.addLog("忘记密码修改密码", userAccount, null, null, null, object.get("cusId").toString(), ConstantClazz.LOG_STATU_ERROR, null);
		}
		return JSON.toJSONString(object);
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
		JSONObject json = new JSONObject ();
		try{
			String type = map.get("type");
			String userAccount = request.getSession().getAttribute("userAccount_" + type).toString();
			JSONObject teleJson = forgotCodeService.getTeleCode(userAccount);
			request.getSession().setAttribute(teleJson.getString("telephone").toString() + "_" + type, teleJson.toJSONString());
			json.put("respCode", ConstantClazz.SUCCESS_CODE_OP);
		}catch(Exception e){
			json.put("respCode", ConstantClazz.ERROR_CODE);
			throw e;
		}
		return json.toJSONString();
	}

}

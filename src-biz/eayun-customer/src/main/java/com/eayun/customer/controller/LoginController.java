/**
 * 
 */
package com.eayun.customer.controller;

import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.HttpUtil;
import com.eayun.customer.serivce.LoginService;
import com.eayun.log.service.LogService;

/**
 * @author 陈鹏飞
 *
 */
@Controller
@RequestMapping("/sys/login")
@Scope("prototype")
public class LoginController extends BaseController{
	private static final Logger log = LoggerFactory.getLogger(LoginController.class);
	@Autowired
	private LoginService loginService;
	@Autowired
	private LogService logService;

	/**
	 * 登录
	 * 
	 * @param request
	 * @param customer
	 * @return
	 * @throws AppException
	 */
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	@ResponseBody
	public String login(HttpServletRequest request, HttpServletResponse response,
			@RequestBody Map<String, String> map) {
		log.info("登录,登录账户为:"+map.get("userAccount"));
		JSONObject json = new JSONObject ();
		
		String userAccount = map.get("userAccount");
		String Password = map.get("Password");
		String idCode = map.get("idCode");
		String type = map.get("type");
		String rightIdCode = (String) request.getSession().getAttribute(type);

		String key = (String) request.getSession().getAttribute(ConstantClazz.PASSWORD_SESSION);
		
		SessionUserInfo sessionUserInfo = null;
		
		try {
			sessionUserInfo = loginService.login(userAccount, Password, idCode, rightIdCode , key);
			// 登录成功
			if(sessionUserInfo.getUserId() != null){
				sessionUserInfo.setIP(HttpUtil.getRequestIP(request));
			    request.getSession().setAttribute(ConstantClazz.SYS_SESSION_USERINFO, sessionUserInfo);
			    // 设置cookie
			    Cookie userCookie = new Cookie("userAccount", "%22" + userAccount + "%22");
			    userCookie.setPath("/");
			    userCookie.setMaxAge(24 * 60 * 60 * 15);
			    response.addCookie(userCookie);
			}
			
			SessionUserInfo cookieUserInfo = new SessionUserInfo();
			BeanUtils.copyPropertiesByModel(cookieUserInfo, sessionUserInfo);
			cookieUserInfo.setEmail(null);
			cookieUserInfo.setPhone(null);
			
			json.put("userInfo", cookieUserInfo);
			json.put("respCode", ConstantClazz.SUCCESS_CODE_OP);
			if(sessionUserInfo.getError()==null){
				logService.addLog(ConstantClazz.LOG_LOGIN, "系统", null, null,ConstantClazz.LOG_STATU_SUCCESS, null);
			}else{
				Exception e=new Exception(sessionUserInfo.getError());
				logService.addLog(ConstantClazz.LOG_LOGIN, userAccount,"系统", null, null,null,ConstantClazz.LOG_STATU_ERROR, e);
			}
		} catch (Exception e) {
		    log.error("登录失败", e);
			json.put("respCode", ConstantClazz.ERROR_CODE);
			logService.addLog(ConstantClazz.LOG_LOGIN, "系统", null, null,ConstantClazz.LOG_STATU_ERROR, e);
			throw e;
		}
		return json.toJSONString();
	}
	
	@RequestMapping(value = "/logout", method = RequestMethod.POST)
	@ResponseBody
	public String logout(HttpServletRequest request){
	    log.info("退出");
		JSONObject json = new JSONObject ();
		SessionUserInfo sessionUserInfo = (SessionUserInfo)request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
	    try{
            logService.addLog(ConstantClazz.LOG_LOGOUT,sessionUserInfo.getUserName(),"系统",null,null, 
                sessionUserInfo.getCusId(),ConstantClazz.LOG_STATU_SUCCESS, null);
            request.getSession().removeAttribute(ConstantClazz.SYS_SESSION_USERINFO);
            json.put("respCode", ConstantClazz.SUCCESS_CODE_OP);
            
        }catch(Exception e){
            log.error("退出失败", e);
            logService.addLog(ConstantClazz.LOG_LOGOUT,sessionUserInfo.getUserName(),"系统",null,null, 
                sessionUserInfo.getCusId(),ConstantClazz.LOG_STATU_SUCCESS, null);
            json.put("respCode", ConstantClazz.ERROR_CODE);
            throw e;
        }
		return json.toString();
	}
	
	@RequestMapping(value = "/getPassKey", method = RequestMethod.POST)
    @ResponseBody
    public String getPassKey(HttpServletRequest request){
	    int key = (int)((Math.random()*9+1)*100000);
        String passKey = String.valueOf(key);
        request.getSession().setAttribute(ConstantClazz.PASSWORD_SESSION, passKey);
        return JSONObject.toJSONString(passKey);
	}
}
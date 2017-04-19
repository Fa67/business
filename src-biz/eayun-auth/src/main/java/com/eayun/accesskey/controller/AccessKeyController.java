package com.eayun.accesskey.controller;


import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eayun.accesskey.model.AccessKey;
import com.eayun.accesskey.service.AccessKeyService;
import com.eayun.accesskey.service.AkSmsService;
import com.eayun.common.ConstantClazz;
import com.eayun.common.controller.BaseController;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.customer.model.User;
import com.eayun.customer.serivce.UserService;
import com.eayun.obs.service.ObsOpenService;
/**
 * accessKey管理
 * @author xiangyu.cao@eayun.com
 *
 * @date 2016年1月12日 上午9:01:42
 */
@Controller
@RequestMapping("/obs/accessKey")
@Scope("prototype")
public class AccessKeyController extends BaseController {
	private static final Logger log = LoggerFactory
			.getLogger(AccessKeyController.class);
	@Autowired
	private AccessKeyService accessKeyService;
	@Autowired
	private AkSmsService akSmsService;
	@Autowired
	private UserService userService;
	@Autowired
	private ObsOpenService obsOpenService;
	
	@ResponseBody
	@RequestMapping("/getAcckListPage")
	public String getAcckListPage(HttpServletRequest request,Page page) throws Exception{
		log.info("开始获取密钥列表");
		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
		String cusId=sessionUser.getCusId();
		page= accessKeyService.getAKListPage(cusId);
		return JSONObject.toJSONString(page);
	}
	
	
	/**
	 * 是否需要短信验证
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping("/akOperator")
	public String akOperator(HttpServletRequest request) throws Exception{
		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
		boolean isOpenServiceAndWhiteList=obsOpenService.isOpenObsServiceAndWhiteList(sessionUser.getUserName());
		String state="false";
		if(isOpenServiceAndWhiteList){
			Date time=sessionUser.getLastVerifySmsTime();
			state=accessKeyService.operatorIsPass(time);
		}else{
			state="notWhiteList";
		}
		return JSON.toJSONString(state);
	}
	/**
	 * 开启ak
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
    @ResponseBody
	@RequestMapping("/startAcck")
	public String startAcck(HttpServletRequest request,@RequestBody Map map) throws Exception{
		log.info("开始启用ak");
		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
		String cusId=sessionUser.getCusId();
		map.put("userId", cusId);
		AccessKey ak=accessKeyService.startAcck(map);
		return JSON.toJSONString(ak);
	}
	/**
	 * 停用ak
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("rawtypes")
    @ResponseBody
	@RequestMapping("/blockAcck")
	public String blockAcck(HttpServletRequest request,@RequestBody Map map) throws Exception{
		log.info("开始停用ak");
		
		AccessKey ak=accessKeyService.blockAcck(map);
		return JSON.toJSONString(ak);
	}
	/**
	 * 删除ak
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("rawtypes")
    @ResponseBody
	@RequestMapping("/deleteAcck")
	public String deleteAcck(HttpServletRequest request,@RequestBody Map map) throws Exception{
		log.info("开始删除ak");
		
		String state=accessKeyService.deleteAcck(map);
		return JSON.toJSONString(state);
	}
	
	/**
	 * 创建ak
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping("/addAcck")
	public String addAcck(HttpServletRequest request) throws Exception{
		log.info("开始创建ak");
		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
		boolean isOpenServiceAndWhiteList=obsOpenService.isOpenObsServiceAndWhiteList(sessionUser.getUserName());
		if(isOpenServiceAndWhiteList){
			String cusId=sessionUser.getCusId();
			AccessKey ak=accessKeyService.addAcck(cusId);
			return JSON.toJSONString(ak);
		}else{
			throw new AppException("服务暂缓开通");
		}
	}
	/**
	 * 发送短信
	 * @param request
	 * @param params
	 * @return
	 */
	@SuppressWarnings("rawtypes")
    @ResponseBody
	@RequestMapping("/sendSMS")
	public String sendSMS(HttpServletRequest request , @RequestBody Map params){
		log.info("开始发送短信验证码");
		 SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
	     String userId = sessionUser.getUserId();
	     User user=userService.findUserById(userId);
	     String phone = user!=null?user.getUserPhone():null;
	     try {
	    	 akSmsService.sendValidSms(phone , userId);
	        } catch (Exception e) {
	            log.error("发送验证短信失败", e);
	            throw e;
	        }
	     return JSONObject.toJSONString("success");
	}
	/**
	 * 短信验证码验证
	 * @param request
	 * @param params
	 * @return
	 */
	 @SuppressWarnings("rawtypes")
    @ResponseBody
	 @RequestMapping(value = "/checkCode")
	 public String checkCode(HttpServletRequest request , @RequestBody Map params) {
	        log.info("验证手机验证码开始");
	        String verCode = params.get("verCode").toString();
	        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
	        String userId = sessionUser.getUserId();
	        User user=userService.findUserById(userId);
		    String phone = user!=null?user.getUserPhone():null;
	        boolean isRight = accessKeyService.checkCode(userId , verCode , phone);
	        sessionUser.setLastVerifySmsTime(new Date());
	        return JSONObject.toJSONString(isRight);
	    }
	 /**
	  * 更改显示状态
	  * @param request
	  * @param params
	  * @return
	 * @throws Exception 
	  */
	 @SuppressWarnings("rawtypes")
    @ResponseBody
	 @RequestMapping(value = "/changeShow")
	 public String changeShow(HttpServletRequest request , @RequestBody Map params) throws Exception {
	        log.info("开始更改显示状态");
	        AccessKey ak=accessKeyService.checkShow(params);
	        return JSONObject.toJSONString(ak);
	    }
	 /**
	  * 更改显示状态
	  * @param request
	  * @param params
	  * @return
	 * @throws Exception 
	  */
	 @ResponseBody
	 @RequestMapping(value = "/flush")
	 public String flush(HttpServletRequest request) throws Exception {
		 	log.info("开始获取密钥列表");
			SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
			String cusId=sessionUser.getCusId();
			String result=accessKeyService.flush(cusId);
	        return JSONObject.toJSONString(result);
	    }
	 /**
	  * 手机号码是否已经验证通过
	  * @param request
	  * @param params
	  * @return
	 * @throws Exception 
	  */
	 @ResponseBody
	 @RequestMapping(value = "/checkphoneispass")
	 public String checkPhoneIsPass(HttpServletRequest request) throws Exception {
		 	log.info("开始验证该用户手机号是否通过验证");
			SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
			String userId=sessionUser.getUserId();
			EayunResponseJson json = new EayunResponseJson();
			try {
				accessKeyService.checkPhoneIsPass(userId);
				json.setRespCode(ConstantClazz.SUCCESS_CODE);
			} catch (AppException e) {
				json.setRespCode(ConstantClazz.ERROR_CODE);
			}
			
	        return JSONObject.toJSONString(json);
	    }
}

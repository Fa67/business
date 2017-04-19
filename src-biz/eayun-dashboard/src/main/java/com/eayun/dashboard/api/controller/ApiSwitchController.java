package com.eayun.dashboard.api.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.util.StringUtil;
import com.eayun.dashboard.api.service.ApiSwitchService;
import com.eayun.log.ecmcsevice.EcmcLogService;

@Controller
@RequestMapping("/ecmc/api/switch")
public class ApiSwitchController {

	private static final Logger log = LoggerFactory.getLogger(ApiSwitchController.class); 
	@Autowired
	private ApiSwitchService apiSwitchService;
	
	@Autowired
	private EcmcLogService ecmcLogService;

	
    @RequestMapping(value = "/getapiswitch" , method = RequestMethod.POST)
    @ResponseBody
    public String getApiSwitch (HttpServletRequest request , @RequestBody Map map)throws Exception {
    	log.info("查询数据中心下API开关状态");
    	EayunResponseJson json = new EayunResponseJson();
    	String dcId = null == map.get("dcId")?"":map.get("dcId").toString();
        try {
        	boolean isOpen = apiSwitchService.getApiSwitch(dcId);
        	json.setData(isOpen);
        	json.setRespCode(ConstantClazz.SUCCESS_CODE);
        } catch (Exception e) {
        	json.setRespCode(ConstantClazz.ERROR_CODE);
        	json.setMessage(e.getMessage());
			log.error(e.getMessage(),e);
			throw e;
		}
    	return JSONObject.toJSONString(json);
    }
    
    @RequestMapping(value = "/getcodeforapiswitch" , method = RequestMethod.POST)
    @ResponseBody
    public String getCodeForApiSwitch (HttpServletRequest request , @RequestBody Map map)throws Exception {
    	log.info("开启/关闭API开关发送验证码");
    	EayunResponseJson json = new EayunResponseJson();
    	String dcId = null == map.get("dcId")?"":map.get("dcId").toString();
    	String operation = null == map.get("operation")?"":map.get("operation").toString();
    	String currentPhone = (String) request.getSession().getAttribute(ConstantClazz.CURRENT_PHONE);
        try {
        	apiSwitchService.getCodeForApiSwitch(dcId,currentPhone,operation);
        	json.setRespCode(ConstantClazz.SUCCESS_CODE);
        } catch (Exception e) {
        	json.setRespCode(ConstantClazz.ERROR_CODE);
        	json.setMessage(e.getMessage());
			log.error(e.getMessage(),e);
			throw e;
		}
    	return JSONObject.toJSONString(json);
    }
    
    @RequestMapping(value = "/operationapiswitch" , method = RequestMethod.POST)
    @ResponseBody
    public String operationApiSwitch (HttpServletRequest request , @RequestBody Map map)throws Exception {
    	log.info("开启/关闭API开关");
    	EayunResponseJson json = new EayunResponseJson();
    	String cusId = null == map.get("cusId")?"":map.get("cusId").toString();
        String operation = null == map.get("operation")?"":map.get("operation").toString();
        String code = null == map.get("code")?"":map.get("code").toString();
        String dcId = null == map.get("dcId")?"":map.get("dcId").toString();
        String currentPhone = (String) request.getSession().getAttribute(ConstantClazz.CURRENT_PHONE);
        try {
        	apiSwitchService.operationApiSwitch(cusId,operation,code,dcId,currentPhone);
        	json.setRespCode(ConstantClazz.SUCCESS_CODE);
        	ecmcLogService.addLog("0".equals(operation)?"关闭-API总开关":"开启-API总开关",
        			"API管理", "API开关", null, 1, null, null);
        } catch (Exception e) {
        	json.setRespCode(ConstantClazz.ERROR_CODE);
        	json.setMessage(e.getMessage());
			log.error(e.getMessage(),e);
			ecmcLogService.addLog("0".equals(operation)?"关闭-API总开关":"开启-API总开关",
					"API管理", "API开关", null, 0, null, e);
			throw e;
		}
    	return JSONObject.toJSONString(json);
    }
    
    @RequestMapping(value = "/getapiswitchphone" , method = RequestMethod.POST)
    @ResponseBody
    public String getApiSwitchPhone  (HttpServletRequest request)throws Exception {
    	log.info("查询API最高权限的手机号码");
    	EayunResponseJson json = new EayunResponseJson();
        try {
        	String phone = apiSwitchService.getApiSwitchPhone();
        	if(!StringUtil.isEmpty(phone)){
        		request.getSession().setAttribute(ConstantClazz.CURRENT_PHONE, phone);
        		phone = phone.substring(0,3)+"****"+phone.substring(7,11);
        	}
        	json.setData(phone);
        	json.setRespCode(ConstantClazz.SUCCESS_CODE);
        } catch (Exception e) {
        	json.setRespCode(ConstantClazz.ERROR_CODE);
        	json.setMessage(e.getMessage());
			log.error(e.getMessage(),e);
		}
    	return JSONObject.toJSONString(json);
    }
    
    @RequestMapping(value = "/sendapiphonecode" , method = RequestMethod.POST)
    @ResponseBody
    public String sendApiPhoneCode  (HttpServletRequest request , @RequestBody Map map)throws Exception {
    	log.info("修改最高权限手机号码发送验证码");
    	EayunResponseJson json = new EayunResponseJson();
    	String type = null == map.get("type")?"":map.get("type").toString();
    	String newPhone = null == map.get("newPhone")?"":map.get("newPhone").toString();
    	String currentPhone = (String) request.getSession().getAttribute(ConstantClazz.CURRENT_PHONE);
        try {
        	apiSwitchService.sendApiPhoneCode(type,currentPhone,newPhone);
        	json.setRespCode(ConstantClazz.SUCCESS_CODE);
        } catch (Exception e) {
        	json.setRespCode(ConstantClazz.ERROR_CODE);
        	json.setMessage(e.getMessage());
			log.error(e.getMessage(),e);
			throw e;
		}
    	return JSONObject.toJSONString(json);
    }
    
    @RequestMapping(value = "/verifyapiphonecode" , method = RequestMethod.POST)
    @ResponseBody
    public String verifyApiPhoneCode  (HttpServletRequest request , @RequestBody Map map)throws Exception {
    	log.info("校验验证码是否正确");
    	EayunResponseJson json = new EayunResponseJson();
    	String code = null == map.get("code")?"":map.get("code").toString();
    	String currentPhone = (String) request.getSession().getAttribute(ConstantClazz.CURRENT_PHONE);
    	boolean isok = false;
        try {
        	isok = apiSwitchService.verifyApiPhoneCode(code,currentPhone);
        	json.setData(isok);
        	json.setRespCode(ConstantClazz.SUCCESS_CODE);
        } catch (Exception e) {
        	json.setRespCode(ConstantClazz.ERROR_CODE);
        	json.setMessage(e.getMessage());
			log.error(e.getMessage(),e);
			throw e;
		}
    	return JSONObject.toJSONString(json);
    }
    
    @RequestMapping(value = "/editapiswitchphone" , method = RequestMethod.POST)
    @ResponseBody
    public String editApiSwitchPhone  (HttpServletRequest request , @RequestBody Map map)throws Exception {
    	log.info("修改最高权限手机号码");
    	EayunResponseJson json = new EayunResponseJson();
    	String code = null == map.get("code")?"":map.get("code").toString();
    	String newPhone = null == map.get("newPhone")?"":map.get("newPhone").toString();
    	String logType = null == map.get("logType")?"":map.get("logType").toString();
    	String type = "修改手机号";
    	if("0".equals(logType)){
    		type = "绑定手机号";
    	}
    	boolean isok = false;
        try {
        	isok = apiSwitchService.editApiSwitchPhone(code,newPhone);
        	json.setData(isok);
        	json.setRespCode(ConstantClazz.SUCCESS_CODE);
        	ecmcLogService.addLog(type,"API管理", "API总开关权限", null, 1, null, null);
        } catch (Exception e) {
        	json.setRespCode(ConstantClazz.ERROR_CODE);
        	json.setMessage(e.getMessage());
			log.error(e.getMessage(),e);
			ecmcLogService.addLog(type,"API管理", "API总开关权限", null, 0, null, e);
			throw e;
		}
    	return JSONObject.toJSONString(json);
    }
}

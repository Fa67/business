package com.eayun.dashboard.volumetype.controller;

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
import com.eayun.dashboard.api.model.BaseApiSwitchPhone;
import com.eayun.dashboard.volumetype.service.VolumeTypeVerifyService;
import com.eayun.log.ecmcsevice.EcmcLogService;

@Controller
@RequestMapping("/ecmc/verify/volumetype")
public class VolumeTypeVerifyController {

	private static final Logger log = LoggerFactory.getLogger(VolumeTypeVerifyController.class); 
	@Autowired
	private VolumeTypeVerifyService volumeTypeVerifyService;
	
	@Autowired
	private EcmcLogService ecmcLogService;

	
	/**
	 * 获取云硬盘类型最高操作权限手机号和名称
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/getvolumetypephone" , method = RequestMethod.POST)
    @ResponseBody
    public String getVolumeTypePhone  (HttpServletRequest request)throws Exception {
    	log.info("查询云硬盘类型最高权限的手机号码");
    	EayunResponseJson json = new EayunResponseJson();
        try {
        	BaseApiSwitchPhone phone = volumeTypeVerifyService.getVolumeTypePhone();
        	if(null!=phone&&null!=phone.getPhone()&&!"".equals(phone.getPhone())){
        		request.getSession().setAttribute(ConstantClazz.VOlUMETYPE_PHONE, phone.getPhone());
        		String phoneNumber = phone.getPhone().substring(0,3)+"****"+phone.getPhone().substring(7,11);
        		phone.setPhone(phoneNumber);
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
	
	
	/**
	 * 绑定新手机号时发送验证码
	 * @param request
	 * @param map
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/sendvolumetypephonecode" , method = RequestMethod.POST)
    @ResponseBody
    public String sendApiPhoneCode  (HttpServletRequest request , @RequestBody Map map)throws Exception {
    	log.info("修改最高权限手机号码发送验证码");
    	EayunResponseJson json = new EayunResponseJson();
    	String type = null == map.get("type")?"":map.get("type").toString();
    	String newPhone = null == map.get("newPhone")?"":map.get("newPhone").toString();
    	String volumePhone = (String) request.getSession().getAttribute(ConstantClazz.VOlUMETYPE_PHONE);
        try {
        	volumeTypeVerifyService.sendVolumeTypePhoneCode(type,volumePhone,newPhone);
        	json.setRespCode(ConstantClazz.SUCCESS_CODE);
        } catch (Exception e) {
        	json.setRespCode(ConstantClazz.ERROR_CODE);
        	json.setMessage(e.getMessage());
			log.error(e.getMessage(),e);
			throw e;
		}
    	return JSONObject.toJSONString(json);
    }
	
	
	/**
	 * 修改最高权限手机号码
	 */
	@RequestMapping(value = "/editvolumetypephone" , method = RequestMethod.POST)
    @ResponseBody
    public String editVolumeTypePhone  (HttpServletRequest request , @RequestBody Map map)throws Exception {
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
        	isok = volumeTypeVerifyService.editVolumeTypePhone(code,newPhone);
        	json.setData(isok);
        	json.setRespCode(ConstantClazz.SUCCESS_CODE);
        	ecmcLogService.addLog(type,ConstantClazz.LOG_TYPE_OPERATIONAL, "硬盘分类限速", null, 1, null, null);
        } catch (Exception e) {
        	json.setRespCode(ConstantClazz.ERROR_CODE);
        	json.setMessage(e.getMessage());
			log.error(e.getMessage(),e);
			ecmcLogService.addLog(type,ConstantClazz.LOG_TYPE_OPERATIONAL, "硬盘分类限速", null, 0, null, e);
			throw e;
		}
    	return JSONObject.toJSONString(json);
    }
	
	
	/**
	 * 修改联系人
	 */
	@RequestMapping(value = "/editvolumetypeperson" , method = RequestMethod.POST)
    @ResponseBody
    public String editVolumeTypePerson  (HttpServletRequest request , @RequestBody Map map)throws Exception {
    	log.info("修改联系人");
    	EayunResponseJson json = new EayunResponseJson();
    	String newName = null == map.get("name")?"":map.get("name").toString();
    	
    	boolean isok = false;
        try {
        	isok = volumeTypeVerifyService.editVolumeTypePerson(newName);
        	json.setData(isok);
        	json.setRespCode(ConstantClazz.SUCCESS_CODE);
        	ecmcLogService.addLog("编辑联系人",ConstantClazz.LOG_TYPE_OPERATIONAL, "硬盘分类限速", null, 1, null, null);
        } catch (Exception e) {
        	json.setRespCode(ConstantClazz.ERROR_CODE);
        	json.setMessage(e.getMessage());
			log.error(e.getMessage(),e);
			ecmcLogService.addLog("编辑联系人",ConstantClazz.LOG_TYPE_OPERATIONAL, "硬盘分类限速", null, 0, null, e);
			throw e;
		}
    	return JSONObject.toJSONString(json);
    }
	
	
	/**
	 * 获取云硬盘分类限速验证码
	 * @param request
	 * @param map
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/getcodeforvolumetype" , method = RequestMethod.POST)
    @ResponseBody
    public String getCodeForVolumeType (HttpServletRequest request , @RequestBody Map map)throws Exception {
    	log.info("云硬盘分类限速发送验证码");
    	EayunResponseJson json = new EayunResponseJson();
    	String volumePhone = (String) request.getSession().getAttribute(ConstantClazz.VOlUMETYPE_PHONE);
        try {
        	volumeTypeVerifyService.getCodeForVolumeType(volumePhone);
        	json.setRespCode(ConstantClazz.SUCCESS_CODE);
        } catch (Exception e) {
        	json.setRespCode(ConstantClazz.ERROR_CODE);
        	json.setMessage(e.getMessage());
			log.error(e.getMessage(),e);
			throw e;
		}
    	return JSONObject.toJSONString(json);
    }
	
	
	/**
	 * 验证操作权限验证码
	 */
	@RequestMapping(value = "/checkvolumephonecode" , method = RequestMethod.POST)
    @ResponseBody
    public String checkVolumePhoneCode  (HttpServletRequest request , @RequestBody Map map)throws Exception {
    	log.info("校验最高权限手机号码");
    	EayunResponseJson json = new EayunResponseJson();
    	String code = null == map.get("code")?"":map.get("code").toString();
    	String volumePhone = (String) request.getSession().getAttribute(ConstantClazz.VOlUMETYPE_PHONE);
    	boolean isok = false;
        try {
        	isok = volumeTypeVerifyService.checkVolumePhoneCode(code,volumePhone);
        	if(isok){
        		request.getSession().setAttribute(ConstantClazz.VOlUMETYPE_PHONE_SUCCESS, volumePhone);
        	}
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
	
	/**
	 * 查看是否已经获取了操作权限
	 * @param request
	 * @param map
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/ischeckedphone" , method = RequestMethod.POST)
    @ResponseBody
    public String isCheckedPhone  (HttpServletRequest request , @RequestBody Map map)throws Exception {
    	log.info("查询是否已经成功获取了操作权限");
    	EayunResponseJson json = new EayunResponseJson();
    	String volumePhone = (String) request.getSession().getAttribute(ConstantClazz.VOlUMETYPE_PHONE_SUCCESS);
    	boolean isok = false;
        try {
        	isok = volumeTypeVerifyService.checkVolumePhone(volumePhone);
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
	
	
    
}

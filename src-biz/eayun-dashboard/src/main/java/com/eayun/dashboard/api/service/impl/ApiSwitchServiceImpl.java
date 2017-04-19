package com.eayun.dashboard.api.service.impl;


import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.constant.PhoneVerify;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.dashboard.api.dao.ApiSwitchDao;
import com.eayun.dashboard.api.model.BaseApiSwitchPhone;
import com.eayun.dashboard.api.service.ApiSwitchService;
import com.eayun.datacenter.ecmcservice.EcmcDataCenterService;
import com.eayun.datacenter.model.DcDataCenter;
import com.eayun.sms.service.SMSService;

@Service
@Transactional
public class ApiSwitchServiceImpl implements ApiSwitchService {
	
	private static final Logger log = LoggerFactory.getLogger(ApiSwitchServiceImpl.class);

	@Autowired
	private ApiSwitchDao apiSwitchDao;
	
	@Autowired
	private SMSService smsService;
	
	@Autowired
	private JedisUtil jedisUtil;
	
	@Autowired
	private EcmcDataCenterService ecmcDataCenterService;
	
	@Override
	public boolean getApiSwitch(String dcId) {
		log.info("查询数据中心下API开关状态实现...");
		boolean isOpen = true;
		DcDataCenter dc = ecmcDataCenterService.getdatacenterbyid(dcId);
		if(null == dc){
			throw new AppException("数据中心不存在！");
		}
		if(null != dc.getApiStatus()){
			isOpen =  dc.getApiStatus();
		}
		return isOpen;
	}

	@Override
	public void getCodeForApiSwitch(String dcId,String currentPhone,String operation) {
		log.info("开启/关闭API开关发送验证码实现...");
		DcDataCenter dc = ecmcDataCenterService.getdatacenterbyid(dcId);
		if(null == dc){
			throw new AppException("数据中心不存在！");
		}
		String phone = this.getApiSwitchPhone();
		if(StringUtil.isEmpty(phone)){
			throw new AppException("请先绑定手机号！");
		}
		if(!phone.equals(currentPhone)){
			throw new AppException("发现手机号变更！");
		}
		int code = (int)((Math.random()*9+1)*100000);
        String phoneCode = String.valueOf(code);
        try {
			jedisUtil.setEx(RedisKey.API_SWITCH_CODE+dcId+":"+phone, phoneCode, 300);
		} catch (Exception e1) {
			log.error(e1.toString(),e1);
			throw new AppException("redis连接失败！");
		}
        String operationName = "";
        if("0".equals(operation)){
        	operationName = "关闭";
        }else if("1".equals(operation)){
        	operationName = "开启";
        }
        String content = "您正在运维中心，执行"+operationName+dc.getName()+"API开关操作，验证码为："
        		+phoneCode+"；此操作可能影响客户操作，请谨慎使用；此验证码5分钟内有效。";
        List<String> mobiles = new ArrayList<>();
        mobiles.add(phone);
        try {
            smsService.send(content, mobiles);
        } catch (Exception e) {
            log.error("信息发送失败", e);
            throw new AppException("信息发送失败");
        }
	}
	
	@Override
	public void operationApiSwitch(String cusId, String operation, String code, String dcId, String currentPhone) {
		log.info("开启/关闭API开关实现...");
		if (StringUtil.isEmpty(code)) {
            throw new AppException("请输入验证码!");
        }
		String phone = this.getApiSwitchPhone();
		if(StringUtil.isEmpty(phone)){
			throw new AppException("请先绑定手机号！");
		}
		if(!phone.equals(currentPhone)){
			throw new AppException("发现手机号变更！");
		}
		try {
			String phoneCode = jedisUtil.get(RedisKey.API_SWITCH_CODE+dcId+":"+phone);
			if (StringUtil.isEmpty(phoneCode) || (!code.equals(phoneCode))) {
	            throw new AppException("手机验证码不正确，请重新输入！");
	        }
			jedisUtil.delete(RedisKey.API_SWITCH_CODE+dcId+":"+phone);
			int count = ecmcDataCenterService.operationApiSwitchById(operation, dcId);
			if(count == 0){
				throw new AppException("开启失败！");
			}
	        jedisUtil.set(RedisKey.API_SWITCH_STATUS+dcId, operation);
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			throw new AppException("redis连接失败！");
		}
	}

	@Override
	public String getApiSwitchPhone() {
		log.info("查询API最高权限的手机号码实现...");
		String phone = "";
		List<String> phones = apiSwitchDao.getPublicPhone(PhoneVerify.TypeApi);
		if(!phones.isEmpty()){
			phone = phones.get(0);
		}
		return phone;
	}

	@Override
	public void sendApiPhoneCode(String type,String currentPhone,String newPhone) {
		log.info("修改最高权限手机号码发送验证码实现...");
		String phone = this.getApiSwitchPhone();
		if(StringUtil.isEmpty(phone) && "old".equals(type)){
			throw new AppException("手机号码未设置！");
		}
		if(!phone.equals(currentPhone) && "old".equals(type)){
			throw new AppException("发现手机号变更！");
		}
		int code = (int)((Math.random()*9+1)*100000);
        String phoneCode = String.valueOf(code);
        try {
        	if("new".equals(type)){
        		jedisUtil.setEx(RedisKey.API_SWITCH_PHONE_NEW+newPhone, phoneCode, 300);
        	}else{
        		jedisUtil.setEx(RedisKey.API_SWITCH_PHONE_OLD+phone, phoneCode, 300);
        	}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			throw new AppException("redis连接失败！");
		}
        String operationName = "绑定";
        if("old".equals(type)){
        	operationName = "解除";
        }
        String content = "您正在运维中心，"+operationName+"API开关权限，验证码为："
        		+phoneCode+"；此验证码5分钟内有效。";
        List<String> mobiles = new ArrayList<>();
        if("old".equals(type)){
        	mobiles.add(phone);
    	}else{
    		mobiles.add(newPhone);
    	}
        try {
            smsService.send(content, mobiles);
        } catch (Exception e) {
            log.error("信息发送失败", e);
            throw new AppException("信息发送失败");
        }
	}

	@Override
	public boolean verifyApiPhoneCode(String code, String currentPhone) {
		if (StringUtil.isEmpty(code)) {
            throw new AppException("请输入验证码!");
        }
		String phone = this.getApiSwitchPhone();
		if(StringUtil.isEmpty(phone)){
			throw new AppException("手机号码未设置！");
		}
		if(!phone.equals(currentPhone)){
			throw new AppException("发现手机号变更！");
		}
		try {
			String phoneCode = jedisUtil.get(RedisKey.API_SWITCH_PHONE_OLD+phone);
			if (StringUtil.isEmpty(phoneCode) || (!code.equals(phoneCode))) {
	            throw new AppException("手机验证码不正确，请重新输入！");
	        }else{
	        	jedisUtil.delete(RedisKey.API_SWITCH_PHONE_OLD+phone);
	        	return true;
	        }
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			throw new AppException("redis连接失败！");
		}
	}

	@Override
	public boolean editApiSwitchPhone(String code, String newPhone){
		log.info("修改最高权限手机号码实现...");
		if (StringUtil.isEmpty(code)) {
            throw new AppException("请输入验证码!");
        }
		try {
			String phoneCode = jedisUtil.get(RedisKey.API_SWITCH_PHONE_NEW+newPhone);
			if (StringUtil.isEmpty(phoneCode) || (!code.equals(phoneCode))) {
	            throw new AppException("手机验证码不正确，请重新输入！");
	        }else{
	        	String phone = this.getApiSwitchPhone();
	        	jedisUtil.delete(RedisKey.API_SWITCH_PHONE_NEW+newPhone);
	        	if(StringUtil.isEmpty(phone)){
	        		BaseApiSwitchPhone baseApiSwitchPhone = new BaseApiSwitchPhone();
		        	baseApiSwitchPhone.setPhone(newPhone);
		        	baseApiSwitchPhone.setType(PhoneVerify.TypeApi);
		        	apiSwitchDao.saveEntity(baseApiSwitchPhone);
	        	}else{
	        		StringBuffer sb = new StringBuffer();
	    	        sb.append("update BaseApiSwitchPhone set phone=? where type='1' ");
	    	        int count = apiSwitchDao.executeUpdate(sb.toString(), newPhone);
	        	}
	        	return true;
	        }
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			throw new AppException("redis连接失败！");
		}
	}
}

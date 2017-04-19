package com.eayun.dashboard.volumetype.service.impl;


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
import com.eayun.dashboard.volumetype.service.VolumeTypeVerifyService;
import com.eayun.datacenter.ecmcservice.EcmcDataCenterService;
import com.eayun.datacenter.model.DcDataCenter;
import com.eayun.sms.service.SMSService;

@Service
@Transactional
public class VolumeTypeVerifyServiceImpl implements VolumeTypeVerifyService {
	
	private static final Logger log = LoggerFactory.getLogger(VolumeTypeVerifyServiceImpl.class);

	@Autowired
	private ApiSwitchDao apiSwitchDao;
	
	@Autowired
	private SMSService smsService;
	
	@Autowired
	private JedisUtil jedisUtil;
	
	@Autowired
	private EcmcDataCenterService ecmcDataCenterService;
	
	
	
	
	@Override
	public BaseApiSwitchPhone getVolumeTypePhone() {
		log.info("查询云硬盘类型最高权限的手机号码实现...");
		BaseApiSwitchPhone phone = null;
		List<BaseApiSwitchPhone> phones = apiSwitchDao.getVolumeTypePhone(PhoneVerify.TypeVolType);
		if(!phones.isEmpty()){
			phone = phones.get(0);
		}
		return phone;
	}
	
	
	
	@Override
	public void getCodeForVolumeType(String volumePhone) {
		log.info("云硬盘分类限速发送验证码实现...");
		BaseApiSwitchPhone phone = this.getVolumeTypePhone();
		if(null==phone||StringUtil.isEmpty(phone.getPhone())){
			throw new AppException("操作验证管理下未设置手机号，请联系超级管理员绑定手机号后再操作！");
		}
		if(null!=phone.getPhone()&&!phone.getPhone().equals(volumePhone)){
			throw new AppException("发现手机号变更！");
		}
		int code = (int)((Math.random()*9+1)*100000);
        String phoneCode = String.valueOf(code);
        try {
			jedisUtil.setEx(RedisKey.VOLUMETYPE_CODE+phone.getPhone(), phoneCode, 300);
		} catch (Exception e1) {
			log.error(e1.toString(),e1);
			throw new AppException("redis连接失败！");
		}
       
        String content = "您正在运维中心，执行云硬盘分类限速手机号验证操作，验证码为："
        		+phoneCode+"；此操作可能影响客户操作，请谨慎使用；此验证码5分钟内有效。";
        List<String> mobiles = new ArrayList<>();
        mobiles.add(phone.getPhone());
        try {
            smsService.send(content, mobiles);
        } catch (Exception e) {
            log.error("信息发送失败", e);
            throw new AppException("信息发送失败");
        }
	}
	
	
	
	@Override
	public boolean checkVolumePhoneCode(String code,String volumePhone) {
		log.info("校验最高权限手机验证码实现...");
		if (StringUtil.isEmpty(code)) {
            throw new AppException("请输入验证码!");
        }
		BaseApiSwitchPhone phone=getVolumeTypePhone();
		if(null!=phone&&null!=phone.getPhone()&&!phone.getPhone().equals(volumePhone)){
			throw new AppException("发现手机号变更！");
		}
		try {
			String phoneCode = jedisUtil.get(RedisKey.VOLUMETYPE_CODE+phone.getPhone());
			if (StringUtil.isEmpty(phoneCode) || (!code.equals(phoneCode))) {
	            throw new AppException("验证码不正确，请核对后重新输入");
	        }else{
	        	jedisUtil.delete(RedisKey.VOLUMETYPE_CODE+volumePhone);
	        	return true;
	        }
		}catch(AppException e){
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			throw new AppException("redis连接失败！");
		}
	}



	
	@Override
	public boolean checkVolumePhone(String volumePhone) {
		log.info("查看是否成功获取了操作权限");
		if (StringUtil.isEmpty(volumePhone)) {
            return false;
        }
		BaseApiSwitchPhone phone=getVolumeTypePhone();
		if(null==phone||null==phone.getPhone()||!phone.getPhone().equals(volumePhone)){
			return false;
		}
		return true;
	}

	




	@Override
	public void sendVolumeTypePhoneCode(String type, String volumePhone,
			String newPhone) {
		log.info("修改最高权限手机号码发送验证码实现...");
		if(StringUtil.isEmpty(newPhone) ){
			throw new AppException("手机号码未设置！");
		}
		BaseApiSwitchPhone phone = this.getVolumeTypePhone();
		int code = (int)((Math.random()*9+1)*100000);
        String phoneCode = String.valueOf(code);
        try {
        	if("new".equals(type)){
        		jedisUtil.setEx(RedisKey.VOLUMETYPE_PHONE_NEW+newPhone, phoneCode, 300);
        	}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			throw new AppException("redis连接失败！");
		}
        String operationName = "绑定";
        if("old".equals(type)){
        	operationName = "解除";
        }
        String content = "您正在运维中心，"+operationName+"云硬盘分类限速权限，验证码为："
        		+phoneCode+"；此验证码5分钟内有效。";
        List<String> mobiles = new ArrayList<>();
        if("old".equals(type)){
        	mobiles.add(phone.getPhone());
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
	public boolean editVolumeTypePhone(String code, String newPhone) {
		log.info("修改最高权限手机号码实现...");
		if (StringUtil.isEmpty(code)) {
            throw new AppException("请输入验证码!");
        }
		try {
			String phoneCode = jedisUtil.get(RedisKey.VOLUMETYPE_PHONE_NEW+newPhone);
			if (StringUtil.isEmpty(phoneCode) || (!code.equals(phoneCode))) {
	            throw new AppException("验证码不正确，请核对后重新输入");
	        }else{
	        	BaseApiSwitchPhone phone = this.getVolumeTypePhone();;
	        	jedisUtil.delete(RedisKey.VOLUMETYPE_PHONE_NEW+newPhone);
	        	if(phone==null){
	        		BaseApiSwitchPhone baseApiSwitchPhone = new BaseApiSwitchPhone();
		        	baseApiSwitchPhone.setPhone(newPhone);
		        	baseApiSwitchPhone.setType(PhoneVerify.TypeVolType);
		        	apiSwitchDao.saveEntity(baseApiSwitchPhone);
	        	}else{
	        		StringBuffer sb = new StringBuffer();
	    	        sb.append("update BaseApiSwitchPhone set phone=? where type='2' ");
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




	@Override
	public boolean editVolumeTypePerson(String newName) {
		log.info("修改联系人实现...");
		try {
        	BaseApiSwitchPhone phone = this.getVolumeTypePhone();;
        	if(phone==null){
        		BaseApiSwitchPhone baseApiSwitchPhone = new BaseApiSwitchPhone();
	        	baseApiSwitchPhone.setName(newName);;
	        	baseApiSwitchPhone.setType(PhoneVerify.TypeVolType);
	        	apiSwitchDao.saveEntity(baseApiSwitchPhone);
        	}else{
        		StringBuffer sb = new StringBuffer();
    	        sb.append("update BaseApiSwitchPhone set name=? where type='2' ");
    	        int count = apiSwitchDao.executeUpdate(sb.toString(), newName);
        	}
        	return true;
	    
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			throw new AppException("redis连接失败！");
		}
		
	}

	
}

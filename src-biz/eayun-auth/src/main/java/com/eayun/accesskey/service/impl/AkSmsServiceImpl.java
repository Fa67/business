package com.eayun.accesskey.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.accesskey.service.AkSmsService;
import com.eayun.common.exception.AppException;
import com.eayun.customer.bean.PhoneVerify;
import com.eayun.customer.serivce.PhoneVerifyService;
import com.eayun.sms.service.SMSService;
@Service
@Transactional
public class AkSmsServiceImpl implements AkSmsService {
    private static final Logger log = LoggerFactory.getLogger(AkSmsServiceImpl.class);
	@Autowired
    private SMSService smsService;
    
    @Autowired
    private PhoneVerifyService phoneVerifyService;
	@Override
	public void sendValidSms(String phone, String userId) {
		log.info("发送短信");
        /*BaseUser baseuser = userDao.findOne(userId);
        baseuser.setUserPhone(phone);
        baseuser.setIsPhoneValid(false);
        userDao.merge(baseuser);*/
        
        PhoneVerify phoneVerify = new PhoneVerify();
        phoneVerify.setUserId(userId);
        phoneVerify.setPhone(phone);
        int code = (int)((Math.random()*9+1)*100000);
        String phoneCode = String.valueOf(code);
        phoneVerify.setPhoneCode(phoneCode);
        phoneVerify.setNewphone(false);
        phoneVerifyService.addPhoneVerify(phoneVerify);
        
        String content = "尊敬的客户：欢迎使用公有云管理控制台，您的验证码为："+phoneCode+",验证码有效时间5分钟。如有问题请致电400-606-6396。";
        List<String> mobiles = new ArrayList<>();
        mobiles.add(phone);
        try {
            smsService.send(content, mobiles);
        } catch (Exception e) {
            log.error("信息发送失败", e);
            throw new AppException("信息发送失败");
        }

	}

}

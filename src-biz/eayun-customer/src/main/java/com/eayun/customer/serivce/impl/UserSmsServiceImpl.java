package com.eayun.customer.serivce.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.exception.AppException;
import com.eayun.customer.bean.PhoneVerify;
import com.eayun.customer.dao.UserDao;
import com.eayun.customer.model.BaseUser;
import com.eayun.customer.serivce.PhoneVerifyService;
import com.eayun.customer.serivce.UserSmsService;
import com.eayun.sms.service.SMSService;

@Service
@Transactional
public class UserSmsServiceImpl implements UserSmsService{
    private static final Logger log = LoggerFactory.getLogger(UserSmsServiceImpl.class);
    
    @Autowired
    private SMSService smsService;
    
    @Autowired
    private PhoneVerifyService phoneVerifyService;
    
    @Autowired
    private UserDao userDao;
    
    /**
     * 1.发送一条短信验证码
     * 2.增加一条短信记录（包含用户id，手机号码，验证码，失效时间等）
     * @param phone
     * @param userId
     */
    @Override
    public void sendValidSms(String phone, String userId , String type) {
        log.info("发送短信");
        BaseUser baseUser = userDao.findOne(userId);
        if(type.equals("old")){
        	phone = baseUser.getUserPhone();
        }else if(phone.contains("****") && !baseUser.getIsPhoneValid()){
        	phone = baseUser.getUserPhone();
        }
        PhoneVerify phoneVerify = new PhoneVerify();
        phoneVerify.setUserId(userId);
        phoneVerify.setPhone(phone);
        int code = (int)((Math.random()*9+1)*100000);
        String phoneCode = String.valueOf(code);
        phoneVerify.setPhoneCode(phoneCode);
        if(type.equals("old")){
            phoneVerify.setNewphone(false);
        }else{
            phoneVerify.setNewphone(true);
        }
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
    
    @Override
    public void sendSmsForMail(String userId) {
        log.info("发送短信");
        BaseUser baseuser = userDao.findOne(userId);
        if(baseuser!=null){
            if(baseuser.getIsPhoneValid()&&baseuser.getUserPhone()!=null){
                String content = "尊敬的客户：您正在修改公有云管理控制台的联系邮箱。如有问题请致电400-606-6396。";
                List<String> mobiles = new ArrayList<>();
                mobiles.add(baseuser.getUserPhone());
                try {
                    smsService.send(content, mobiles);
                } catch (Exception e) {
                    log.error("信息发送失败", e);
                    throw new AppException("信息发送失败");
                }
            }
        }

    }

}

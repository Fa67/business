package com.eayun.work.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.GenericsUtil;
import com.eayun.common.util.MD5;
import com.eayun.customer.model.User;
import com.eayun.customer.serivce.UserService;
import com.eayun.sms.service.SMSService;
import com.eayun.work.service.ForgotCodeService;

@Service
@Transactional
public class ForgotCodeServiceImpl implements ForgotCodeService{
    private static final Logger log = LoggerFactory.getLogger(ForgotCodeServiceImpl.class);
	@Autowired
	private UserService userService;
	@Autowired
	private SMSService smsService;
	@Autowired
	private JedisUtil jedisUtil;
	
	@Override
	public void firstCheck(String userAccount,String idCode,String rightIdCode,JSONObject object) throws Exception {
		JSONObject json =JSONObject.parseObject(rightIdCode);
		if(null==json){
			throw new AppException("图片验证码不正确，请重新输入");
		}
		String code = json.getString("code");
        String startTimeStr = json.getString("startTime");
        long startTime = Long.parseLong(startTimeStr);
        long timeDiff = (System.currentTimeMillis()-startTime-180000);
        if (timeDiff > 0){
        	throw new AppException("图片验证码已过期，请重新输入");
        } else {
        	if (!idCode.equals(code)) {
        		throw new AppException("图片验证码不正确，请重新输入");
        	} else {
        		object.put("codeFlag", true);
	        	User user = userService.findUserByUserName(userAccount);
	        	if (user == null) {
	        		object.put("accountFlag", false);
	        	} else {
	        		jedisUtil.set(RedisKey.FORGOTCODE_CODECHECK + userAccount, "code_pass");
	        		jedisUtil.expireKey(RedisKey.FORGOTCODE_CODECHECK + userAccount, 600);
	        		object.put("accountFlag", true);
	        		object.put("userPhone", user.getUserPhone());
	        	}
        	}
        }
	}
	
	@Override
	public void secondCheck(String userAccount, String phoneCode, String teleJsonStr, JSONObject object) throws Exception{
		JSONObject teleJson = JSONObject.parseObject(teleJsonStr);
		if(null==teleJson){
			throw new AppException("手机验证码不正确，请重新输入");
		}
		String teleCode = teleJson.getString("code");
		String teleStartTimeStr = teleJson.getString("startTime");
		long teleStartTime = Long.parseLong(teleStartTimeStr);
		long teleTimeDiff = (System.currentTimeMillis()-teleStartTime-300000);
		if (teleTimeDiff > 0){
			throw new AppException("手机验证码已过期，请重新获取");
		} else {
			if (!phoneCode.equals(teleCode)){
				throw new AppException("手机验证码不正确，请重新输入");
			} else {
				jedisUtil.set(RedisKey.FORGOTCODE_PHONECHECK + userAccount, "phone_pass");
				jedisUtil.expireKey(RedisKey.FORGOTCODE_PHONECHECK + userAccount, 600);
				object.put("phoneFlag", true);
			}
		}
	}
	
	@Override
	public void modifyPassword(String userAccount,String userPassword, JSONObject object) throws Exception {
		User user = userService.findUserByUserName(userAccount);
		if (("code_pass").equals(jedisUtil.get(RedisKey.FORGOTCODE_CODECHECK + userAccount)) && ("phone_pass").equals(jedisUtil.get(RedisKey.FORGOTCODE_PHONECHECK + userAccount))) {
			int newSalt = (int)((Math.random() * 9 + 1) * 100000);
			String userSalt = String.valueOf(newSalt);
			MD5 md5 = new MD5();
			String saltPassword = md5.getMD5ofStr(md5.getMD5ofStr(userPassword) + userSalt);
			user.setUserPassword(saltPassword);
			user.setSalt(userSalt);
			userService.updateUserBySql(user);
			object.put("done", true);
		} else {
			object.put("done", false);
			object.put("msg", "您的验证信息不完整，请按照规定步骤重新申请！");
		}
		object.put("cusId", user.getCusId());
		jedisUtil.delete(RedisKey.FORGOTCODE_CODECHECK + userAccount);
		jedisUtil.delete(RedisKey.FORGOTCODE_PHONECHECK + userAccount);
	}
	/**
	 * 发送注册手机验证码
	 * @param telephone
	 * @throws AppException
	 */
	@Override
	public JSONObject getTeleCode(String userAccount) throws AppException{
		String verifyCode = GenericsUtil.getRandNumber(6);
		JSONObject teleJson = new JSONObject ();
		teleJson.put("code", verifyCode);
		log.info("-------------"+verifyCode+"-----------");
		teleJson.put("startTime", System.currentTimeMillis());
		
		String content = "尊敬的客户：欢迎使用公有云管理控制台，您的验证码为："+verifyCode+",验证码有效时间5分钟。如有问题请致电400-606-6396。";
        List<String> phones = new ArrayList<>();
        User user = userService.findUserByUserName(userAccount);
        phones.add(user.getUserPhone());
        try {
            smsService.send(content, phones);
        } catch (Exception e) {
            throw new AppException("信息发送失败");
        }
        teleJson.put("telephone", user.getUserPhone());
		return teleJson;
	}
}

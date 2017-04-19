package com.eayun.work.service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;

public interface ForgotCodeService {
	
	public void firstCheck(String userAccount, String idCode, String rightIdCode, JSONObject object) throws Exception;
	
	public void secondCheck(String userAccount, String phoneCode, String teleJsonStr, JSONObject object) throws Exception;
	
	public void modifyPassword(String userAccount, String userPassword, JSONObject object) throws Exception;
	/**
	 * 发送注册手机验证码
	 * @throws AppException
	 */
	public JSONObject getTeleCode(String userAccount) throws Exception;
}

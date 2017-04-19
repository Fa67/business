package com.eayun.work.service;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.customer.model.Customer;

public interface RegisterService {
	
	/**
	 * 注册用户
	 * @param request
	 * @throws AppException
	 */
	public String register(HttpServletRequest request,Customer custiomer) throws Exception;
	
	/**
	 * 根据查询条件客户
	 * @throws AppException
	 */
	public boolean checkCustomerByCondition(Customer custiomer) throws Exception;
	
	/**
	 * 发送注册手机验证码
	 * @param telephone
	 * @throws AppException
	 */
	public JSONObject getTeleCode(String telephone) throws Exception;
	
	
}

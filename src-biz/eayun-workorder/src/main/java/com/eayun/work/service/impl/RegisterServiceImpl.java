package com.eayun.work.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.exception.AppException;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.GenericsUtil;
import com.eayun.customer.model.BaseCustomer;
import com.eayun.customer.model.Customer;
import com.eayun.customer.serivce.CustomerService;
import com.eayun.sms.service.SMSService;
import com.eayun.work.service.RegisterService;
import com.eayun.work.service.WorkorderService;

@Service
@Transactional
public class RegisterServiceImpl implements RegisterService {
    private static final Logger log = LoggerFactory.getLogger(RegisterServiceImpl.class);
    
    @Autowired
    private SMSService smsService;
    @Autowired
    private WorkorderService workorderService ;
    @Autowired
    private CustomerService customerService ;
    
	/**
	 * 注册用户
	 * ------------------
	 * @author zhouhaitao
	 * @param request
	 * @param customer
	 * @throws AppException
	 */
	public String register(HttpServletRequest request,Customer customer) throws Exception{
		String respMsg =checkVerifyCode(request, customer);
		if(!StringUtils.isEmpty(respMsg)){
			return respMsg;
		}
		customer.setCusFalg('0');
		BaseCustomer baseCustomer = new BaseCustomer();
		BeanUtils.copyPropertiesByModel(baseCustomer, customer);
		baseCustomer.setCreatTime(new Date());
		
		baseCustomer = customerService.save(baseCustomer);
		BeanUtils.copyPropertiesByModel(customer, baseCustomer);
		
		workorderService.addRegisterWorkorder(customer);
		respMsg = ConstantClazz.SUCCESS_CODE_OP;
		return respMsg;
	}
	
	/**
	 * 根据查询条件客户
	 * @param customer
	 * @throws AppException
	 */
	@SuppressWarnings("rawtypes")
	public boolean checkCustomerByCondition(Customer customer) throws AppException{
		StringBuffer hql = new StringBuffer ();
		Map<String,Object> map = new HashMap<String,Object>();
		hql.append(" from BaseCustomer ");
		hql.append(" where 1=1 ");
		if(!StringUtils.isEmpty(customer.getCusEmail())){
			hql.append(" and cusEmail = :email ");
			map.put("email", customer.getCusEmail());
		}
		if(!StringUtils.isEmpty(customer.getCusPhone())){
			hql.append(" and cusPhone = :phone ");
			map.put("phone", customer.getCusPhone());
		}
		if(!StringUtils.isEmpty(customer.getCusCpname())){
			hql.append(" and cusCpname = :cusCpname ");
			map.put("cusCpname", customer.getCusCpname());
		}
		
		hql.append(" and  cusFalg in ('0','1')");
		
		List list = customerService.findBy(hql.toString(), map);
		return null==list||list.size()==0;
		
	}

	
	public String checkVerifyCode(HttpServletRequest request,Customer customer){
		String respMsg ="";
		String teleJsonStr = request.getSession().getAttribute(customer.getCusPhone()+"_register")+"";
		String imageJsonStr = request.getSession().getAttribute("register")+"";
		
		if(StringUtils.isEmpty(imageJsonStr)||"null".equals(imageJsonStr)){
			respMsg +="imageCodeNull,";
		}
		else{
			JSONObject imageJson = JSONObject.parseObject(imageJsonStr);
			if(null==imageJson){
				respMsg +="imageCodeError,";
			}
			else{
				String imageCode = imageJson.getString("code");
				String imageStartTimeStr = imageJson.getString("startTime");
				long imageStartTime = Long.parseLong(imageStartTimeStr);
				long imageTimeDiff = (System.currentTimeMillis()-imageStartTime-180000);
				if(imageTimeDiff>0){
					respMsg +="imageCodeTimeout,";
				}
				else {
					if(!customer.getImageCode().equals(imageCode)){
						respMsg +="imageCodeError,";
					}
					else {
						if(StringUtils.isEmpty(teleJsonStr)||"null".equals(teleJsonStr)){
							respMsg +="phoneCodeError,";
						}
						else {
							JSONObject teleJson = JSONObject.parseObject(teleJsonStr);
							if(null==teleJson){
								respMsg +="phoneCodeError,";
							}
							else {
								String teleCode = teleJson.getString("code");
								String teleStartTimeStr = teleJson.getString("startTime");
								long teleStartTime = Long.parseLong(teleStartTimeStr);
								long teleTimeDiff = (System.currentTimeMillis()-teleStartTime-300000);
								if(teleTimeDiff>0){
									respMsg +="phoneCodeTimeout,";
								}
								else {
									if(!customer.getTeleCode().equals(teleCode)){
										respMsg +="phoneCodeError,";
									}
								}
							}
						}
					}
				}
				}
			}
		
		if(!StringUtils.isEmpty(respMsg)){
			respMsg = respMsg.substring(0,respMsg.length()-1);
		}
		return respMsg;
	}
	
	/**
	 * 发送注册手机验证码
	 * @param telephone
	 * @throws AppException
	 */
	public JSONObject getTeleCode(String telephone) throws AppException{
		String verifyCode = GenericsUtil.getRandNumber(6);
		JSONObject teleJson = new JSONObject ();
		teleJson.put("code", verifyCode);
		log.info("-------------"+verifyCode+"-----------");
		teleJson.put("startTime", System.currentTimeMillis());
		
		String content = "尊敬的客户：欢迎使用公有云管理控制台，您的验证码为："+verifyCode+",验证码有效时间5分钟。如有问题请致电400-606-6396。";
        List<String> phones = new ArrayList<>();
        phones.add(telephone);
        try {
            smsService.send(content, phones);
        } catch (Exception e) {
            throw new AppException("信息发送失败");
        }
		
		return teleJson;
	}
	
}

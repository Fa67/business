package com.eayun.obs.service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.customer.model.CusServiceState;
import com.eayun.obs.model.ObsUser;
import com.eayun.order.model.Order;

/**
 * 对象存储用户Service接口类
 * @author xiangyu.cao@eayun.com
 *
 * @date 2016年1月11日 下午5:40:09
 */
public interface ObsOpenService {
	/**
	 * 根据客户id查询开通状态
	 * @param cusId
	 * @return
	 */
	public CusServiceState getObsByCusId(String cusId);
	/**
	 * 对象存储服务开通
	 * @param cusId
	 * @param cusName
	 * @return
	 * @throws Exception 
	 */
	public  ObsUser addObsUser(String cusId,String cusName) throws Exception;
	/**
	 * 是否开通obs服务
	 * @param userName
	 * @return
	 */
	public boolean isOpenObsServiceAndWhiteList(String userName);
	
	/**
	 * 余额是否达到开通值
	 * @param limit
	 * @param cusId
	 * @return
	 * @throws Exception
	 */
	public boolean isPassForLimitValue(String limit,String cusId) throws Exception;
	/**
	 * 获取开通值
	 * @return
	 * @throws Exception
	 */
	public String getLimitValue() throws Exception;
	
	/**
	 * 创建obs订单
	 * @param cusId
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public Order createObsOrder(String cusId, String userId) throws Exception;
	
	/**
	 * 完成obs订单
	 * @param orderNo
	 * @param b
	 * @param obsUser
	 * @throws Exception
	 */
	public void completeObsOrder(String orderNo, boolean b,String cusId) throws Exception;
	/**
	 * 开通对象存储服务
	 * @param userName
	 * @param userId
	 * @param cusId
	 * @return
	 * @throws Exception
	 */
	public JSONObject openObs(String userName,String userId ,String cusId,boolean isAdmin) throws Exception;
	/**
	 * 是否满足开通对象存储服务条件
	 * @param userName
	 * @param cusId
	 * @return
	 * @throws Exception
	 */
	public JSONObject isAllowOpen(String userName,String cusId) throws Exception;
	
}

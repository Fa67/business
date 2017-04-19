package com.eayun.ecmcapi.service;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.ecmcapi.model.ApiBlackList;
import com.eayun.ecmcapi.model.BaseApiBlackList;


public interface EcmcBlackService {
	
	
	/**
	 * 查询黑名单IP展示一页内容
	 * @return page
	 */
	public Page getBlackIp (Page page, QueryMap queryMap);
	
	
	/**
	 * 查询黑名单客户展示一页内容
	 * @return page
	 */
	public Page getBlackCustomer(Page page, QueryMap queryMap);
	
	 /**
     * 添加黑名单(客户或IP)
     * @param ApiBlackList
     * @return boolean
	 * @throws Exception 
     */
	public BaseApiBlackList addBlack (ApiBlackList blackList) throws Exception;
	
	/**
     * 删除黑名单(客户或IP)
     * @param ApiBlackList
     * @return boolean
	 * @throws Exception 
     */
	public boolean deleteBlack (String apiId) throws Exception;
	
	/**
	 * 同步ECMC黑名单客户与IP到缓存中
	 * @return boolean
	 * @throws Exception 
	 */
	public boolean synchronizeBlack() throws Exception; 
	
	public ApiBlackList getApiBlack(String apiId) throws Exception;
	
	/**
     * 校验重复IP
     * @param ApiBlackList
	 * @return boolean
	 * @throws Exception
     * */
	public boolean checkBlackIpExist(ApiBlackList blackList) throws Exception;
	
}

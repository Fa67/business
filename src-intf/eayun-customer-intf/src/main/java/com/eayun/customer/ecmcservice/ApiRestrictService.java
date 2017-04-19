package com.eayun.customer.ecmcservice;

import java.util.List;
import java.util.Map;

import com.eayun.customer.model.ApiCountRestrict;

public interface ApiRestrictService {
	/**
	 * 获取所有的自定义访问限制
	 * @throws Exception
	 */
	public List<ApiCountRestrict> getAllApiRestrict() throws Exception ;

	/**
	 * 获取所有api类别
	 * @return
	 * @throws Exception
	 */
	public List<ApiCountRestrict> getApiType() throws Exception;
	
	/**
	 * 根据客户id版本号,api类别获取访问限制次数 
	 * 用于ecmc
	 * @param version
	 * @param apiType
	 * @return
	 * @throws Exception
	 */
	public List<ApiCountRestrict> getRestrictRequestCount(String cusId ,String version ,String apiType) throws Exception;

	/**
	 * 修改访问限制次数
	 * @throws Exception
	 */
	public void updateRestrictRequestCount(List<Map<String, Object>> actions) throws Exception;


}

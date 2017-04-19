package com.eayun.ecmcapi.service;


import java.util.List;
import java.util.Map;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.ecmcapi.model.ApiDefaultCount;

public interface EcmcApiRestrictService {
	/**
	 * 同步api访问限制
	 * @throws Exception
	 */
	public void syncApiCount() throws Exception ;
	
	/**
	 *获取访问限制次数
	 * @return
	 * @throws Exception
	 */
	public Page getApiDefaultCount(String version,String apiType,QueryMap queryMap, Page page) throws Exception;
	
	/**
	 *获取访问限制次数List
	 * @return
	 * @throws Exception
	 */
	public List<ApiDefaultCount> getApiDefaultCountList(String version,String apiType) throws Exception;
	
	/**
	 * 修改限制修改默认值每小时访问特定类别api次数
	 * @param actions
	 * @throws Exception
	 */
	public void updateApiDefaultCount(List<Map> actions,String version ,String apiType) throws Exception;
	
}

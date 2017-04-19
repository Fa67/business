package com.eayun.unit.service;

import java.util.List;
import java.util.Map;

import com.eayun.unit.model.BaseWebSiteInfo;
import com.eayun.unit.model.WebSiteIP;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2016年12月22日
 */
public interface EcscWebsiteService {

	/**
	 * 根据主体信息查询网站信息
	 * @param unitId
	 * @return
	 */
	public List<WebSiteIP> getUnitWebsite(String unitId);
	/**
	 * 添加网站
	 * @param web
	 * @return
	 */
	public BaseWebSiteInfo addWebsite(Map<String, Object> parms) throws Exception;
	/**
	 * 根据网站ID删除网站信息
	 * @param webId
	 * @return
	 */
	public int deleteWebsite(String webId) throws Exception;
	
	/**
	 * 复制备案
	 * @param webId
	 * @return
	 * @throws Exception
	 */
	public BaseWebSiteInfo copyWebsite(String webId)throws Exception;
	/**
	 *  变更备案
	 * @return
	 */
	public List<BaseWebSiteInfo> changeWebsite(Map<String, Object> parms)throws Exception;
	/**
	 * 修改网站
	 * @param web
	 * @return
	 * @throws Exception
	 */
	public BaseWebSiteInfo updateWebsite(BaseWebSiteInfo web) throws Exception;
	
	/**
	 * <p>此方法已过期</p>
	 * <p>备案V1.1表结构改变，IP以存放至BaseWebDataCenterIp</p>
	 * <p>查询此公网IP是否已绑定备案服务请调用com.eayun.unit.service.EcscRecordService.getWebDataCenterIp</p>
	 * 检查此公网IP是否已绑定备案服务
	 * 已绑定：true
	 * 未绑定：false
	 * @Author: duanbinbin
	 * @param floIp
	 * @return
	 * @throws Exception
	 *<li>Date: 2017年1月18日</li>
	 */
	@Deprecated
	public boolean checkFloatIpWebSite(String floIp) throws Exception;
	
}

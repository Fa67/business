package com.eayun.obs.ecmcservice;

import java.util.Date;
import java.util.List;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.obs.ecmcmodel.EcmcObsEchartsBean;
import com.eayun.obs.model.CdnBucket;
import com.eayun.obs.model.ObsUsedType;

public interface EcmcObsCdnService {

	/**
	 * 获取所有开通过CDN服务的客户列表
	 * @Author: duanbinbin
	 * @return
	 *<li>Date: 2016年6月30日</li>
	 */
	public List<CdnBucket> getObsCdnCusList();
	
	/**
	 * 获取客户下本月使用CDN加速流量中加速域名列表
	 * @param queryMap 
	 * @param page 
	 * @Author: duanbinbin
	 * @param cusId
	 * @return
	 *<li>Date: 2016年6月30日</li>
	 */
	public Page getMonthDomainList(Page page, QueryMap queryMap, String cusId);
	
	/**
	 * 获取客户下所有加速域名列表
	 * @Author: duanbinbin
	 * @param cusId
	 * @return
	 *<li>Date: 2016年6月30日</li>
	 */
	public List<CdnBucket> getAllDomainList(String cusId);
	
	/**
	 * 加速域名下载流量图表
	 * @Author: duanbinbin
	 * @param domain
	 * @param startTime
	 * @param endTime
	 * @return
	 *<li>Date: 2016年6月30日</li>
	 */
	public EcmcObsEchartsBean getDomainData(String cusId,String domain , Date startTime , Date endTime , String type,String queryType) throws Exception;
	
	/**
	 * 客户本月使用的CDN下载流量
	 * @Author: duanbinbin
	 * @param cusId
	 * @return
	 *<li>Date: 2016年6月30日</li>
	 */
	public ObsUsedType getMonthDomainData(String cusId) throws Exception;
	
	/**
	 * 获取资源详情
	 * @param page
	 * @param queryMap
	 * @param useStart
	 * @param useEnd
	 * @param cusId
	 * @return
	 */
	public Page getCDNResources(Page page, QueryMap queryMap, Date useStart,
			Date useEnd, String cusId) throws Exception;
	
	/**
	 * 获取历史账单,分页
	 * 
	 * @param cusId
	 * @return
	 */
	public Page getCdnHistoryResources(Page page, QueryMap queryMap,String cusId) throws Exception;
}

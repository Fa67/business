package com.eayun.monitor.ecmcservice;

import java.util.Date;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.monitor.bean.EcmcVmIndicator;

public interface EcmcInstanceMonitorService {

	/**
	 * 运维查询数据库实例资源监控实时数据
	 * @Author: duanbinbin
	 * @param page
	 * @param queryMap
	 * @param queryType
	 * @param queryName
	 * @param dcName
	 * @param orderBy
	 * @param sort
	 * @param versionId
	 * @return
	 *<li>Date: 2017年3月2日</li>
	 */
	public Page getInstanceListforEcmcLive(Page page, QueryMap queryMap,
			String queryType, String queryName, String dcName, String orderBy,
			String sort, String versionId);

	/**
	 * 运维查询数据库实例资源监控历史数据
	 * @Author: duanbinbin
	 * @param page
	 * @param queryMap
	 * @param queryType
	 * @param queryName
	 * @param endDate
	 * @param mins
	 * @param orderBy
	 * @param sort
	 * @param dcName
	 * @param versionId
	 * @return
	 *<li>Date: 2017年3月2日</li>
	 */
	public Page getInstanceListforEcmcLast(Page page, QueryMap queryMap,
			String queryType, String queryName, Date endDate, int mins,
			String orderBy, String sort, String dcName, String versionId);

	public EcmcVmIndicator getInstancedetailById(String instanceId);

}

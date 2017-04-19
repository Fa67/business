package com.eayun.monitor.ecmcservice;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.monitor.bean.LdPoolIndicator;
import com.eayun.monitor.model.CloudLdpoolExp;

public interface EcmcLdPoolMonitorService {

	/**
	 * 运维查询负载均衡资源监控列表
	 * @Author: duanbinbin
	 * @param page
	 * @param queryMap
	 * @param queryType
	 * @param queryName
	 * @param orderBy
	 * @param sort
	 * @param dcName
	 * @param mode
	 * @return
	 *<li>Date: 2017年3月2日</li>
	 */
	public Page getEcmcLdPoolMonitorList(Page page, QueryMap queryMap,
			String queryType, String queryName, String orderBy, String sort,
			String dcName, String mode);

	/**
	 * 运维查询负载均衡下成员异常记录列表
	 * @Author: duanbinbin
	 * @param page
	 * @param queryMap
	 * @param cusId
	 * @param endTime
	 * @param cou
	 * @param poolId
	 * @param mode
	 * @param role
	 * @param memberName
	 * @param healthName
	 * @param isRepair
	 * @return
	 *<li>Date: 2017年3月2日</li>
	 */
	public Page getEcmcLdPoolExpList(Page page, QueryMap queryMap,
			Date endTime, int cou, String poolId, String mode, String role,
			String memberName, String healthName, String isRepair);

	public LdPoolIndicator getLdPoolDetailById(String ldPoolId);

	/**
	 * 查询成员名称、健康检查下拉框
	 * @Author: duanbinbin
	 * @param endTime
	 * @param cou
	 * @param poolId
	 * @param mode
	 * @param role
	 * @param memberId
	 * @param healthId
	 * @param isRepair
	 * @return
	 *<li>Date: 2017年3月13日</li>
	 */
	public Map<String, List<CloudLdpoolExp>> getMemAndHeaNameById(Date endTime, int cou,
			String poolId, String mode, String role, String memberName,
			String healthName, String isRepair);

}

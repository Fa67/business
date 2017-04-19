package com.eayun.monitor.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.monitor.bean.LdPoolIndicator;
import com.eayun.monitor.model.BaseCloudLdpoolExp;
import com.eayun.monitor.model.CloudLdpoolExp;

public interface LdPoolAlarmMonitorService {

	/**
	 * 查询负载均衡资源监控列表
	 * @Author: duanbinbin
	 * @param page
	 * @param queryMap
	 * @param projectId
	 * @param poolName
	 * @param mode
	 * @return
	 *<li>Date: 2017年3月2日</li>
	 */
	public Page getLdPoolMonitorList(Page page, QueryMap queryMap, String projectId,
			String poolName, String mode);

	/**
	 * 查询负载均衡的成员异常记录列表
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
	public Page getLdPoolExpList(Page page, QueryMap queryMap, String cusId,
			Date endTime, int cou, String poolId, String mode, String role,
			String memberName, String healthName, String isRepair);
	/**
	 * 检测到成员异常时，添加异常信息记录
	 * 同时如果之前有相同健康检查、成员的异常记录，则把之前的是否需要修复状态置为否
	 * 
	 * @Author: duanbinbin
	 * @param cloudLdpoolExp
	 * @param isExp			是否为异常信息
	 *<li>Date: 2017年3月10日</li>
	 */
	public void addCloudLdpoolExp(BaseCloudLdpoolExp cloudLdpoolExp,Boolean isExp);

	public LdPoolIndicator getLdPoolDetailById(String ldPoolId);

	public Map<String, List<CloudLdpoolExp>> getNameListById(Date endTime,
			int cou, String poolId, String mode, String role, String memberName,
			String healthName, String isRepair);
	
}

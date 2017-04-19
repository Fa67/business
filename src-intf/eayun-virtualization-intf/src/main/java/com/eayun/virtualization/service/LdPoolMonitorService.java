package com.eayun.virtualization.service;

import java.util.List;

import com.eayun.virtualization.model.BaseCloudLdPoolMonitor;

public interface LdPoolMonitorService {
	public BaseCloudLdPoolMonitor saveEntiry(BaseCloudLdPoolMonitor entity);
	public int deleteByPoolIdAndMonitorId(String sql, String poolId,String monitorId);
	
	/**
	 * 查询负载均衡器下的健康检查
	 * -------------------
	 * @author zhouhaitao
	 * @param poolId
	 * @return
	 */
	public List<BaseCloudLdPoolMonitor> getMonitorByPool(String poolId);
	/**
	 * 查询该负载均衡是否有正在生效是健康检查
	 * @param poolId
	 * @return
	 * @throws Exception
	 */
	public boolean hasTakeEffectHealthMonitor(String poolId) throws Exception;
}

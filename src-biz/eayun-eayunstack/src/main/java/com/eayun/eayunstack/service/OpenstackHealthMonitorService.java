package com.eayun.eayunstack.service;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.HealthMonitor;

public interface OpenstackHealthMonitorService extends
		OpenstackBaseService<HealthMonitor> {
	/**
	 * 资源池解除一条监控信息
	 * @param datacenterId
	 * @param projectId
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public boolean detachHealthMonitor(String datacenterId, String projectId,String poolId,String monitorId) throws AppException;
	
	/**
	 * 查询底层 数据中心的云资源
	 * -------------------
	 * @author zhouhaitao
	 * @param dataCenter
	 * @return
	 */
	public Map<String,Object> getStackList (BaseDcDataCenter dataCenter);
	
	public JSONObject get (String dcId,String fwId) throws Exception;
}

package com.eayun.schedule.service;

import com.eayun.datacenter.model.BaseDcDataCenter;

public interface CloudLdMonitorService {
	
	/**
	 * 同步底层数据中心下的负载均衡监控
	 * -----------------
	 * @param dataCenter
	 * @param prjId
	 */
	public void synchData(BaseDcDataCenter dataCenter) throws Exception;
}	

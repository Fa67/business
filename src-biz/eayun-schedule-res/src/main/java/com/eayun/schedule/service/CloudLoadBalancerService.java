package com.eayun.schedule.service;

import com.eayun.datacenter.model.BaseDcDataCenter;

public interface CloudLoadBalancerService {
	
	/**
	 * 同步底层负载均衡器的浮动IP
	 * -----------------
	 * @author zhouhaitao
	 * @param dataCenter
	 */
	public void synchData(BaseDcDataCenter dataCenter) throws Exception;
}

package com.eayun.schedule.service;

import com.eayun.datacenter.model.BaseDcDataCenter;

public interface CloudFirewallRuleService {

	/**
	 * 同步底层数据中心下的防火墙规则
	 * -----------------
	 * @param dataCenter
	 * @param prjId
	 */
	public void synchData(BaseDcDataCenter dataCenter) throws Exception;
}

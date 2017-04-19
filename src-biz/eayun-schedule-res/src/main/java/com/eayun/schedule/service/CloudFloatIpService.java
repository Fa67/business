package com.eayun.schedule.service;

import com.eayun.datacenter.model.BaseDcDataCenter;

public interface CloudFloatIpService {

	/**
	 * 同步底层数据中心下的浮动IP
	 * -----------------
	 * @param dataCenter
	 * @param prjId
	 */
	public void synchData(BaseDcDataCenter dataCenter,String prjId) throws Exception;
}

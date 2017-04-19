package com.eayun.schedule.service;

import java.util.List;

import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.virtualization.model.BaseCloudProject;

public interface CloudProjectService {
	
	/**
	 * 同步底层数据中心下的项目（除去admin/services）
	 * -----------------
	 * @param dataCenter
	 * @param prjId
	 */
	public void synchData(BaseDcDataCenter dataCenter) throws Exception;
	
	public List<BaseCloudProject> getAllProjectsByDcId (String dcId) throws Exception;
}

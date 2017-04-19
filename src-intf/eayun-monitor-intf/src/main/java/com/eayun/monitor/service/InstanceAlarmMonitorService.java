package com.eayun.monitor.service;

import java.util.List;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.database.configgroup.model.datastore.DatastoreVersion;
import com.eayun.monitor.bean.VmIndicator;

public interface InstanceAlarmMonitorService {

	/**
	 * 获取云数据库实例监控列表
	 * @Author: duanbinbin
	 * @param page
	 * @param queryMap
	 * @param prjId
	 * @param instanceName
	 * @param versionId
	 * @return
	 *<li>Date: 2017年3月2日</li>
	 */
	public Page getInstanceMonitorPage(Page page, QueryMap queryMap, String prjId,
			String instanceName, String versionId);

	/**
	 * 获取云数据库版本列表
	 * @Author: duanbinbin
	 * @return
	 *<li>Date: 2017年3月2日</li>
	 */
	public List<DatastoreVersion> getDataVersionList();

	public VmIndicator getRdsDetailById(String instanceId);

}

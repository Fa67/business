package com.eayun.database.monitor.service;

import java.util.Date;

import org.springframework.data.mongodb.core.MongoTemplate;

import com.alibaba.fastjson.JSONObject;
import com.eayun.database.instance.model.CloudRDSInstance;

public interface InstanceMonitorService {

	/**
	 * 查询数据库实例详情信息，用于获取磁盘使用率数据
	 * @Author: duanbinbin
	 * @param dcId			数据中心id（上层）
	 * @param prjId			项目id（上层）
	 * @param instanceId	实例id
	 * @return
	 *<li>Date: 2017年3月7日</li>
	 */
	public JSONObject getInstanceForVolUsed(String dcId , String prjId , String instanceId);

	/**
	 * 汇总数据库实例的各项指标信息
	 * @Author: duanbinbin
	 * @param mongoTemplate
	 * @param meter
	 * @param projectId
	 * @param rds
	 * @param interval
	 *<li>Date: 2017年3月14日</li>
	 */
	public void summaryMonitor(MongoTemplate mongoTemplate, String meter,
			String projectId, CloudRDSInstance rds, Date now,String interval);
}

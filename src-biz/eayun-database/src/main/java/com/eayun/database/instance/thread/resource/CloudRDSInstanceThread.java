package com.eayun.database.instance.thread.resource;

import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eayun.common.exception.AppException;
import com.eayun.database.instance.service.CloudRDSInstanceService;
import com.eayun.datacenter.model.BaseDcDataCenter;

/**
 * 云数据库同步线程
 *
 */
public class CloudRDSInstanceThread implements Callable<String>{
	private final static Log logger = LogFactory
			.getLog(CloudRDSInstanceThread.class);
	private BaseDcDataCenter dataCenter;
	private String projectId;
	private CloudRDSInstanceService service;
	
	public CloudRDSInstanceThread(CloudRDSInstanceService service) {
		this.service = service;
	}

	@Override
	public String call() throws Exception {
		logger.info("执行项目ID:"+projectId+"下云数据库同步---开始");
		try {
			service.synchData(dataCenter, projectId);
			logger.info("执行项目ID:"+projectId+"下云数据库同步---结束");
			return "success";
		} catch (AppException e) {
			logger.error("执行项目ID:"+projectId+"下云数据库同步出错:" + e.getMessage(),e);
			return "failed";
		}
	}

	public BaseDcDataCenter getDataCenter() {
		return dataCenter;
	}

	public void setDataCenter(BaseDcDataCenter dataCenter) {
		this.dataCenter = dataCenter;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}
	
}

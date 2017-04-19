package com.eayun.schedule.thread.resource;

import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.schedule.service.CloudVolumeService;

/**
 * 云硬盘同步线程
 * @author zhouhaitao
 *
 */
public class CloudDiskThread implements Callable<String>{
	private static final Log logger = LogFactory.getLog(CloudDiskThread.class);
	private String projectId;
	private	BaseDcDataCenter dataCenter;
	private CloudVolumeService service;
	
	public CloudDiskThread(CloudVolumeService service) {
		this.service = service;
	}
	
	public String getProjectId() {
		return projectId;
	}
	
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}
	
	public BaseDcDataCenter getDataCenter() {
		return dataCenter;
	}
	
	public void setDataCenter(BaseDcDataCenter dataCenter) {
		this.dataCenter = dataCenter;
	}
	
	@Override
	public String call() throws Exception {
		logger.info("执行项目ID:"+projectId+"下云硬盘同步---开始");
		try {
			service.synchData(dataCenter,projectId);
			logger.info("执行项目ID:"+projectId+"下云硬盘同步---结束");
			return "success";
		} catch (AppException e) {
			logger.error("执行项目ID:"+projectId+"下云硬盘同步出错:"+e.getMessage(),e);
			return "failed";
		}
	}
}

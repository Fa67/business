package com.eayun.schedule.thread.resource;

import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.schedule.service.CloudVolumeSnapService;

/**
 * 云硬盘备份同步线程
 * @author zhouhaitao
 *
 */
public class CloudDiskSnapshotThread implements Callable<String>{
	private static final Log logger = LogFactory.getLog(CloudDiskSnapshotThread.class);
	private String projectId;
	private	BaseDcDataCenter dataCenter;
	private CloudVolumeSnapService service;
	public CloudDiskSnapshotThread(CloudVolumeSnapService service) {
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
		logger.info("执行数据中心ID:"+dataCenter.getId()+"下云硬盘备份同步---开始");
		try {
			service.synchData(dataCenter);
			logger.info("执行数据中心ID:"+dataCenter.getId()+"下云硬盘备份同步---结束");
			return "success";
		} catch (AppException e) {
			logger.error("执行数据中心ID:"+dataCenter.getId()+"下云硬盘备份出错:"+e.getMessage(),e);
			return "failed";
		}
	}
	
}

package com.eayun.schedule.thread.resource;

import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.schedule.service.CloudVolumeService;
import com.eayun.schedule.service.CloudVolumeTypeService;

/**
 * 云硬盘类型同步线程
 * @author chengxiaodong
 *
 */
public class CloudVolumeTypeThread implements Callable<String>{
	private static final Log logger = LogFactory.getLog(CloudVolumeTypeThread.class);
	private String projectId;
	private	BaseDcDataCenter dataCenter;
	private CloudVolumeTypeService service;
	
	public CloudVolumeTypeThread(CloudVolumeTypeService service) {
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
		logger.info("执行数据中心ID:"+dataCenter.getId()+"下云硬盘类型同步---开始");
		try {
			service.synchData(dataCenter);
			logger.info("执行数据中心ID:"+dataCenter.getId()+"下云硬盘类型同步---结束");
			return "success";
		} catch (AppException e) {
			logger.error("执行项目ID:"+dataCenter.getId()+"下云硬盘类型出错:"+e.getMessage(),e);
			return "failed";
		}
	}
}

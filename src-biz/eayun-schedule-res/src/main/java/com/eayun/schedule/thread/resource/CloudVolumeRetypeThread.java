package com.eayun.schedule.thread.resource;

import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.schedule.service.CloudVolumeService;

/**
 * 云硬盘retype同步线程
 * @author chengxiaodong
 *
 */
public class CloudVolumeRetypeThread implements Callable<String>{
	private static final Log logger = LogFactory.getLog(CloudVolumeRetypeThread.class);
	private String projectId;
	private	BaseDcDataCenter dataCenter;
	private CloudVolumeService service;
	
	public CloudVolumeRetypeThread(CloudVolumeService service) {
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
		logger.info("执行项目ID:"+projectId+"下云硬盘retype---开始");
		try {
			service.synchVolumeRetype(dataCenter,projectId);
			logger.info("执行项目ID:"+projectId+"下云硬盘retype---结束");
			return "success";
		} catch (AppException e) {
			logger.error("执行项目ID:"+projectId+"下云硬盘retype出错:"+e.getMessage(),e);
			return "failed";
		}catch (Exception e) {
			logger.error("执行项目ID:"+projectId+"下云硬盘retype出错:"+e.getMessage(),e);
			return "failed";
		}
	}
}

package com.eayun.schedule.thread.resource;

import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.schedule.service.CloudFloatIpService;

/**
 * 浮动IP同步线程
 * @author zhouhaitao
 *
 */
public class CloudFloatIpThread implements Callable<String>{
	private final static Log logger=LogFactory.getLog(CloudFloatIpThread.class);
	private BaseDcDataCenter dataCenter;
	private String  projectId;
	private CloudFloatIpService service;
	
	public CloudFloatIpThread(CloudFloatIpService service) {
		this.service = service;
	}
	@Override
	public String call() throws Exception {
		logger.info("执行项目ID:"+projectId+"下浮动IP同步---开始");
		try {
			service.synchData(dataCenter, projectId);
			logger.info("执行项目ID:"+projectId+"下浮动IP同步---结束");
			return "success";
		} catch (AppException e) {
			logger.error("执行项目ID:"+projectId+"下浮动IP同步出错:" + e.getMessage(),e);
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

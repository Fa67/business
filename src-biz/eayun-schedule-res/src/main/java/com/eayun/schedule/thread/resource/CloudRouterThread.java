package com.eayun.schedule.thread.resource;

import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.schedule.service.CloudRouterService;

/**
 * 路由同步线程
 * @author zhouhaitao
 *
 */
public class CloudRouterThread implements Callable<String>{
	private final static Log logger = LogFactory
			.getLog(CloudRouterThread.class);
	private BaseDcDataCenter dataCenter;
	private CloudRouterService service;
	
	public CloudRouterThread(CloudRouterService service) {
		this.service = service;
	}

	@Override
	public String call() throws Exception {
		logger.info("执行项目同步---开始");
		try {
			service.synchData(dataCenter);
			logger.info("执行项目同步---结束");
			return "success";
		} catch (AppException e) {
			logger.error("执行项目同步出错:" + e.getMessage(),e);
			return "failed";
		}
	}

	public BaseDcDataCenter getDataCenter() {
		return dataCenter;
	}

	public void setDataCenter(BaseDcDataCenter dataCenter) {
		this.dataCenter = dataCenter;
	}

}

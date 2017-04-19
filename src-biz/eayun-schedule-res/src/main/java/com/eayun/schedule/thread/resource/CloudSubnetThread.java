package com.eayun.schedule.thread.resource;

import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.schedule.service.CloudSubnetService;

/**
 * 子网同步线程
 * @author zhouhaitao
 *
 */
public class CloudSubnetThread implements Callable<String>{
	private final static Log logger = LogFactory
			.getLog(CloudSubnetThread.class);
	private BaseDcDataCenter dataCenter;
	private CloudSubnetService service;
	
	public CloudSubnetThread(CloudSubnetService service) {
		this.service = service;
	}

	@Override
	public String call() throws Exception {
		logger.info("执行子网同步---开始");
		try {
			service.synchData(dataCenter);
			logger.info("执行子网同步---结束");
			return "success";
		} catch (AppException e) {
			logger.error("执行子网同步出错:" + e.getMessage(),e);
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

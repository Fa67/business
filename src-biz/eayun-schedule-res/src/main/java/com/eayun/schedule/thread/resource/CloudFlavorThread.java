package com.eayun.schedule.thread.resource;

import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.schedule.service.CloudVmFlavorService;

/**
 * 云主机类型同步线程
 * @author zhouhaitao
 *
 */
public class CloudFlavorThread implements Callable<String>{
	private final static Log logger = LogFactory.getLog(CloudFlavorThread.class);
	private BaseDcDataCenter dataCenter;
	private CloudVmFlavorService service;
	
	public CloudFlavorThread(CloudVmFlavorService service) {
		this.service = service;
	}
	
	@Override
	public String call() throws Exception {
		logger.info("执行云主机类型同步---开始");
		try {
			service.synchData(dataCenter);
			logger.info("执行云主机类型同步---结束");
			return "success";
		} catch (AppException e) {
			logger.error("执行云主机类型同步出错:" + e.getMessage(),e);
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

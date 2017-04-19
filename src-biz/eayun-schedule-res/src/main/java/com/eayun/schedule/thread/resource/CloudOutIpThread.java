package com.eayun.schedule.thread.resource;


import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.schedule.service.CloudOutIpService;

/**
 * OutIp
 * @author zhouhaitao
 *
 */
public class CloudOutIpThread implements Callable<String>{
	private CloudOutIpService cloudOutIpService;
	
	private BaseDcDataCenter dataCenter;
	
	private static final Log logger = LogFactory.getLog(CloudOutIpThread.class);
	
	public CloudOutIpThread(CloudOutIpService cloudOutIpService) {
		this.cloudOutIpService = cloudOutIpService;
	}
	
	@Override
	public String call() throws Exception {
		logger.info("执行OutIp同步---开始");
		try {
			cloudOutIpService.synchData(dataCenter);
			logger.info("执行OutIp同步---结束");
			return "success";
		} catch (AppException e) {
			logger.error("执行OutIp同步出错:"+e.getMessage(),e);
			return "failed";
		}
	}

	public BaseDcDataCenter getDataCenter() {
		return this.dataCenter;
	}

	public void setDataCenter(BaseDcDataCenter dataCenter) {
		this.dataCenter = dataCenter;
	}

}

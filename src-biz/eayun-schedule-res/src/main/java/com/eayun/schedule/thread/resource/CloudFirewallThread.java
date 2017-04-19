package com.eayun.schedule.thread.resource;

import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.schedule.service.CloudFirewallService;

/**
 * 防火墙同步线程
 * 
 * @author zhouhaitao
 * 
 */
public class CloudFirewallThread implements Callable<String> {
	private final static Log logger = LogFactory
			.getLog(CloudFirewallThread.class);
	private BaseDcDataCenter dataCenter;
	private CloudFirewallService service;
	
	public CloudFirewallThread(CloudFirewallService service) {
		this.service = service;
	}

	@Override
	public String call() throws Exception {
		logger.info("执行防火墙同步---开始");
		try {
			service.synchData(dataCenter);
			logger.info("执行防火墙同步---结束");
			return "success";
		} catch (AppException e) {
			logger.error("执行防火墙同步出错:" + e.getMessage(),e);
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

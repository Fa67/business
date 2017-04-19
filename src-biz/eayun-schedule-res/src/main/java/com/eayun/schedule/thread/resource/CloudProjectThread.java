package com.eayun.schedule.thread.resource;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.schedule.service.CloudProjectService;

/**
 * 项目同步线程
 * @author zhouhaitao
 *
 */
public class CloudProjectThread implements Callable<String>{
	private final static Log logger = LogFactory
			.getLog(CloudProjectThread.class);
	private BaseDcDataCenter dataCenter;
	private CountDownLatch cdl;
	private CloudProjectService service;
	
	public CloudProjectThread(CloudProjectService service) {
		this.service = service;
	}

	@Override
	public String call() throws Exception {
		logger.info("执行项目同步---开始");
		try {
			service.synchData(dataCenter);
			logger.info("执行项目同步---结束");
			cdl.countDown();
			return "success";
		} catch (Exception e) {
			logger.error("执行项目同步出错:" + e.getMessage(),e);
			throw e;
		}
	}

	public BaseDcDataCenter getDataCenter() {
		return dataCenter;
	}

	public void setDataCenter(BaseDcDataCenter dataCenter) {
		this.dataCenter = dataCenter;
	}

	public CountDownLatch getCdl() {
		return cdl;
	}

	public void setCdl(CountDownLatch cdl) {
		this.cdl = cdl;
	}

}

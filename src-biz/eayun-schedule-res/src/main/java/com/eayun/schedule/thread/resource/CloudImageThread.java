package com.eayun.schedule.thread.resource;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.schedule.service.CloudImageService;

/**
 * 镜像同步线程
 * @author zhouhaitao
 *
 */
public class CloudImageThread implements Callable<String>{
	private final static Log logger=LogFactory.getLog(CloudImageThread.class);
	private BaseDcDataCenter dataCenter;
	private CountDownLatch cdl;
	private CloudImageService service;
	
	public CloudImageThread(CloudImageService service) {
		this.service = service;
	}
	
	@Override
	public String call() throws Exception {
		logger.info("执行镜像同步---开始");
		try {
			service.synchData(dataCenter);
			logger.info("执行镜像同步---结束");
			cdl.countDown();
			return "success";
		} catch (AppException e) {
			logger.error("执行镜像同步出错:" + e.getMessage(),e);
			return "failed";
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

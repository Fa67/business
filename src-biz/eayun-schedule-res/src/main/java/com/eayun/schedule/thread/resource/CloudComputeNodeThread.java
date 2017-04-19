package com.eayun.schedule.thread.resource;


import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.schedule.service.CloudComputenodeService;

/**
 * 计算节点同步线程
 * @author zhouhaitao
 *
 */
public class CloudComputeNodeThread implements Callable<String>{

	private CloudComputenodeService cloudComputenodeService;
	
	public CloudComputeNodeThread(CloudComputenodeService cloudComputenodeService) {
		this.cloudComputenodeService = cloudComputenodeService;
	}
	
	private BaseDcDataCenter dataCenter;
	
	private static final Log logger = LogFactory.getLog(CloudComputeNodeThread.class);
	
	@Override
	public String call() throws Exception {
		logger.info("执行计算节点同步---开始");
		try {
			cloudComputenodeService.synchData(dataCenter);
			logger.info("执行结算节点同步---结束");
			return "success";
		} catch (AppException e) {
			logger.error("执行计算节点同步出错:"+e.getMessage(),e);
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

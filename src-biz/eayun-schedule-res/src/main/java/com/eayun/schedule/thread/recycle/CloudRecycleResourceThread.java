package com.eayun.schedule.thread.recycle;

import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eayun.schedule.service.CloudRecycleDeleteService;

public class CloudRecycleResourceThread implements Callable<String>{
	
	private static final Log logger = LogFactory.getLog(CloudRecycleResourceThread.class);
	
	private CloudRecycleDeleteService cloudRecycleDeleteService;
	
	public CloudRecycleResourceThread(CloudRecycleDeleteService cloudRecycleDeleteService){
		this.cloudRecycleDeleteService = cloudRecycleDeleteService;
	}
	
	@Override
	public String call() throws Exception {
		try{
			logger.info("执行自动删除回收站的资源");
			boolean result = cloudRecycleDeleteService.handleExpireRecycleReource();
			if(result){
				return "success";
			}
			else{
				return "failed";
			}
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			return "failed";
		}
	}
	
}

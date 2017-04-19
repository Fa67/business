package com.eayun.schedule.service;

public interface CloudRecycleDeleteService {
	
	/**
	 * 处理回收站中已经过期的资源
	 * -----------------------
	 * @author zhouhaitao
	 * @throws Exception 
	 * 
	 * @return
	 */
	public boolean handleExpireRecycleReource() throws Exception;
}

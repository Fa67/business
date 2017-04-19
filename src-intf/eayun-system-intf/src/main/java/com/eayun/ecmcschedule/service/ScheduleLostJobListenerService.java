package com.eayun.ecmcschedule.service;

import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.support.Page;

public interface ScheduleLostJobListenerService {

	/**
	 * 分页获取存储在Mongodb中的计划任务漏跑详细信息
	 * @param paramsMap 请求参数封装对象
	 * @return
	 */
	public Page getTaskList(ParamsMap paramsMap) ;
	
}

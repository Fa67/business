package com.eayun.eayunstack.service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.eayunstack.model.EayunQosQueue;

public interface OpenstackEayunQosQueueService {
	
	public EayunQosQueue create(String datacenterId, JSONObject data) throws AppException ;

	public boolean delete(String datacenterId, String id) throws AppException ;
	
	public EayunQosQueue modify(String datacenterId, JSONObject data,String id) throws AppException ;

}

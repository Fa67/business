package com.eayun.eayunstack.service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.eayunstack.model.EayunQosFilter;

public interface OpenstackEayunQosFilterService {
	
	public EayunQosFilter create(String datacenterId, JSONObject data) throws AppException ;
	
	public boolean delete(String datacenterId, String id) throws AppException ;
}

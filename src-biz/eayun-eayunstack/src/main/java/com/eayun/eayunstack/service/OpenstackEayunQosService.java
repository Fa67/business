package com.eayun.eayunstack.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.eayunstack.model.EayunQos;

public interface OpenstackEayunQosService {
	
	public EayunQos create(String datacenterId, JSONObject data) throws AppException ;
	
	public boolean delete(String datacenterId, String id) throws AppException ;
	
	public EayunQos modify(String datacenterId, JSONObject data,String id)throws AppException;
	
	public EayunQos get(String datacenterId,String id)throws AppException;
	
	public List<EayunQos> list(String datacenterId)throws AppException;
}

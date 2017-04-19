package com.eayun.eayunstack.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.eayunstack.model.EayunQos;
import com.eayun.eayunstack.service.OpenstackEayunQosService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;

@Service
public class OpenstackEayunQosServiceImpl extends OpenstackBaseServiceImpl<EayunQos> implements OpenstackEayunQosService {

	@Override
	public EayunQos create(String datacenterId, JSONObject data) throws AppException {
		EayunQos qos = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.EAYUN_QOS_URI);
		JSONObject result = restService.create(restTokenBean,
				OpenstackUriConstant.EAYUN_QOS_DATA_NAME, data);
		qos = restService.json2bean(result, EayunQos.class);
		return qos;
	}

	@Override
	public boolean delete(String datacenterId, String id) throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.EAYUN_QOS_URI + "/" + id);
		return restService.delete(restTokenBean);
	}
	
	public EayunQos modify(String datacenterId, JSONObject data,String id)throws AppException {
		EayunQos qos = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.EAYUN_QOS_URI + "/" + id);
		JSONObject result = restService.update(restTokenBean,
				OpenstackUriConstant.EAYUN_QOS_DATA_NAME, data);
		qos = restService.json2bean(result, EayunQos.class);
		return qos;
	}
	
	public EayunQos get(String datacenterId,String id)throws AppException{
		EayunQos qos = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		JSONObject result = restService.getById(restTokenBean,
				OpenstackUriConstant.EAYUN_QOS_URI+ "/",OpenstackUriConstant.EAYUN_QOS_DATA_NAME, id);
		qos = restService.json2bean(result, EayunQos.class);
		return qos;
	}
	
	public List<EayunQos> list(String datacenterId)throws AppException{
		List <EayunQos> list = new ArrayList<EayunQos>();                                         
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,OpenstackUriConstant.NETWORK_SERVICE_URI);                                                          
		restTokenBean.setUrl(OpenstackUriConstant.EAYUN_QOS_URI);                                           
		List<JSONObject> result = restService.list(restTokenBean,"qoss");
		if (result != null && result.size() > 0) {                                                              
			for (JSONObject jsonObject : result) {                                                                
				EayunQos data = restService.json2bean(jsonObject,EayunQos.class);
					list.add(data);                                                                                  
				}                                                                                                   
			}                                                                                                     
		return list;                                                                                            
	} 
	
}

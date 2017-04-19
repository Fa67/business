package com.eayun.eayunstack.service.impl;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.eayunstack.model.EayunQosQueue;
import com.eayun.eayunstack.service.OpenstackEayunQosQueueService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;

@Service
public class OpenstackEayunQosQueueServiceImpl extends OpenstackBaseServiceImpl<EayunQosQueue> implements OpenstackEayunQosQueueService {

	@Override
	public EayunQosQueue create(String datacenterId, JSONObject data) throws AppException {
		EayunQosQueue qosQueue = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.EAYUN_QOS_QUEUE_URI);
		JSONObject result = restService.create(restTokenBean,
				OpenstackUriConstant.EAYUN_QOS_QUEUE_DATA_NAME, data);
		qosQueue = restService.json2bean(result, EayunQosQueue.class);
		return qosQueue;
	}

	@Override
	public boolean delete(String datacenterId, String id) throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.EAYUN_QOS_QUEUE_URI + "/" + id);
		return restService.delete(restTokenBean);
	}
	
	@Override
	public EayunQosQueue modify(String datacenterId, JSONObject data,String id)throws AppException{
		EayunQosQueue qosQueue = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.EAYUN_QOS_QUEUE_URI + "/" + id);
		JSONObject result = restService.update(restTokenBean,
				OpenstackUriConstant.EAYUN_QOS_QUEUE_DATA_NAME, data);
		qosQueue = restService.json2bean(result, EayunQosQueue.class);
		return qosQueue;
	}
	
}

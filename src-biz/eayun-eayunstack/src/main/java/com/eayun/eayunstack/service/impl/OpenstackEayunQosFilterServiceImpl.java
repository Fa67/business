package com.eayun.eayunstack.service.impl;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.eayunstack.model.EayunQosFilter;
import com.eayun.eayunstack.service.OpenstackEayunQosFilterService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;

@Service
public class OpenstackEayunQosFilterServiceImpl extends OpenstackBaseServiceImpl<EayunQosFilter> implements OpenstackEayunQosFilterService {

	@Override
	public EayunQosFilter create(String datacenterId, JSONObject data) throws AppException {
		EayunQosFilter qosFilter = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.EAYUN_QOS_FILTER_URI);
		JSONObject result = restService.create(restTokenBean,
				OpenstackUriConstant.EAYUN_QOS_FILTER_DATA_NAME, data);
		qosFilter = restService.json2bean(result, EayunQosFilter.class);
		return qosFilter;
	}

	@Override
	public boolean delete(String datacenterId, String id) throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.EAYUN_QOS_FILTER_URI + "/" + id);
		return restService.delete(restTokenBean);
	}
	
}

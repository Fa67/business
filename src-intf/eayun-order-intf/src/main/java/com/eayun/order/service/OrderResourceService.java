package com.eayun.order.service;

import java.util.List;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.order.model.BaseOrderResource;

public interface OrderResourceService {
	
	public void addOrderResource(List<BaseOrderResource> orderResources);
	
	public Page getResourceByOrderNo(QueryMap queryMap, String orderNo);

}

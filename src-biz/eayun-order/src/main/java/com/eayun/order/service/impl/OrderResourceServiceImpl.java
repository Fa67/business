package com.eayun.order.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.util.StringUtil;
import com.eayun.order.dao.OrderResourceDao;
import com.eayun.order.model.BaseOrderResource;
import com.eayun.order.service.OrderResourceService;

@Service
@Transactional
public class OrderResourceServiceImpl implements OrderResourceService {

	@Autowired
	private OrderResourceDao orderResourceDao;

	@Override
	public void addOrderResource(List<BaseOrderResource> orderResources) {
		orderResourceDao.save(orderResources);
	}

	@Override
	public Page getResourceByOrderNo(QueryMap queryMap, String orderNo) {
		List<String> params = new ArrayList<String>();
		String hql = "from BaseOrderResource where orderNo = ?";
		if (!StringUtil.isEmpty(orderNo)) {
			params.add(orderNo);
		}
		return orderResourceDao.pagedQuery(hql, queryMap, params.toArray());
	}

}

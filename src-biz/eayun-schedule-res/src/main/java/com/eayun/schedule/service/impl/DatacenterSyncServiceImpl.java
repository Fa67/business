package com.eayun.schedule.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.datacenter.service.DataCenterService;
import com.eayun.schedule.service.CloudResourceService;
import com.eayun.schedule.service.DatacenterSyncService;

@Transactional
@Service
public class DatacenterSyncServiceImpl implements DatacenterSyncService {
	@Autowired
	private DataCenterService dataCenterService;
	@Autowired
	private CloudResourceService service;
	
	@Override
	public void syncDatacenter(String dcId) throws AppException {
		BaseDcDataCenter dataCenter = dataCenterService.getById(dcId);
		service.synchAllData(dataCenter);
	}

}

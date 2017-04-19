package com.eayun.schedule.service;

import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;

public interface CloudResourceService {
	
	public void synchAllData(BaseDcDataCenter dataCenter) throws AppException;
	
}

package com.eayun.schedule.service;

import com.eayun.common.exception.AppException;

public interface DatacenterSyncService {
	public void syncDatacenter(String dcId) throws AppException;
}

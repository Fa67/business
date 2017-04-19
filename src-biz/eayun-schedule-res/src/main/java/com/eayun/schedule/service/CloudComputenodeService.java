package com.eayun.schedule.service;

import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.virtualization.model.BaseCloudComputenode;

public interface CloudComputenodeService {
	
	public void synchData (BaseDcDataCenter dataCenter)throws Exception;
	
	public boolean updateComputenode(BaseCloudComputenode bccn);
}

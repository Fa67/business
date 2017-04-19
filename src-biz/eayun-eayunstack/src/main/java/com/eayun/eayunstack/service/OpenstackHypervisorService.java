package com.eayun.eayunstack.service;

import java.util.List;

import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.Hypervisor;
import com.eayun.virtualization.model.BaseCloudComputenode;

public interface OpenstackHypervisorService {

	public List<Hypervisor> listAll(String datacenterId) throws AppException;
	
	public List<BaseCloudComputenode> getStackList(BaseDcDataCenter dataCenter) throws AppException;
	

}

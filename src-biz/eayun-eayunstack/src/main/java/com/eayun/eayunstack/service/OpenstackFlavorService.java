package com.eayun.eayunstack.service;

import java.util.List;

import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.virtualization.model.BaseCloudFlavor;

public interface OpenstackFlavorService {
	
	public List<BaseCloudFlavor> getStackList (BaseDcDataCenter dataCenter);
}

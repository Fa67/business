package com.eayun.eayunstack.service;

import java.util.List;

import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.FirewallPolicy;
import com.eayun.virtualization.model.BaseCloudFwPolicy;

public interface OpenstackFirewallPolicyService extends
		OpenstackBaseService<FirewallPolicy> {
	
	public List<BaseCloudFwPolicy> getStackList (BaseDcDataCenter dataCenter);
}
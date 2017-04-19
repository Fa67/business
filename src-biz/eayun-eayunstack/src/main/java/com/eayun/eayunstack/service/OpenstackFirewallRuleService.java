package com.eayun.eayunstack.service;

import java.util.List;

import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.FirewallRule;
import com.eayun.virtualization.model.BaseCloudFwRule;

public interface OpenstackFirewallRuleService extends
		OpenstackBaseService<FirewallRule> {
	
	public List<BaseCloudFwRule> getStackList (BaseDcDataCenter dataCenter);

	public FirewallRule updateByNetJson(String dcId, String prjId,
			net.sf.json.JSONObject resultData, String fwrId);
}

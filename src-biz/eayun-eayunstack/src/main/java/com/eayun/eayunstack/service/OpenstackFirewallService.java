package com.eayun.eayunstack.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.Firewall;
import com.eayun.virtualization.model.BaseCloudFireWall;

public interface OpenstackFirewallService extends
		OpenstackBaseService<Firewall> {

	
	
	public JSONObject get (String dcId,String fwId) throws Exception;
	
	public List<BaseCloudFireWall> getStackList (BaseDcDataCenter dataCenter);
}

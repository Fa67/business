package com.eayun.eayunstack.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.eayunstack.model.Rule;
import com.eayun.eayunstack.model.SecurityGroup;

public interface OpenstackSecurityGroupRuleService {

	public Rule createRule(String datacenterId, String projectId,
			JSONObject data) throws AppException;

	public List<SecurityGroup> listSecurityGroup(String datacenterId,
			String projectId) throws AppException;

	public Rule create(String datacenterId, String projectId, JSONObject data)
			throws AppException;

	public boolean delete(String datacenterId, String projectId, String id)
			throws AppException;

}

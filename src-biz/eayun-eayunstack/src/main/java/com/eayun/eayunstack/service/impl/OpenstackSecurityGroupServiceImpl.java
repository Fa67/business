package com.eayun.eayunstack.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.Rule;
import com.eayun.eayunstack.model.SecurityGroup;
import com.eayun.eayunstack.service.OpenstackSecurityGroupService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;
import com.eayun.virtualization.model.BaseCloudSecurityGroup;
import com.eayun.virtualization.model.BaseCloudSecurityGroupRule;

@Service
public class OpenstackSecurityGroupServiceImpl extends
		OpenstackBaseServiceImpl<SecurityGroup> implements
		OpenstackSecurityGroupService {
	private static final Log log = LogFactory
			.getLog(OpenstackSecurityGroupServiceImpl.class);

	@Override
	public List<SecurityGroup> list(String datacenterId, String projectId)
			throws AppException {
		List<SecurityGroup> list = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.SECURITY_GROUP_URI);
		// 获取安全组信息
		List<JSONObject> result = restService.list(restTokenBean,
				OpenstackUriConstant.SECURITY_GROUP_URI_NAMES);
		// 获取当前项目下的云硬盘信息

		if (result != null && result.size() > 0) {

			for (JSONObject jsonObject : result) {
				if (list == null) {
					list = new ArrayList<SecurityGroup>();
				}
				// json转换为java对象
				SecurityGroup data = restService.json2bean(jsonObject,
						SecurityGroup.class);

				if (data.getTenant_id() != null
						&& data.getTenant_id().equals(projectId)) {
					list.add(data);
				}
			}
		}
		return list;
	}

	@Override
	// 根据数据中心ID查找
	public List<SecurityGroup> listAll(String datacenterId) throws AppException {
		List<SecurityGroup> list = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.SECURITY_GROUP_URI);
		List<JSONObject> result = restService.list(restTokenBean,
				OpenstackUriConstant.SECURITY_GROUP_URI_NAMES);
		if (result != null && result.size() > 0) {
			for (JSONObject jsonObject : result) {
				if (list == null) {
					list = new ArrayList<SecurityGroup>();
				}
				// json转换为java对象
				SecurityGroup data = restService.json2bean(jsonObject,
						SecurityGroup.class);
				list.add(data);
			}
		}
		return list;
	}

	/**
	 * 删除指定id的安全组
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public boolean delete(String datacenterId, String projectId, String id)
			throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.COMPUTE_SERVICE_URI);
		restTokenBean.setUrl("/os-security-groups/" + id);
		return restService.delete(restTokenBean);
	}

	/**
	 * 创建安全组方法
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 *            json字符串，包含待创建的安全组的具体配置信息
	 * @return
	 * @throws AppException
	 */
	public SecurityGroup create(String datacenterId, String projectId,
			JSONObject data) throws AppException {
		SecurityGroup secrurityGroup = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		
		restTokenBean.setUrl(OpenstackUriConstant.SECURITY_GROUP_URI);
		
		JSONObject result = restService.create(restTokenBean,
				OpenstackUriConstant.SECURITY_GROUP_URI_NAME, data);
		secrurityGroup = restService.json2bean(result, SecurityGroup.class);
		return secrurityGroup;
	}

	public SecurityGroup update(String datacenterId, String projectId,
			JSONObject data, String securityGroupId) throws AppException {
		SecurityGroup secrurityGroup = null;

		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.SECURITY_GROUP_URI + "/"
				+ securityGroupId);
		JSONObject result = restService.update(restTokenBean,
				OpenstackUriConstant.SECURITY_GROUP_URI_NAME, data);
		secrurityGroup = restService.json2bean(result, SecurityGroup.class);
		return secrurityGroup;
	}

	/**
	 * 获取指定数据中心下的指定id的安全组的详情
	 */
	public SecurityGroup getById(String datacenterId, String projectId,
			String securitygroupId) throws AppException {
		SecurityGroup result = null;

		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.COMPUTE_SERVICE_URI);
		// 执行具体业务操作，并获取返回结果
		JSONObject json = restService.getById(restTokenBean,
				OpenstackUriConstant.SECURITY_GROUP_URI + "/",
				OpenstackUriConstant.SECURITY_GROUP_URI_NAME, securitygroupId);
		if (json != null) {
			result = restService.json2bean(json, SecurityGroup.class);
		}

		return result;
	}
	
	@SuppressWarnings("rawtypes")
	public Map<String,List> getStackList (BaseDcDataCenter dataCenter){
		Map<String, List> map = new HashMap<String, List>();
		List<BaseCloudSecurityGroup> groupList = new ArrayList<BaseCloudSecurityGroup>();
		List<BaseCloudSecurityGroupRule> ruleList = new ArrayList<BaseCloudSecurityGroupRule>();
		RestTokenBean restTokenBean = getRestTokenBean(dataCenter.getId(), OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.SECURITY_GROUP_URI);
		List<JSONObject> result = restService.list(restTokenBean, OpenstackUriConstant.SECURITY_GROUP_URI_NAMES);

		if (result != null && result.size() > 0) {
			for (JSONObject jsonObject : result) {
				SecurityGroup data = restService.json2bean(jsonObject, SecurityGroup.class);
				BaseCloudSecurityGroup ccn = new BaseCloudSecurityGroup(data, dataCenter.getId());
				if(null!=data&&null!=data.getSecurity_group_rules()){
					for(Rule rule:data.getSecurity_group_rules()){
						BaseCloudSecurityGroupRule cgr=new BaseCloudSecurityGroupRule(rule,dataCenter.getId());
						ruleList.add(cgr);
					}
				}
				groupList.add(ccn);
			}
		}
		map.put("GroupList", groupList);
		map.put("RuleList", ruleList);
		return map;
	}
}

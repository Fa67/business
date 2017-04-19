package com.eayun.eayunstack.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.eayunstack.model.Rule;
import com.eayun.eayunstack.model.SecurityGroup;
import com.eayun.eayunstack.service.OpenstackSecurityGroupRuleService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;

@Service
public class OpenstackSecurityGroupRuleServiceImpl extends
		OpenstackBaseServiceImpl<Rule> implements
		OpenstackSecurityGroupRuleService {

	private static final Logger log = LoggerFactory
			.getLogger(OpenstackSecurityGroupRuleServiceImpl.class);

	/**
	 * 删除指定id的规则
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
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl("/v2.0/security-group-rules/" + id);
		return restService.delete(restTokenBean);
	}

	/**
	 * 创建规则
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 *            json字符串，包含待创建的安全组的具体配置信息
	 * @return
	 * @throws AppException
	 */
	public Rule create(String datacenterId, String projectId, JSONObject data)
			throws AppException {
		Rule resultData = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl("/v2.0/security-group-rules");
		JSONObject result = restService.create(restTokenBean,
				"security_group_rule", data);
		resultData = restService.json2bean(result, Rule.class);
		return resultData;
	}

	/**
	 * 创建安全组规则方法
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 *            json字符串，包含待创建的安全组的具体配置信息
	 * @return
	 * @throws AppException
	 */
	public Rule createRule(String datacenterId, String projectId,
			JSONObject data) throws AppException {
		Rule rule = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.SECURITY_GROUP_RULES_URI);
		// 执行具体业务操作，并获取返回结果
		JSONObject result = restService.create(restTokenBean,
				OpenstackUriConstant.SECURITY_GROUP_RULES_NAME, data);
		// 将获取的JSONObject对象转换为model包中定义的与之对应的java对象
		if (result != null) {
			rule = restService.json2bean(result, Rule.class);
		}
		return rule;
	}

	// 查询所属项目的安全组；
	public List<SecurityGroup> listSecurityGroup(String datacenterId,
			String projectId) throws AppException {
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
}

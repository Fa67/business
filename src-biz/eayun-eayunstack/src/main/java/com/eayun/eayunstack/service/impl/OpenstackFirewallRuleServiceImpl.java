package com.eayun.eayunstack.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.FirewallRule;
import com.eayun.eayunstack.service.OpenstackFirewallRuleService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;
import com.eayun.virtualization.model.BaseCloudFwRule;
import com.eayun.virtualization.model.BaseCloudProject;
import com.eayun.virtualization.model.CloudProject;

@Service
public class OpenstackFirewallRuleServiceImpl extends
		OpenstackBaseServiceImpl<FirewallRule> implements
		OpenstackFirewallRuleService {
	private static final Logger log = LoggerFactory
			.getLogger(OpenstackFirewallRuleServiceImpl.class);

	private void initData(FirewallRule data, JSONObject object) {

	}

	private List<FirewallRule> list(RestTokenBean restTokenBean)
			throws AppException {
		List<FirewallRule> list = null;
		restTokenBean.setUrl(OpenstackUriConstant.FIREWALL_RULE_URI);
		List<JSONObject> result = restService.list(restTokenBean,
				OpenstackUriConstant.FIREWALL_RULE_DATA_NAMES);
		if (result != null && result.size() > 0) {
			for (JSONObject jsonObject : result) {
				if (list == null) {
					list = new ArrayList<FirewallRule>();
				}
				FirewallRule data = restService.json2bean(jsonObject,
						FirewallRule.class);
				initData(data, jsonObject);
				if (restTokenBean.getTenantId().equals(data.getTenant_id())) {
					list.add(data);
				}
			}
		}

		return list;
	}

	public List<FirewallRule> list(String datacenterId, String projectId)
			throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);

		return list(restTokenBean);
	}

	public List<FirewallRule> listAll(String datacenterId) throws AppException {
		List<FirewallRule> list = null;
		// 获取数据库中项目的列表
		List<CloudProject> projectList = projectService
				.getProjectListByDataCenter(datacenterId);
		// 项目列表非空并且长度大于0时
		if (projectList != null && projectList.size() > 0) {
			if (list == null) {
				list = new ArrayList<FirewallRule>();
			}
			RestTokenBean restTokenBean = null;
			// 对每个项目调用list方法，并将每一次的操作结果集全部放入本方法中定义的list变量中
			for (BaseCloudProject cloudProject : projectList) {
				if (restTokenBean == null) {
					restTokenBean = getRestTokenBean(datacenterId,
							cloudProject.getProjectId(),
							OpenstackUriConstant.NETWORK_SERVICE_URI);
				} else {
					restTokenBean.setTenantId(cloudProject.getProjectId());
				}

				list.addAll(list(restTokenBean));
			}
		}

		return list;
	}

	/**
	 * 获取指定数据中心下的指定id的云硬盘的详情
	 */
	public FirewallRule getById(String datacenterId, String projectId, String id)
			throws AppException {
		FirewallRule object = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		JSONObject json = restService.getById(restTokenBean,
				OpenstackUriConstant.FIREWALL_RULE_URI + "/",
				OpenstackUriConstant.FIREWALL_RULE_DATA_NAME, id);
		if (json != null) {
			object = restService.json2bean(json, FirewallRule.class);
			initData(object, json);
		}

		return object;
	}

	/**
	 * 创建云主机方法
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 *            json字符串，包含待创建的云主机的具体配置信息
	 * @return
	 * @throws AppException
	 */
	public FirewallRule create(String datacenterId, String projectId,
			JSONObject data) throws AppException {
		FirewallRule object = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.FIREWALL_RULE_URI);
		JSONObject result = restService.create(restTokenBean,
				OpenstackUriConstant.FIREWALL_RULE_DATA_NAME, data);
		object = restService.json2bean(result, FirewallRule.class);

		return object;
	}

	/**
	 * 删除指定id的云主机
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public boolean delete(String datacenterId, String projectId, String id)
			throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.FIREWALL_RULE_URI + "/" + id);
		return restService.delete(restTokenBean);
	}

	/**
	 * 修改指定id的云主机，允许修改的数据为云主机的名称和ipv4，ipv6地址
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 *            修改的配置信息
	 * @param voe
	 * @return
	 * @throws AppException
	 */
	public FirewallRule update(String datacenterId, String projectId,
			JSONObject data, String id) throws AppException {
		FirewallRule object = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.FIREWALL_RULE_URI + "/" + id);

		JSONObject result = restService.update(restTokenBean,
				OpenstackUriConstant.FIREWALL_RULE_DATA_NAME, data);
		object = restService.json2bean(result, FirewallRule.class);

		return object;
	}
	
	@Override
	public FirewallRule updateByNetJson(String datacenterId, String prjId,
			net.sf.json.JSONObject data, String id) {
		FirewallRule object = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.FIREWALL_RULE_URI + "/" + id);

		JSONObject result = restService.updateByNetJson(restTokenBean,
				OpenstackUriConstant.FIREWALL_RULE_DATA_NAME, data);
		object = restService.json2bean(result, FirewallRule.class);

		return object;
		
	}

	/**
	 * 获取底层数据中心下的防火墙规则
	 * -----------------
	 * @author zhouhaitao
	 * @param dataCenter
	 * 
	 * @return
	 * 
	 */
	public List<BaseCloudFwRule> getStackList(BaseDcDataCenter dataCenter) {
		List <BaseCloudFwRule> list = new ArrayList<BaseCloudFwRule>(); 
		RestTokenBean restTokenBean = getRestTokenBean(dataCenter.getId(),
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.FIREWALL_RULE_URI);
		List<JSONObject> result = restService.list(restTokenBean,OpenstackUriConstant.FIREWALL_RULE_DATA_NAMES);

		if (result != null && result.size() > 0) {
			for (JSONObject jsonObject : result) {
				FirewallRule fwRule = restService.json2bean(jsonObject,
						FirewallRule.class);
				BaseCloudFwRule ccn=new BaseCloudFwRule(fwRule,dataCenter.getId());
					list.add(ccn);
				}
			}
		return list;
	}

	

}

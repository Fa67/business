package com.eayun.eayunstack.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.FirewallPolicy;
import com.eayun.eayunstack.service.OpenstackFirewallPolicyService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;
import com.eayun.virtualization.model.BaseCloudFwPolicy;
import com.eayun.virtualization.model.BaseCloudProject;
import com.eayun.virtualization.model.CloudProject;

@Service
public class OpenstackFirewallPolicyServiceImpl extends
		OpenstackBaseServiceImpl<FirewallPolicy> implements
		OpenstackFirewallPolicyService {
	private static final Log log = LogFactory
			.getLog(OpenstackFirewallPolicyServiceImpl.class);

	private void initData(FirewallPolicy data, JSONObject object) {

	}

	private List<FirewallPolicy> list(RestTokenBean restTokenBean)
			throws AppException {
		List<FirewallPolicy> list = null;
		restTokenBean.setUrl(OpenstackUriConstant.FIREWALL_POLICY_URI);
		List<JSONObject> result = restService.list(restTokenBean,
				OpenstackUriConstant.FIREWALL_POLICY_DATA_NAMES);

		if (result != null && result.size() > 0) {
			for (JSONObject jsonObject : result) {
				if (list == null) {
					list = new ArrayList<FirewallPolicy>();
				}
				FirewallPolicy data = restService.json2bean(jsonObject,
						FirewallPolicy.class);
				initData(data, jsonObject);
				// 以项目id过滤返回结果
				if (restTokenBean.getTenantId() != null
						&& restTokenBean.getTenantId().equals(
								data.getTenant_id())) {
					list.add(data);
				}
			}
		}
		return list;
	}

	/**
	 * 获取指定数据中心的指定项目下的防火墙列表
	 */
	public List<FirewallPolicy> list(String datacenterId, String projectId)
			throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);

		return list(restTokenBean);
	}

	/**
	 * 获取指定数据中心下的所有项目的防火墙策略
	 */
	public List<FirewallPolicy> listAll(String datacenterId)
			throws AppException {
		List<FirewallPolicy> list = null;

		List<CloudProject> projectList = projectService
				.getProjectListByDataCenter(datacenterId);
		// 项目列表非空并且长度大于0时
		if (projectList != null && projectList.size() > 0) {
			if (list == null) {
				list = new ArrayList<FirewallPolicy>();
			}
			RestTokenBean restTokenBean = null;
			for (BaseCloudProject cloudProject : projectList) {
				if (restTokenBean == null) {
					restTokenBean = getRestTokenBean(datacenterId,
							cloudProject.getProjectId(),
							OpenstackUriConstant.COMPUTE_SERVICE_URI);
				} else {
					restTokenBean.setTenantId(cloudProject.getProjectId());
				}
				list.addAll(list(restTokenBean));
			}
		}

		return list;
	}

	/**
	 * 获取指定数据中心下的指定id的防火墙策略的详情
	 */
	public FirewallPolicy getById(String datacenterId, String projectId,
			String id) throws AppException {
		FirewallPolicy object = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		JSONObject json = restService.getById(restTokenBean,
				OpenstackUriConstant.FIREWALL_POLICY_URI + "/",
				OpenstackUriConstant.FIREWALL_POLICY_DATA_NAME, id);
		if (json != null) {
			// 转换成java对象
			object = restService.json2bean(json, FirewallPolicy.class);
			// 名称特殊的key，调用initData方法做数据初始化
			initData(object, json);
		}

		return object;
	}

	/**
	 * 创建防火墙策略方法
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 *            json字符串，包含待创建的云主机的具体配置信息
	 * @return
	 * @throws AppException
	 */
	public FirewallPolicy create(String datacenterId, String projectId,
			JSONObject data) throws AppException {
		FirewallPolicy object = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.FIREWALL_POLICY_URI);
		JSONObject result = restService.create(restTokenBean,
				OpenstackUriConstant.FIREWALL_POLICY_DATA_NAME, data);
		object = restService.json2bean(result, FirewallPolicy.class);

		return object;
	}

	/**
	 * 删除指定id的防火墙策略
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
		restTokenBean.setUrl(OpenstackUriConstant.FIREWALL_POLICY_URI + "/"
				+ id);
		return restService.delete(restTokenBean);
	}

	/**
	 * 修改指定id的防火墙策略
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 *            修改的配置信息
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public FirewallPolicy update(String datacenterId, String projectId,
			JSONObject data, String id) throws AppException {
		FirewallPolicy object = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.FIREWALL_POLICY_URI + "/"
				+ id);
		JSONObject result = restService.update(restTokenBean,
				OpenstackUriConstant.FIREWALL_POLICY_DATA_NAME, data);
		object = restService.json2bean(result, FirewallPolicy.class);

		return object;
	}
	
	@Override
	public List<BaseCloudFwPolicy> getStackList (BaseDcDataCenter dataCenter){
		List<BaseCloudFwPolicy> list = null;
		RestTokenBean restTokenBean = getRestTokenBean(dataCenter.getId(),
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.FIREWALL_POLICY_URI);
		List<JSONObject> result = restService.list(restTokenBean,OpenstackUriConstant.FIREWALL_POLICY_DATA_NAMES);

		if (result != null && result.size() > 0) {
			for (JSONObject jsonObject : result) {
				if (list == null) {
					list = new ArrayList<BaseCloudFwPolicy>();
				}
				FirewallPolicy fwPolicy = restService.json2bean(jsonObject,
						FirewallPolicy.class);
				BaseCloudFwPolicy ccn=new BaseCloudFwPolicy(fwPolicy,dataCenter.getId());
					list.add(ccn);
				}
			}
		return list;
	}
}
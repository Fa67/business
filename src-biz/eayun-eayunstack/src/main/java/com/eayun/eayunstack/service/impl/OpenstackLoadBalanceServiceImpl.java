package com.eayun.eayunstack.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.eayunstack.model.LoadBalance;
import com.eayun.eayunstack.service.OpenstackLoadBalanceService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;
import com.eayun.virtualization.model.BaseCloudProject;
import com.eayun.virtualization.model.CloudProject;

@Service
public class OpenstackLoadBalanceServiceImpl extends
		OpenstackBaseServiceImpl<LoadBalance> implements
		OpenstackLoadBalanceService {
	private static final Log log = LogFactory
			.getLog(OpenstackLoadBalanceServiceImpl.class);

	private void initData(LoadBalance data, JSONObject object) {

	}

	private List<LoadBalance> list(RestTokenBean restTokenBean)
			throws AppException {
		restTokenBean.setUrl(OpenstackUriConstant.LOADBALANCE_URI);
		List<LoadBalance> list = null;
		List<JSONObject> result = restService.list(restTokenBean,
				OpenstackUriConstant.LOADBALANCE_DATA_NAMES);

		if (result != null && result.size() > 0) {
			for (JSONObject jsonObject : result) {
				if (list == null) {
					list = new ArrayList<LoadBalance>();
				}
				LoadBalance data = restService.json2bean(jsonObject,
						LoadBalance.class);
				initData(data, jsonObject);
				list.add(data);
			}
		}

		return list;
	}

	/**
	 * 获取指定数据中心的指定项目下的负载均衡列表
	 */
	@Override
	public List<LoadBalance> list(String datacenterId, String projectId)
			throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		return list(restTokenBean);

	}

	/**
	 * 负载均衡列表查询所有
	 */
	@Override
	public List<LoadBalance> listAll(String datacenterId) throws AppException {
		List<LoadBalance> list = null;
		List<CloudProject> projectList = projectService
				.getProjectListByDataCenter(datacenterId);
		if (projectList != null && projectList.size() > 0) {
			if (list == null) {
				list = new ArrayList<LoadBalance>();
			}
			RestTokenBean restTokenBean = null;
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
	 * 根据id查询负载均衡某一条数据
	 */
	public LoadBalance getById(String datacenterId, String projectId, String id)
			throws AppException {
		LoadBalance object = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		JSONObject json = restService.getById(restTokenBean,
				OpenstackUriConstant.LOADBALANCE_URI + "/",
				OpenstackUriConstant.LOADBALANCE_DATA_NAME, id);
		if (json != null) {
			object = restService.json2bean(json, LoadBalance.class);
			initData(object, json);
		}
		return object;
	}

	/**
	 * 创建负载均衡
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 *            json字符串，包含待创建的负载均衡的具体配置信息
	 * @return
	 * @throws AppException
	 */
	public LoadBalance create(String datacenterId, String projectId,
			JSONObject data) throws AppException {
		LoadBalance object = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.LOADBALANCE_URI);
		JSONObject result = restService.create(restTokenBean,
				OpenstackUriConstant.LOADBALANCE_DATA_NAME, data);
		object = restService.json2bean(result, LoadBalance.class);

		return object;
	}

	/**
	 * 删除负载均衡
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
		restTokenBean.setUrl(OpenstackUriConstant.LOADBALANCE_URI + "/" + id);
		return restService.delete(restTokenBean);
	}

	/**
	 * 修改负载均衡
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 *            修改的配置信息
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public LoadBalance update(String datacenterId, String projectId,
			JSONObject data, String id) throws AppException {
		LoadBalance object = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.LOADBALANCE_URI + "/" + id);
		JSONObject result = restService.update(restTokenBean,
				OpenstackUriConstant.LOADBALANCE_DATA_NAME, data);
		object = restService.json2bean(result, LoadBalance.class);

		return object;
	}
}

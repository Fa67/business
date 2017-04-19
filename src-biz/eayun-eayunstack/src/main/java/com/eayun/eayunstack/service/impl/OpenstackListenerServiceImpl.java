package com.eayun.eayunstack.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.eayunstack.model.Listener;
import com.eayun.eayunstack.service.OpenstackListenerService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;
import com.eayun.virtualization.model.BaseCloudProject;
import com.eayun.virtualization.model.CloudProject;

@Service
public class OpenstackListenerServiceImpl extends
		OpenstackBaseServiceImpl<Listener> implements OpenstackListenerService {
	private static final Logger log = LoggerFactory
			.getLogger(OpenstackSnapshotServiceImpl.class);

	private void initData(Listener data, JSONObject object) {

	}

	private List<Listener> list(RestTokenBean restTokenBean)
			throws AppException {
		List<Listener> list = null;
		restTokenBean.setUrl(OpenstackUriConstant.LISTENER_URI);
		List<JSONObject> result = restService.list(restTokenBean,
				OpenstackUriConstant.LISTENER_DATA_NAMES);

		if (result != null && result.size() > 0) {
			for (JSONObject jsonObject : result) {
				if (list == null) {
					list = new ArrayList<Listener>();
				}
				Listener data = restService.json2bean(jsonObject,
						Listener.class);
				initData(data, jsonObject);
				list.add(data);
			}
		}

		return list;
	}

	/**
	 * 获取指定数据中心的指定项目下的监听列表
	 */
	@Override
	public List<Listener> list(String datacenterId, String projectId)
			throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);

		return list(restTokenBean);
	}

	/**
	 * 监听列表查询所有
	 */
	@Override
	public List<Listener> listAll(String datacenterId) throws AppException {
		List<Listener> list = null;
		List<CloudProject> projectList = projectService
				.getProjectListByDataCenter(datacenterId);
		if (projectList != null && projectList.size() > 0) {
			if (list == null) {
				list = new ArrayList<Listener>();
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
	 * 根据id查询监听某一条数据
	 */
	public Listener getById(String datacenterId, String projectId, String id)
			throws AppException {
		Listener object = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);

		JSONObject json = restService.getById(restTokenBean,
				OpenstackUriConstant.LISTENER_URI + "/",
				OpenstackUriConstant.LISTENER_DATA_NAME, id);
		if (json != null) {
			object = restService.json2bean(json, Listener.class);
			initData(object, json);
		}
		return object;
	}

	/**
	 * 创建监听
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 *            json字符串，包含待创建的监听的具体配置信息
	 * @return
	 * @throws AppException
	 */
	public Listener create(String datacenterId, String projectId,
			JSONObject data) throws AppException {
		Listener object = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl("v2.0/lbaas/listeners");
		JSONObject result = restService.create(restTokenBean,
				OpenstackUriConstant.LISTENER_DATA_NAME, data);
		object = restService.json2bean(result, Listener.class);

		return object;
	}

	/**
	 * 删除一条监听信息
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
		restTokenBean.setUrl(OpenstackUriConstant.LISTENER_URI + "/" + id);
		return restService.delete(restTokenBean);
	}

	/**
	 * 修改监听信息
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 *            修改的配置信息
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public Listener update(String datacenterId, String projectId,
			JSONObject data, String id) throws AppException {
		Listener object = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.LISTENER_URI + "/" + id);
		JSONObject result = restService.update(restTokenBean,
				OpenstackUriConstant.LISTENER_DATA_NAME, data);
		object = restService.json2bean(result, Listener.class);
		return object;
	}
}
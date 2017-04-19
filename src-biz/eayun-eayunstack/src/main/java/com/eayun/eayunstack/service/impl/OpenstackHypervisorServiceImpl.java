package com.eayun.eayunstack.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.Hypervisor;
import com.eayun.eayunstack.service.OpenstackHypervisorService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;
import com.eayun.virtualization.model.BaseCloudComputenode;

@Service
public class OpenstackHypervisorServiceImpl extends
		OpenstackBaseServiceImpl<Hypervisor> implements
		OpenstackHypervisorService {
	private static final Logger log = LoggerFactory
			.getLogger(OpenstackHypervisorServiceImpl.class);

	/**
	 * 获取指定数据中心下的所有项目的计算节点总和的列表
	 */
	@Override
	public List<Hypervisor> listAll(String datacenterId) throws AppException {
		List<Hypervisor> list = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.COMPUTE_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.HYPERVISOR_URI);
		List<JSONObject> result = restService.list(restTokenBean,
				OpenstackUriConstant.HYPERVISOR_DATA_NAMES);
		if (result != null) {
			if (list == null) {
				list = new ArrayList<Hypervisor>();
			}
			for (JSONObject jsonObject : result) {
				Hypervisor host = restService.json2bean(jsonObject,
						Hypervisor.class);
				list.add(host);
			}
		}
		return list;
	}
	
	/**
	 * 查询数据中心下的云资源
	 * ------------------
	 * @author zhouhaitao
	 * @param dataCenter
	 * @throws AppException
	 * 
	 * @return 
	 */
	public List<BaseCloudComputenode> getStackList(BaseDcDataCenter dataCenter) throws AppException{
		List<BaseCloudComputenode> list = null;
		RestTokenBean restTokenBean = getRestTokenBean(dataCenter.getId(),
				OpenstackUriConstant.COMPUTE_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.HYPERVISOR_URI);
		List<JSONObject> result = restService.list(restTokenBean,
				OpenstackUriConstant.HYPERVISOR_DATA_NAMES);
		if (result != null) {
			if (list == null) {
				list = new ArrayList<BaseCloudComputenode>();
			}
			for (JSONObject jsonObject : result) {
				Hypervisor host = restService.json2bean(jsonObject,
						Hypervisor.class);
				BaseCloudComputenode ccn = new BaseCloudComputenode(host, dataCenter.getId());
				list.add(ccn);
			}
		}
		return list;
	}
}

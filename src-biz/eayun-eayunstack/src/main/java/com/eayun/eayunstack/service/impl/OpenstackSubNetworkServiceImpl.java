package com.eayun.eayunstack.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.SubNetwork;
import com.eayun.eayunstack.service.OpenstackSubNetworkService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;
import com.eayun.virtualization.model.BaseCloudSubNetWork;

/**
 * openstack子网络服务的service类
 */
@Service
public class OpenstackSubNetworkServiceImpl extends
		OpenstackBaseServiceImpl<SubNetwork> implements
		OpenstackSubNetworkService {

	private static final Logger log = LoggerFactory
			.getLogger(OpenstackSubNetworkServiceImpl.class);
	/**
	 * 私有方法，用于将JSONObject对象中的一些无法自动转换的参数，手动设置到java对象中
	 * 
	 * @param vm
	 * @param object
	 */
	private void initData(SubNetwork subnetwork, JSONObject object) {

	}

	/**
	 * 获取指定数据中心的指定项目下的子网列表
	 */
	@Override
	public List<SubNetwork> list(String datacenterId, String projectId)
			throws AppException {
		List<SubNetwork> list = null;
		list = this.listAll(datacenterId);
		if (list != null && list.size() > 0) {
			List<SubNetwork> temp = new ArrayList<SubNetwork>();
			for (SubNetwork subNetwork : list) {
				if (subNetwork.getTenant_id().equals(projectId)) {
					temp.add(subNetwork);
				}
			}
			list = temp;
		}
		return list;
	}

	/**
	 * 获取指定数据中心下的所有项目的子网络总和的列表
	 */
	@Override
	public List<SubNetwork> listAll(String datacenterId) throws AppException {
		List<SubNetwork> list = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.SUBNETWORK_URI);

		List<JSONObject> result = restService.list(restTokenBean,
				OpenstackUriConstant.SUBNETWORK_DATA_NAMES);

		if (result != null && result.size() > 0) {
			for (JSONObject jsonObject : result) {
				if (list == null) {
					list = new ArrayList<SubNetwork>();
				}
				SubNetwork data = restService.json2bean(jsonObject,
						SubNetwork.class);
				initData(data, jsonObject);
				list.add(data);
			}
		}

		return list;
	}

	/**
	 * 获取指定数据中心下的指定id的云主机的详情
	 */
	public SubNetwork getById(String datacenterId, String projectId, String id)
			throws AppException {
		SubNetwork result = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		JSONObject json = restService.getById(restTokenBean,
				OpenstackUriConstant.SUBNETWORK_URI + "/",
				OpenstackUriConstant.SUBNETWORK_DATA_NAME, id);
		result = restService.json2bean(json, SubNetwork.class);

		return result;
	}

	/**
	 * 创建网络
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 *            json字符串，包含待创建的云主机的具体配置信息
	 * @return
	 * @throws AppException
	 */
	public SubNetwork create(String datacenterId, String projectId,
			JSONObject data) throws AppException {
		SubNetwork subnetwork = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.SUBNETWORK_URI);
		JSONObject result = restService.create(restTokenBean,
				OpenstackUriConstant.SUBNETWORK_DATA_NAME, data);
		subnetwork = restService.json2bean(result, SubNetwork.class);

		return subnetwork;
	}
	
	public SubNetwork create(String datacenterId, String projectId,
	        net.sf.json.JSONObject data) throws AppException {
	    SubNetwork subnetwork = null;
	    RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
	            OpenstackUriConstant.NETWORK_SERVICE_URI);
	    restTokenBean.setUrl(OpenstackUriConstant.SUBNETWORK_URI);
	    JSONObject result = restService.create(restTokenBean,
	            OpenstackUriConstant.SUBNETWORK_DATA_NAME, data);
	    subnetwork = restService.json2bean(result, SubNetwork.class);
	    
	    return subnetwork;
	}

	/**
	 * 修改网络
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 *            json字符串，包含待修改网络的具体配置信息
	 * @return
	 * @throws AppException
	 */
	public SubNetwork update(String datacenterId, String projectId,
			JSONObject data, String id) throws AppException {
		SubNetwork subnetwork = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.SUBNETWORK_URI + "/" + id);
		JSONObject result = restService.update(restTokenBean,
				OpenstackUriConstant.SUBNETWORK_DATA_NAME, data);
		subnetwork = restService.json2bean(result, SubNetwork.class);
		return subnetwork;
	}

	public boolean delete(String datacenterId, String projectId, String id)
			throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.SUBNETWORK_URI + "/" + id);
		return restService.delete(restTokenBean);
	}
	
	/**                                                                                                         
	 * 获取底层数据中心下的子网                                                               
	 * -----------------                                                                                      
	 * @author zhouhaitao                                                                                     
	 * @param dataCenter                                                                                      
	 *                                                                                                        
	 * @return                                                                                                
	 *                                                                                                        
	 */                                                                                                       
	public List<BaseCloudSubNetWork> getStackList(BaseDcDataCenter dataCenter) {                                  
		List <BaseCloudSubNetWork> list = new ArrayList<BaseCloudSubNetWork>();                                         
		RestTokenBean restTokenBean = getRestTokenBean(dataCenter.getId(),                                              
				OpenstackUriConstant.NETWORK_SERVICE_URI);                                                          
		restTokenBean.setUrl(OpenstackUriConstant.SUBNETWORK_URI);                                           
		List<JSONObject> result = restService.list(restTokenBean,OpenstackUriConstant.SUBNETWORK_DATA_NAMES);
                                                                                                            
		if (result != null && result.size() > 0) {                                                              
			for (JSONObject jsonObject : result) {                                                                
				SubNetwork data = restService.json2bean(jsonObject,                                             
						SubNetwork.class);                                                                            
				 BaseCloudSubNetWork ccn=new BaseCloudSubNetWork(data,dataCenter.getId());                                 
					list.add(ccn);                                                                                    
				}                                                                                                   
			}                                                                                                     
		return list;                                                                                            
	}                                                                                                         
}

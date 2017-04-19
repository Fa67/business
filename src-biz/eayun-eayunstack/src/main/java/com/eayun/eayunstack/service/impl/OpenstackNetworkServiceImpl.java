package com.eayun.eayunstack.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.Network;
import com.eayun.eayunstack.service.OpenstackNetworkService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;
import com.eayun.virtualization.model.BaseCloudNetwork;

/**
 * openstack网络服务的service类
 */
@Service
public class OpenstackNetworkServiceImpl extends
		OpenstackBaseServiceImpl<Network> implements OpenstackNetworkService {

	private static final Logger log = LoggerFactory
			.getLogger(OpenstackNetworkServiceImpl.class);

	/**
	 * 私有方法，用于将JSONObject对象中的一些无法自动转换的参数，手动设置到java对象中
	 * 
	 * @param vm
	 * @param object
	 */
	private void initData(Network network, JSONObject object) {
		if (network != null) {
			network.setRouter_external("true".equals(object.getString("router:external")));
		}
	}

	/**
	 * 获取指定数据中心的指定项目下的云主机列表
	 */
	@Override
	public List<Network> list(String datacenterId, String projectId)
			throws AppException {
		List<Network> list = null;
		list = this.listAll(datacenterId);
		if (list != null && list.size() > 0) {
			List<Network> temp = new ArrayList<Network>();
			for (Network network : list) {
				if (network.getTenant_id().equals(projectId)) {
					temp.add(network);
				}
			}
			list = temp;
		}
		return list;
	}

	/**
	 * 获取指定数据中心下的所有项目的云主机总和的列表
	 */
	@Override
	public List<Network> listAll(String datacenterId) throws AppException {
		List<Network> list = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.NETWORK_URI);
		List<JSONObject> result = restService.list(restTokenBean,
				OpenstackUriConstant.NETWORK_DATA_NAMES);

		if (result != null && result.size() > 0) {
			for (JSONObject jsonObject : result) {
				if (list == null) {
					list = new ArrayList<Network>();
				}
				Network data = restService.json2bean(jsonObject, Network.class);
				initData(data, jsonObject);
				list.add(data);
			}
		}
		return list;
	}

	/**
	 * 获取指定数据中心下的指定id的云网络的详情
	 */
	public Network getById(String datacenterId, String projectId, String id)
			throws AppException {
		Network network = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		JSONObject result = restService.getById(restTokenBean,
				OpenstackUriConstant.NETWORK_URI + "/",
				OpenstackUriConstant.NETWORK_DATA_NAME, id);
		network = restService.json2bean(result, Network.class);
		initData(network, result);
		return network;
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
	public Network create(String datacenterId, String projectId, JSONObject data)
			throws AppException {
		Network network = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.NETWORK_URI);
		JSONObject result = restService.create(restTokenBean,
				OpenstackUriConstant.NETWORK_DATA_NAME, data);
		network = restService.json2bean(result, Network.class);
		initData(network, result);

		return network;
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
	public Network update(String datacenterId, String projectId,
			JSONObject networkObject, String id) throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.NETWORK_URI + "/" + id);
		JSONObject result = restService.update(restTokenBean,
				OpenstackUriConstant.NETWORK_DATA_NAME, networkObject);
		Network network = restService.json2bean(result, Network.class);
		initData(network, result);
		return network;
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
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.NETWORK_URI + "/" + id);
		return restService.delete(restTokenBean);
	}
	
	/**                                                                                                         
	 * 获取底层数据中心下的网络                                                               
	 * -----------------                                                                                      
	 * @author zhouhaitao                                                                                     
	 * @param dataCenter                                                                                      
	 *                                                                                                        
	 * @return                                                                                                
	 *                                                                                                        
	 */                                                                                                       
	public List<BaseCloudNetwork> getStackList(BaseDcDataCenter dataCenter) {                                  
		List <BaseCloudNetwork> list = new ArrayList<>();                                         
		RestTokenBean restTokenBean = getRestTokenBean(dataCenter.getId(),                                              
				OpenstackUriConstant.NETWORK_SERVICE_URI);                                                          
		restTokenBean.setUrl(OpenstackUriConstant.NETWORK_URI);                                           
		List<JSONObject> result = restService.list(restTokenBean,OpenstackUriConstant.NETWORK_DATA_NAMES);
                                                                                                            
		if (result != null && result.size() > 0) {                                                              
			for (JSONObject jsonObject : result) {                                                                
				 Network data = restService.json2bean(jsonObject,                                             
						 Network.class);
				 initData(data, jsonObject);
				 BaseCloudNetwork ccn=new BaseCloudNetwork(data,dataCenter.getId());                                 
					list.add(ccn);                                                                                    
				}                                                                                                   
			}                                                                                                     
		return list;                                                                                            
	} 
	
	/**
	 * 查询网络下的端口
	 * @param dataCenter
	 * @param netId
	 * @return
	 */
	public List<JSONObject> getPortByNet(BaseDcDataCenter dataCenter,String netId,String deviceOwner){
		RestTokenBean restTokenBean = getRestTokenBean(dataCenter.getId(), OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.PORT_URI + "?device_owner="+deviceOwner+"&network_id="+ netId);
		List<JSONObject> portJson = restService.list(restTokenBean, OpenstackUriConstant.PORT_DATA_NAMES);
		return portJson;
	}
	
}

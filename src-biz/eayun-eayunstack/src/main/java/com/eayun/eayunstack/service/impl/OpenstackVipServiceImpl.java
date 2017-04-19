package com.eayun.eayunstack.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.VIP;
import com.eayun.eayunstack.service.OpenstackVipService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;
import com.eayun.virtualization.model.BaseCloudLdVip;

@Service
public class OpenstackVipServiceImpl extends OpenstackBaseServiceImpl<VIP>
		implements OpenstackVipService {
	private static final Logger log = LoggerFactory
			.getLogger(OpenstackVipServiceImpl.class);

	private void initData(VIP data, JSONObject object) {

	}

	/**
	 * 获取指定数据中心的指定项目下的vip列表
	 */
	@Override
	public List<VIP> list(String datacenterId, String projectId)
			throws AppException {
		List<VIP> list = null;
		list = this.listAll(datacenterId);
		if (list != null && list.size() > 0) {
			List<VIP> temp = new ArrayList<VIP>();
			for (VIP vip : list) {
				if (vip.getTenant_id().equals(projectId)) {
					temp.add(vip);
				}
			}
			list = temp;
		}
		return list;
	}

	/**
	 * VIP列表查询所有
	 */
	@Override
	public List<VIP> listAll(String datacenterId) throws AppException {
		List<VIP> list = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.VIP_URI);
		List<JSONObject> result = restService.list(restTokenBean,
				OpenstackUriConstant.VIP_DATA_NAMES);

		if (result != null && result.size() > 0) {
			for (JSONObject jsonObject : result) {
				if (list == null) {
					list = new ArrayList<VIP>();
				}
				if (jsonObject.getString("session_persistence") == null) {
					VIP data = restService.json2bean(jsonObject, VIP.class);
					initData(data, jsonObject);
					list.add(data);
				}
			}
		}

		return list;
	}

	/**
	 * 根据id查询VIP某一条数据
	 */
	public VIP getById(String datacenterId, String projectId, String id)
			throws AppException {
		VIP object = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);

		JSONObject json = restService.getById(restTokenBean,
				OpenstackUriConstant.VIP_URI + "/",
				OpenstackUriConstant.VIP_DATA_NAME, id);
		if (json != null) {
			object = restService.json2bean(json, VIP.class);
			initData(object, json);
		}
		return object;
	}

	/**
	 * 创建VIP
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 *            json字符串，包含待创建的VIP的具体配置信息
	 * @return
	 * @throws AppException
	 */
	public VIP create(String datacenterId, String projectId, JSONObject data)
			throws AppException {
		VIP object = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.VIP_URI);
		JSONObject result = restService.create(restTokenBean,
				OpenstackUriConstant.VIP_DATA_NAME, data);
		object = restService.json2bean(result, VIP.class);
		return object;
	}

	/**
	 * 删除一条VIP信息
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
		restTokenBean.setUrl(OpenstackUriConstant.VIP_URI + "/" + id);
		return restService.delete(restTokenBean);
	}

	/**
	 * 修改VIP信息
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 *            修改的配置信息
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public VIP update(String datacenterId, String projectId, JSONObject data,
			String id) throws AppException {
		VIP object = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.VIP_URI + "/" + id);
		JSONObject result = restService.update(restTokenBean,
				OpenstackUriConstant.VIP_DATA_NAME, data);
		object = restService.json2bean(result, VIP.class);
		return object;
	}
	
	public JSONObject get (String dcId,String fwId) throws Exception{
		RestTokenBean restTokenBean = getRestTokenBean(dcId,OpenstackUriConstant.NETWORK_SERVICE_URI);
		return restService.getJSONById(restTokenBean, OpenstackUriConstant.VIP_URI+"/", fwId);
	}
	
	/**                                                                                                         
	 * 获取底层数据中心下的负载均衡VIP                                                               
	 * -----------------                                                                                      
	 * @author zhouhaitao                                                                                     
	 * @param dataCenter                                                                                      
	 *                                                                                                        
	 * @return                                                                                                
	 *                                                                                                        
	 */                                                                                                       
	public List<BaseCloudLdVip> getStackList(BaseDcDataCenter dataCenter) {                                  
		List <BaseCloudLdVip> list = new ArrayList<BaseCloudLdVip>();                                         
		RestTokenBean restTokenBean = getRestTokenBean(dataCenter.getId(),                                              
				OpenstackUriConstant.NETWORK_SERVICE_URI);                                                          
		restTokenBean.setUrl(OpenstackUriConstant.VIP_URI);                                           
		List<JSONObject> result = restService.list(restTokenBean,OpenstackUriConstant.VIP_DATA_NAMES);
                                                                                                            
		if (result != null && result.size() > 0) {                                                              
			for (JSONObject jsonObject : result) {                                                                
				 VIP data = restService.json2bean(jsonObject,                                             
						 VIP.class);                                                                            
				 BaseCloudLdVip ccn=new BaseCloudLdVip(data,dataCenter.getId());                                 
					list.add(ccn);                                                                                    
				}                                                                                                   
			}                                                                                                     
		return list;                                                                                            
	}                                                                                                         
}

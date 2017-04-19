package com.eayun.eayunstack.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.Pool;
import com.eayun.eayunstack.service.OpenstackPoolService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;
import com.eayun.virtualization.model.BaseCloudLdPool;

@Service
public class OpenstackPoolServiceImpl extends OpenstackBaseServiceImpl<Pool>
		implements OpenstackPoolService {
	private static final Logger log = LoggerFactory
			.getLogger(OpenstackPoolServiceImpl.class);

	private void initData(Pool data, JSONObject object) {

	}

	/**
	 * 获取指定数据中心的指定项目下的资源池列表
	 */
	@Override
	public List<Pool> list(String datacenterId, String projectId)
			throws AppException {
		List<Pool> list = null;
		list = this.listAll(datacenterId);
		if (list != null && list.size() > 0) {
			List<Pool> temp = new ArrayList<Pool>();
			for (Pool pool : list) {
				if (pool.getTenant_id().equals(projectId)) {
					temp.add(pool);
				}
			}
			list = temp;
		}

		return list;
	}

	/**
	 * 资源池列表查询所有
	 */
	@Override
	public List<Pool> listAll(String datacenterId) throws AppException {
		List<Pool> list = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.POOL_URI);
		List<JSONObject> result = restService.list(restTokenBean,
				OpenstackUriConstant.POOL_DATA_NAMES);

		if (result != null && result.size() > 0) {
			for (JSONObject jsonObject : result) {
				if (list == null) {
					list = new ArrayList<Pool>();
				}
				Pool data = restService.json2bean(jsonObject, Pool.class);
				initData(data, jsonObject);
				list.add(data);
			}
		}
		return list;
	}

	/**
	 * 根据id查询资源池某一条数据
	 */
	public Pool getById(String datacenterId, String projectId, String id)
			throws AppException {
		Pool object = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		JSONObject json = restService.getById(restTokenBean,
				OpenstackUriConstant.POOL_URI + "/",
				OpenstackUriConstant.POOL_DATA_NAME, id);
		if (json != null) {
			// 转换成java对象
			object = restService.json2bean(json, Pool.class);
			// 名称特殊的key，调用initData方法做数据初始化
			initData(object, json);
		}

		return object;
	}

	/**
	 * 绑定监控
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 *            json字符串，包含待创建的资源池的具体配置信息
	 * @return
	 * @throws AppException
	 */
	public boolean bind(String datacenterId, String projectId, String id,
			String healthId) throws AppException {
		// 根据操作类型设置request body 对象
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
		temp.put("id", healthId);
		data.put("health_monitor", temp);
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl("v2.0/lb/pools/" + id + "/" + "health_monitors");
		JSONObject result = restService.create(restTokenBean, null, data);
		return true;
	}

	/**
	 * 创建资源池
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 *            json字符串，包含待创建的资源池的具体配置信息
	 * @return
	 * @throws AppException
	 */
	public Pool create(String datacenterId, String projectId, JSONObject data)
			throws AppException {
		Pool object = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.POOL_URI);
		JSONObject result = restService.create(restTokenBean,
				OpenstackUriConstant.POOL_DATA_NAME, data);
		object = restService.json2bean(result, Pool.class);

		return object;
	}

	/**
	 * 删除资源池
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
		restTokenBean.setUrl(OpenstackUriConstant.POOL_URI + "/" + id);
		return restService.delete(restTokenBean);
	}

	/**
	 * 修改资源池
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 *            修改的配置信息
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public Pool update(String datacenterId, String projectId, JSONObject data,
			String id) throws AppException {
		Pool object = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.POOL_URI + "/" + id);
		JSONObject result = restService.update(restTokenBean,
				OpenstackUriConstant.POOL_DATA_NAME, data);
		object = restService.json2bean(result, Pool.class);
		return object;
	}
	
	
	public JSONObject get (String dcId,String fwId) throws Exception{
		RestTokenBean restTokenBean = getRestTokenBean(dcId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		return restService.getJSONById(restTokenBean, OpenstackUriConstant.POOL_URI+"/", fwId);
	}
	
	/**                                                                                                         
	 * 获取底层数据中心下的负载均衡资源池                                                               
	 * -----------------                                                                                      
	 * @author zhouhaitao                                                                                     
	 * @param dataCenter                                                                                      
	 *                                                                                                        
	 * @return                                                                                                
	 *                                                                                                        
	 */                                                                                                       
	public List<BaseCloudLdPool> getStackList(BaseDcDataCenter dataCenter) {                                  
		List <BaseCloudLdPool> list = new ArrayList<BaseCloudLdPool>();                                         
		RestTokenBean restTokenBean = getRestTokenBean(dataCenter.getId(),                                              
				OpenstackUriConstant.NETWORK_SERVICE_URI);                                                          
		restTokenBean.setUrl(OpenstackUriConstant.POOL_URI);                                           
		List<JSONObject> result = restService.list(restTokenBean,OpenstackUriConstant.POOL_DATA_NAMES);
                                                                                                            
		if (result != null && result.size() > 0) {                                                              
			for (JSONObject jsonObject : result) {                                                                
				Pool data = restService.json2bean(jsonObject,                                             
							Pool.class);                                                                            
				 BaseCloudLdPool ccn=new BaseCloudLdPool(data,dataCenter.getId());                                 
					list.add(ccn);                                                                                    
				}                                                                                                   
			}                                                                                                     
		return list;                                                                                            
	}                                                                                                         
}

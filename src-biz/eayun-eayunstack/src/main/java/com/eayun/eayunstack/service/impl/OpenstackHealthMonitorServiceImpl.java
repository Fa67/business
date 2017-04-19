package com.eayun.eayunstack.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.HealthMonitor;
import com.eayun.eayunstack.service.OpenstackHealthMonitorService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;
import com.eayun.virtualization.model.BaseCloudLdMonitor;

@Service
public class OpenstackHealthMonitorServiceImpl extends
		OpenstackBaseServiceImpl<HealthMonitor> implements
		OpenstackHealthMonitorService {
	private static final Logger log = LoggerFactory
			.getLogger(OpenstackHealthMonitorServiceImpl.class);

	private void initData(HealthMonitor data, JSONObject object) {
	}

	/**
	 * 获取指定数据中心的指定项目下的监控列表
	 */
	@Override
	public List<HealthMonitor> list(String datacenterId, String projectId)
			throws AppException {
		List<HealthMonitor> list = null;
		list = this.listAll(datacenterId);
		if (list != null && list.size() > 0) {
			List<HealthMonitor> temp = new ArrayList<HealthMonitor>();
			for (HealthMonitor healthMonitor : list) {
				if (healthMonitor.getTenant_id().equals(projectId)) {
					temp.add(healthMonitor);
				}
			}
			list = temp;
		}

		return list;
	}

	/**
	 * 监控列表查询所有
	 */
	@Override
	public List<HealthMonitor> listAll(String datacenterId) throws AppException {
		List<HealthMonitor> list = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.HEALTH_MONITOR_URI);
		List<JSONObject> result = restService.list(restTokenBean,
				OpenstackUriConstant.HEALTH_MONITOR_DATA_NAMES);

		if (result != null && result.size() > 0) {
			for (JSONObject jsonObject : result) {
				if (list == null) {
					list = new ArrayList<HealthMonitor>();
				}
				HealthMonitor data = restService.json2bean(jsonObject,
						HealthMonitor.class);
				initData(data, jsonObject);
				list.add(data);
			}
		}

		return list;
	}

	/**
	 * 根据id查询监控某一条数据
	 */
	public HealthMonitor getById(String datacenterId, String projectId,
			String id) throws AppException {
		HealthMonitor object = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		JSONObject json = restService.getById(restTokenBean,
				OpenstackUriConstant.HEALTH_MONITOR_URI + "/",
				OpenstackUriConstant.HEALTH_MONITOR_DATA_NAME, id);
		if (json != null) {
			object = restService.json2bean(json, HealthMonitor.class);
			initData(object, json);
		}

		return object;
	}
	@Override
	public JSONObject get(String dcId, String fwId) throws Exception {
		RestTokenBean restTokenBean = getRestTokenBean(dcId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		return restService.getJSONById(restTokenBean, OpenstackUriConstant.HEALTH_MONITOR_URI+"/", fwId);
	}
	/**
	 * 创建监控
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 *            json字符串，包含待创建的监控的具体配置信息
	 * @return
	 * @throws AppException
	 */
	public HealthMonitor create(String datacenterId, String projectId,
			JSONObject data) throws AppException {
		HealthMonitor object = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);

		restTokenBean.setUrl(OpenstackUriConstant.HEALTH_MONITOR_URI);
		JSONObject result = restService.create(restTokenBean,
				OpenstackUriConstant.HEALTH_MONITOR_DATA_NAME, data);
		object = restService.json2bean(result, HealthMonitor.class);

		return object;
	}

	/**
	 * 删除一条监控信息
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
		restTokenBean
				.setUrl(OpenstackUriConstant.HEALTH_MONITOR_URI + "/" + id);
		return restService.delete(restTokenBean);
	}

	/**
	 * 修改监控信息
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 *            修改的配置信息
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public HealthMonitor update(String datacenterId, String projectId,
			JSONObject data, String id) throws AppException {
		HealthMonitor object = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean
				.setUrl(OpenstackUriConstant.HEALTH_MONITOR_URI + "/" + id);
		JSONObject result = restService.update(restTokenBean,
				OpenstackUriConstant.HEALTH_MONITOR_DATA_NAME, data);
		object = restService.json2bean(result, HealthMonitor.class);

		return object;
	}
	/**
	 * 资源池解除一条监控信息
	 * @param datacenterId
	 * @param projectId
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public boolean detachHealthMonitor(String datacenterId, String projectId,String poolId,String monitorId) throws AppException{
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl("v2.0/lb/pools/"+poolId+"/health_monitors/"+monitorId);
		
		return restService.delete(restTokenBean);
	}
	
	/**                                                                                                         
	 * 获取底层数据中心下的负载均衡监控                                                             
	 * -----------------                                                                                      
	 * @author zhouhaitao                                                                                     
	 * @param dataCenter                                                                                      
	 *                                                                                                        
	 * @return                                                                                                
	 *                                                                                                        
	 */                                                                                                       
	public Map<String,Object> getStackList(BaseDcDataCenter dataCenter) { 
		Map<String,Object> map=new HashMap<String,Object>();
		Map<String,List<String>> poolMap =new HashMap<String,List<String>>();
		List <BaseCloudLdMonitor> list = new ArrayList<BaseCloudLdMonitor>();  
		
		RestTokenBean restTokenBean = getRestTokenBean(dataCenter.getId(),                                              
				OpenstackUriConstant.NETWORK_SERVICE_URI);                                                          
		restTokenBean.setUrl(OpenstackUriConstant.HEALTH_MONITOR_URI);                                           
		List<JSONObject> result = restService.list(restTokenBean,OpenstackUriConstant.HEALTH_MONITOR_DATA_NAMES);
                                                                                                            
		if (result != null && result.size() > 0) {                                                              
			for (JSONObject jsonObject : result) { 
				List<String> poolList=new ArrayList<String>();
				HealthMonitor data = restService.json2bean(jsonObject,                                             
						HealthMonitor.class);                                                                            
				initData(poolList,jsonObject);
				BaseCloudLdMonitor ccn=new BaseCloudLdMonitor(data,dataCenter.getId());                                 
				poolMap.put(data.getId(), poolList);
				list.add(ccn);
				
					list.add(ccn);                                                                                    
				}                                                                                                   
			map.put("PoolMap", poolMap);
			map.put("MonitorList", list);
		}
		return map;                                                                                           
	}                                                                                                         
	
	private void initData(List<String> list ,JSONObject json){
		JSONArray poolArrays=json.getJSONArray("pools");
		if(null!=poolArrays&&poolArrays.size()>0){
			for(Object data:poolArrays){
				String poolId=((JSONObject)data).getString("pool_id");
				if(!StringUtils.isEmpty(poolId)){
					list.add(poolId);
				}
			}
		}
	}
}

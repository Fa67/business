package com.eayun.eayunstack.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.FloatIp;
import com.eayun.eayunstack.service.OpenstackFloatIpService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;
import com.eayun.virtualization.model.BaseCloudFloatIp;
import com.eayun.virtualization.model.BaseCloudProject;
import com.eayun.virtualization.model.CloudFloatIp;
import com.eayun.virtualization.model.CloudProject;

@Service
public class OpenstackFloatIpServiceImpl extends
		OpenstackBaseServiceImpl<FloatIp> implements OpenstackFloatIpService {

	private List<FloatIp> list(RestTokenBean restTokenBean) throws AppException {
		List<FloatIp> list = null;
		restTokenBean.setUrl(OpenstackUriConstant.FLOATIP_URL);

		List<JSONObject> result = restService.list(restTokenBean,
				OpenstackUriConstant.FLOATIP_DATA_NAMES);
		if (result != null) {
			if (list == null) {
				list = new ArrayList<FloatIp>();
			}
			for (JSONObject jsonObject : result) {
				FloatIp floatIp = restService.json2bean(jsonObject,
						FloatIp.class);
				floatIp.setTenant_id(restTokenBean.getTenantId());
				list.add(floatIp);
			}
		}

		return list;
	}

	@Override
	public List<FloatIp> list(String datacenterId, String projectId)
			throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.COMPUTE_SERVICE_URI);
		return list(restTokenBean);
	}

	@Override
	public List<FloatIp> listAll(String datacenterId) throws AppException {
		List<FloatIp> list = null;
		List<CloudProject> projectList = projectService
				.getProjectListByDataCenter(datacenterId);
		if (projectList != null && projectList.size() > 0) {
			if (list == null) {
				list = new ArrayList<FloatIp>();
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

				List<FloatIp> floatList = list(restTokenBean);
				if (floatList != null && floatList.size() > 0) {
					list.addAll(floatList);
				}
			}
		}

		return list;
	}

	public FloatIp allocateIp(String datacenterId, String projectId, String pool)
			throws AppException {
		FloatIp floatIp = null;
		JSONObject temp = new JSONObject();
		temp.put("pool", pool);
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.COMPUTE_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.FLOATIP_URL);
		JSONObject result = restService.create(restTokenBean,
				OpenstackUriConstant.FLOATIP_DATA_NAME, temp);
		floatIp = restService.json2bean(result, FloatIp.class);
		return floatIp;
	}

	public boolean deallocateFloatIp(String datacenterId, String projectId,
			String id) throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.COMPUTE_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.FLOATIP_URL + "/" + id);
		return restService.delete(restTokenBean);
	}

	public boolean addFloatIp(String datacenterId, String projectId,String vmId,String vmIp,
			String address) throws AppException {
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
		temp.put("address", address);
		if(!StringUtils.isEmpty(vmIp)&& !"null".equals(vmIp)){
			temp.put("fixed_address", vmIp);
		}
		data.put("addFloatingIp", temp);

		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.COMPUTE_SERVICE_URI);
		restTokenBean
				.setUrl(OpenstackUriConstant.VM_URI + "/" + vmId + "/action");
		restService.create(restTokenBean, null, data);
		return true;

	}

	public boolean removeFloatIp(String datacenterId, String projectId,
			String vm, String address) throws AppException {
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
		temp.put("address", address);
		data.put("removeFloatingIp", temp);
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.COMPUTE_SERVICE_URI);
		restTokenBean
				.setUrl(OpenstackUriConstant.VM_URI + "/" + vm + "/action");
		restService.create(restTokenBean, null, data);
		return true;
	}

	public FloatIp getById(String datacenterId, String projectId, String id)
			throws AppException {
		FloatIp result = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.COMPUTE_SERVICE_URI);
		JSONObject json = restService.getById(restTokenBean,
				OpenstackUriConstant.FLOATIP_URL + "/",
				OpenstackUriConstant.FLOATIP_DATA_NAME, id);
		if (json != null) {
			result = restService.json2bean(json, FloatIp.class);
			initData(result, json);
		}
		return result;
	}

	private void initData(FloatIp result, JSONObject json) {

	}
	
	/**                                                                                                         
	 * 获取底层项目下的浮动IP                                                            
	 * -----------------                                                                                      
	 * @author zhouhaitao                                                                                     
	 * @param dataCenter                                                                                      
	 *                                                                                                        
	 * @return                                                                                                
	 *                                                                                                        
	 */                                                                                                       
	public List<BaseCloudFloatIp> getStackList(BaseDcDataCenter dataCenter,String prjId) {                                  
		List <BaseCloudFloatIp> list = new ArrayList<BaseCloudFloatIp>();                                         
		RestTokenBean restTokenBean = getRestTokenBean(dataCenter.getId(),prjId,                                              
				OpenstackUriConstant.COMPUTE_SERVICE_URI);                                                          
		restTokenBean.setUrl(OpenstackUriConstant.FLOATIP_URL);                                           
		List<JSONObject> result = restService.list(restTokenBean,OpenstackUriConstant.FLOATIP_DATA_NAMES);
                                                                                                            
		if (result != null && result.size() > 0) {                                                              
			for (JSONObject jsonObject : result) {                                                                
				FloatIp data = restService.json2bean(jsonObject,                                             
						FloatIp.class);                                                                            
				 BaseCloudFloatIp ccn=new BaseCloudFloatIp(data,dataCenter.getId(),prjId);                                 
					list.add(ccn);                                                                                    
				}                                                                                                   
			}                                                                                                     
		return list;                                                                                            
	}
	
	/**
	 * 绑定负载均衡器的浮动IP
	 * 
	 * @author zhouhaitao
	 * @param datacenterId
	 * @param projectId
	 * @param portId    portId==null解绑
	 * @param floatId
	 * @return
	 * @throws AppException
	 */
	public boolean bindLoadBalancerFloatIp(String datacenterId, String projectId, String portId,
			String floatId) throws AppException {
		boolean flag = false;
		try{
			JSONObject data = new JSONObject();
			JSONObject temp = new JSONObject();
			temp.put("port_id", portId);
			data.put("floatingip", temp);
			
			RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
					OpenstackUriConstant.NETWORK_SERVICE_URI);
			restTokenBean.setUrl("/v2.0/floatingips/"+floatId);
			restService.update(restTokenBean,OpenstackUriConstant.FLOATIP_DATA_NAME, data);
			flag = true;
		}catch(Exception e){
			flag = false;
			throw e;
		}
		return flag;

	}
	
	/**
	 * 查询底层network服务下的port
	 * -----------------
	 * @author zhouhaitao
	 * @param dataCenter
	 * @return
	 */
	public List<CloudFloatIp> getPoolFloatIpList (BaseDcDataCenter dataCenter){
		List <CloudFloatIp> list = new ArrayList<CloudFloatIp>();                                         
		RestTokenBean restTokenBean = getRestTokenBean(dataCenter.getId(),OpenstackUriConstant.NETWORK_SERVICE_URI);                                                          
		restTokenBean.setUrl("/v2.0/floatingips");                                           
		List<JSONObject> result = restService.list(restTokenBean,"floatingips");
		                                                                                  
		if (result != null && result.size() > 0) {                                                              
			for (JSONObject jsonObject : result) {                                                                
				FloatIp data = restService.json2bean(jsonObject,FloatIp.class);                                                                            
				CloudFloatIp floatIp = new CloudFloatIp();
				
				floatIp.setFloId(data.getId());
				floatIp.setSubnetIp(data.getFixed_ip_address());;
				floatIp.setPortId(data.getPort_id());
				floatIp.setNetId(data.getFloating_network_id());
				
				list.add(floatIp);                                                                                    
			}                                                                                                   
		}                                                                                                     
		return list;                                                                                            

	}
}
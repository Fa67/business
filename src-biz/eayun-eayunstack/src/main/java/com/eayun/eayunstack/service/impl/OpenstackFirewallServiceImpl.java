package com.eayun.eayunstack.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.Firewall;
import com.eayun.eayunstack.service.OpenstackFirewallService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;
import com.eayun.virtualization.model.BaseCloudFireWall;

@Service
public class OpenstackFirewallServiceImpl extends
		OpenstackBaseServiceImpl<Firewall> implements OpenstackFirewallService {
	private static final Log log = LogFactory
			.getLog(OpenstackFirewallServiceImpl.class);

	private void initData(Firewall data, JSONObject object) {

	}

	/**
	 * 获取指定数据中心的指定项目下的防火墙列表
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @return
	 * @throws AppException
	 */
	public List<Firewall> listAll(String datacenterId) throws AppException {
		List<Firewall> list = null;

		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.FIREWALL_URI);
		List<JSONObject> result = null;
		result = restService.list(restTokenBean,
				OpenstackUriConstant.FIREWALL_DATA_NAMES);
		if (result != null && result.size() > 0) {
			for (JSONObject jsonObject : result) {
				if (list == null) {
					list = new ArrayList<Firewall>();
				}
				Firewall data = restService.json2bean(jsonObject,
						Firewall.class);
				initData(data, jsonObject);
				list.add(data);
			}
		}

		return list;
	}

	/**
	 * 
	 * @param datacenterId
	 * @return
	 * @throws AppException
	 */
	public List<Firewall> list(String datacenterId, String projectId)
			throws AppException {
		List<Firewall> list = null;
		List<Firewall> temp = new ArrayList<Firewall>();
		list = this.listAll(datacenterId);
		// 项目列表非空并且长度大于0时
		if (list != null && list.size() > 0) {
			for (Firewall firewall : list) {
				if (firewall.getTenant_id().equals(projectId)) {
					temp.add(firewall);
				}
			}
		}

		return temp;
	}

	/**
	 * 获取指定id的防火墙
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public Firewall getById(String datacenterId, String projectId, String id)
			throws AppException {
		Firewall object = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);

		JSONObject json = restService.getById(restTokenBean,
				OpenstackUriConstant.FIREWALL_URI + "/",
				OpenstackUriConstant.FIREWALL_DATA_NAME, id);
		if (json != null) {
			// 转换成java对象
			object = restService.json2bean(json, Firewall.class);
			// 名称特殊的key，调用initData方法做数据初始化
			initData(object, json);
		}
		return object;
	}

	/**
	 * 创建云主机方法
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param request
	 *            json字符串，包含待创建的云主机的具体配置信息
	 * @return
	 * @throws AppException
	 */
	public Firewall create(String datacenterId, String projectId,
			JSONObject data) throws AppException {
		Firewall object = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.FIREWALL_URI);
		JSONObject result = restService.create(restTokenBean,
				OpenstackUriConstant.FIREWALL_DATA_NAME, data);
		object = restService.json2bean(result, Firewall.class);

		return object;
	}

	/**
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
		restTokenBean.setUrl(OpenstackUriConstant.FIREWALL_URI + "/" + id);
		return restService.delete(restTokenBean);
	}

	/**
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 *            修改的配置信息
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public Firewall update(String datacenterId, String projectId,
			JSONObject data, String id) throws AppException {
		Firewall object = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.FIREWALL_URI + "/" + id);

		JSONObject result = restService.update(restTokenBean,
				OpenstackUriConstant.FIREWALL_DATA_NAME, data);
		object = restService.json2bean(result, Firewall.class);
	
		return object;
	}
	
	public JSONObject get(String dcId,String imageId)throws Exception{
		RestTokenBean restTokenBean = getRestTokenBean(dcId,OpenstackUriConstant.NETWORK_SERVICE_URI);
		return restService.getJSONById(restTokenBean, OpenstackUriConstant.FIREWALL_URI+ "/", imageId);
	}
	
	/**                                                                                                         
	 * 获取底层数据中心下的防火墙                                                      
	 * -----------------                                                                                      
	 * @author zhouhaitao                                                                                     
	 * @param dataCenter                                                                                      
	 *                                                                                                        
	 * @return                                                                                                
	 *                                                                                                        
	 */                                                                                                       
	public List<BaseCloudFireWall> getStackList(BaseDcDataCenter dataCenter) {                                  
		List <BaseCloudFireWall> list = new ArrayList<BaseCloudFireWall>();                                         
		RestTokenBean restTokenBean = getRestTokenBean(dataCenter.getId(),                                              
				OpenstackUriConstant.NETWORK_SERVICE_URI);                                                          
		restTokenBean.setUrl(OpenstackUriConstant.FIREWALL_URI);                                           
		List<JSONObject> result = restService.list(restTokenBean,OpenstackUriConstant.FIREWALL_DATA_NAMES);
                                                                                                            
		if (result != null && result.size() > 0) {                                                              
			for (JSONObject jsonObject : result) {                                                                
				Firewall data = restService.json2bean(jsonObject,                                             
						Firewall.class);                                                                            
				 BaseCloudFireWall ccn=new BaseCloudFireWall(data,dataCenter.getId());                                 
					list.add(ccn);                                                                                    
				}                                                                                                   
			}                                                                                                     
		return list;                                                                                            
	}                                                                                                         
}

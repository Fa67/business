package com.eayun.eayunstack.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.Network;
import com.eayun.virtualization.model.BaseCloudNetwork;

public interface OpenstackNetworkService extends OpenstackBaseService<Network> {

	/**                                                        
	 * 查询底层 数据中心的云资源                             
	 * -------------------                                   
	 * @author zhouhaitao                                    
	 * @param dataCenter                                     
	 * @return                                               
	 */                                                      
	public List<BaseCloudNetwork> getStackList (BaseDcDataCenter dataCenter);
	
	/**
	 * 查询网络下的端口
	 * @param dataCenter
	 * @param netId
	 * @return
	 */
	public List<JSONObject> getPortByNet(BaseDcDataCenter dataCenter,String netId,String deviceOwner);

}
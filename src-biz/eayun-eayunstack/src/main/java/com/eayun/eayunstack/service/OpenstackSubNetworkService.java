package com.eayun.eayunstack.service;

import java.util.List;

import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.SubNetwork;
import com.eayun.virtualization.model.BaseCloudSubNetWork;

public interface OpenstackSubNetworkService extends
		OpenstackBaseService<SubNetwork> {

	/**                                                        
	 * 查询底层 数据中心的云资源                             
	 * -------------------                                   
	 * @author zhouhaitao                                    
	 * @param dataCenter                                     
	 * @return                                               
	 */                                                      
	public List<BaseCloudSubNetWork> getStackList (BaseDcDataCenter dataCenter);
	
	public SubNetwork create(String datacenterId, String projectId, net.sf.json.JSONObject data)
            throws AppException;

}
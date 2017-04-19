package com.eayun.eayunstack.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.Pool;
import com.eayun.virtualization.model.BaseCloudLdPool;

public interface OpenstackPoolService extends OpenstackBaseService<Pool> {
	
	public boolean bind(String datacenterId, String projectId, String id,
			String healthId) throws AppException;
	
	public JSONObject get (String dcId,String fwId) throws Exception;
	
	/**                                                        
	 * 查询底层 数据中心的云资源                             
	 * -------------------                                   
	 * @author zhouhaitao                                    
	 * @param dataCenter                                     
	 * @return                                               
	 */                                                      
	public List<BaseCloudLdPool> getStackList (BaseDcDataCenter dataCenter);

}
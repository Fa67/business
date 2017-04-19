package com.eayun.eayunstack.service;

import java.util.List;
import java.util.Map;

import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.SecurityGroup;

public interface OpenstackSecurityGroupService extends
		OpenstackBaseService<SecurityGroup> {
	
	/**                                                        
	 * 查询底层 数据中心的云资源                             
	 * -------------------                                   
	 * @author zhouhaitao                                    
	 * @param dataCenter                                     
	 * @return                                               
	 */                                                      
	@SuppressWarnings("rawtypes")
	public Map<String,List> getStackList (BaseDcDataCenter dataCenter);
}

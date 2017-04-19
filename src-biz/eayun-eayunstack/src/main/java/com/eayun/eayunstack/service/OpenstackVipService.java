package com.eayun.eayunstack.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.VIP;
import com.eayun.virtualization.model.BaseCloudLdVip;

public interface OpenstackVipService extends OpenstackBaseService<VIP> {
	public JSONObject get (String dcId,String fwId) throws Exception;
	
	/**                                                        
	 * 查询底层 数据中心的云资源                             
	 * -------------------                                   
	 * @author zhouhaitao                                    
	 * @param dataCenter                                     
	 * @return                                               
	 */                                                      
	public List<BaseCloudLdVip> getStackList (BaseDcDataCenter dataCenter);

}

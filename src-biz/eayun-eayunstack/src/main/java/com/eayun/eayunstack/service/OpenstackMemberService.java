package com.eayun.eayunstack.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.Member;
import com.eayun.virtualization.model.BaseCloudLdMember;

public interface OpenstackMemberService extends OpenstackBaseService<Member> {
	public JSONObject get (String dcId,String fwId) throws Exception;
	
	/**                                                        
	 * 查询底层 数据中心的云资源                             
	 * -------------------                                   
	 * @author zhouhaitao                                    
	 * @param dataCenter                                     
	 * @return                                               
	 */                                                      
	public List<BaseCloudLdMember> getStackList (BaseDcDataCenter dataCenter);

}

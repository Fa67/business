package com.eayun.virtualization.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestBody;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.eayunstack.model.Rule;
import com.eayun.virtualization.model.CloudVm;

/**
 * SecurityGroupRuleService
 *                       
 * @Filename: SecurityGroupRuleService.java
 * @Description: 
 * @Version: 1.0
 * @Author: liyanchao
 * @Email: yanchao.li@eayun.com
 * @History:<br>
 *<li>Date: 2015年10月28日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public interface SecurityGroupRuleService {
	/**
	 * 创建安全组规则
	 * @param request
	 * @return 返回指定安全组规则实体
	 */
	@SuppressWarnings("rawtypes")
    public Rule createRule(HttpServletRequest request,String dcId,String prjId,String sgId,@RequestBody Map map);
	// 删除规则
	public boolean deleteGroupRule(String datacenterId, String id);
	
	
	/**
	 * 查询安全组云主机
	 * @param sgrId
	 * */
	public Page querySecurityGroupCloudHostList(String sgid,QueryMap qm)  throws AppException;
	/**
	 * 查询符合条件的云主机
	 * @param sgrId
	 * @param prjid
	 * */
	@SuppressWarnings("rawtypes")
    public  List getaddSecurityGroupCloudHostList(String sgid,String prjid,String sgname,String cusorg)  throws AppException;
	
	/**
	 *添加安全组云主机
	 * */
	public void securityGroupsAddCloudHost(List<CloudVm>cloudvm,String sgId ,String sgname) throws AppException;
	/**
	 * 移除云主机
	 * */
	public void securityGroupsRemoveCloudHost(CloudVm cloudcm,String sgId,String sgname)throws AppException;
	
	
	
	
}

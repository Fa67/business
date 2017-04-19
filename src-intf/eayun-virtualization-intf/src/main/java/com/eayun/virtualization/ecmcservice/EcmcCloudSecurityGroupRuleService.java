package com.eayun.virtualization.ecmcservice;

import java.util.List;
import java.util.Map;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.eayunstack.model.Rule;
import com.eayun.eayunstack.model.SecurityGroup;
import com.eayun.virtualization.model.BaseCloudSecurityGroup;
import com.eayun.virtualization.model.BaseCloudSecurityGroupRule;
import com.eayun.virtualization.model.CloudVm;

public interface EcmcCloudSecurityGroupRuleService {
	
	/**
	 * 添加安全组规则
	 * @author zengbo
	 * @param baseCloudSecurityGroupRule
	 * @return
	 */
	public BaseCloudSecurityGroupRule addSecurityGroupRule(BaseCloudSecurityGroupRule baseCloudSecurityGroupRule);
	
	/**
	 * 根据安全组删除规则
	 * @param sgId
	 */
	public void deleteBySgId(String sgId);
	
	/**
	 * 根据安全组id删除规则
	 * @param datacenterId
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public boolean deleteSecurityGroupRule(String datacenterId, String id) throws AppException;
	/**
	 * 根据数据中心ID，项目ID，查询其所有的安全组
	 * @param datacenterId
	 * @param projectId
	 * @return
	 * @throws AppException
	 */
	public List<SecurityGroup> findStackSecurityGroupsListByProjectId(String datacenterId,String projectId) throws AppException;
	/**
	 * 根据数据中心ID，项目ID，查询其所有的安全组
	 * @param datacenterId
	 * @param projectId
	 * @return
	 * @throws AppException
	 */
	public List<BaseCloudSecurityGroup> getCloudSecurityGroupsByProjectId(String datacenterId, String projectId) throws AppException;
	/**
	 * 创建安全组规则且保存到本地库
	 * @param params
	 * @return
	 * @throws AppException
	 */
	public Rule createRule(Map<String, String> params) throws AppException;
	/**
	 * 查询安全组规则
	 * @param sgrId
	 * @return BaseCloudSecurityGroupRule
	 */
	public BaseCloudSecurityGroupRule getBaseGroupRuleBySgrId(String sgrId);
	
	
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

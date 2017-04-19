package com.eayun.virtualization.ecmcservice;

import java.util.List;
import java.util.Map;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.eayunstack.model.FirewallPolicy;
import com.eayun.virtualization.model.BaseCloudFwPolicy;
import com.eayun.virtualization.model.BaseCloudFwRule;
import com.eayun.virtualization.model.CloudFwPolicy;

/**
 * @author jingang.liu@eayun.com to beijing
 * @date 2016年4月12日
 */
public interface EcmcCloudFireWallPoliyService {

	/**
	 * 用于校验策略名称是否已经存在
	 * @param fwpName
	 * @return
	 * @throws AppException
	 */
	public boolean checkFwPolicyName(String fwpName,String projectId,String datacenterId,String fwpId) throws AppException;
	/**
	 * 分页查询防火墙策略的信息
	 * @param page
	 * @param projectId
	 * @param name
	 * @param querymap
	 * @return
	 * @throws AppException
	 */
	public Page list(Page page,String projectId,String name,String cusName,String datacenterId,QueryMap querymap) throws AppException;
	/**
	 * 查询防火墙策略的名字和ID，用于下啦列表
	 * @param prjId
	 * @return
	 * @throws AppException
	 */
	@SuppressWarnings("rawtypes")
    public List queryIdandName(String prjId) throws AppException;
	/**
	 * 获取指定Id的防火墙策略
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public BaseCloudFwPolicy getById(String id) throws AppException;
	
	/**
	 * 创建防火墙策略
	 * @param parmes
	 * @return
	 * @throws AppException
	 */
	public FirewallPolicy create(Map<String, String> parmes) throws AppException;
	/**
	 * 获取已选择的规则列表
	 * @param fwpId
	 * @return
	 * @throws AppException
	 */
	public List<BaseCloudFwRule> getByFwrId(String fwpId) throws AppException;
	
	/**
	 * 获取未被策略选择的防火墙规则
	 * @param projectId
	 * @return
	 * @throws AppException
	 */
	public List<BaseCloudFwRule> getByFwrIdList(String projectId) throws AppException;
	
	/**
	 * 修改防火墙策略
	 * @param parmes
	 * @return
	 * @throws AppException
	 */
	public FirewallPolicy update(Map<String, String> parmes) throws AppException;
	
	/**
	 * 删除防火墙策略
	 * @param swpId
	 * @return
	 * @throws AppException
	 */
	public boolean deletePolicy(String datacenterId,String projectId,String swpId) throws AppException;
	
	/**
	 * 编辑防火墙策略规则
	 * @param fwp
	 * @return
	 * @throws AppException
	 */
	public boolean toDoFwRule(CloudFwPolicy fwp) throws AppException;
	public List<CloudFwPolicy> getFwpListByPrjId(String dcId, String prjId)throws AppException;
	/**
     * 编辑页面专用
     * 
     * */
	public List<CloudFwPolicy> getPolicyListByDcIdPrjId(String dcId, String prjId)throws AppException; 
	
	/**
	 * 调整规则优先级
	 * 修改规则顺序
	 * @param fwp    策略
	 * @param local  位置（前置，后置）  
	 * @param target 目标规则ID
	 * @param reference 参照物
	 * @return
	 * @throws AppException
	 */
	public boolean updateRuleSequence(CloudFwPolicy fwpId,String local,String target,String reference) throws AppException;
	
	/**
	 * 解绑防火墙策略规则
	 * 删除策略下所有规则（删除部分规则调用：toDoFwRule）
	 * @param fwp
	 * @return
	 * @throws AppException
	 */
	public boolean releaseFwRule(CloudFwPolicy fwp) throws AppException;
}

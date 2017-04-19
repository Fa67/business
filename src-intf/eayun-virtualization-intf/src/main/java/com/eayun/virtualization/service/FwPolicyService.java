package com.eayun.virtualization.service;

import java.util.List;
import java.util.Map;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.eayunstack.model.FirewallPolicy;
import com.eayun.virtualization.model.BaseCloudFwPolicy;
import com.eayun.virtualization.model.BaseCloudFwRule;
import com.eayun.virtualization.model.CloudFwPolicy;


public interface FwPolicyService {

	public List<CloudFwPolicy> getFwpListByPrjId(String dcId, String prjId)throws AppException;

	public Page getFwpList(Page page, String prjId, String dcId,
			String fwpName, QueryMap queryMap)throws AppException;

	public boolean deleteFwp(CloudFwPolicy fwp)throws AppException;

	public BaseCloudFwPolicy addFwPolicy(String dcId, String prjId,String createName, String fwpName)throws AppException;

	public boolean updateFwPolicy(CloudFwPolicy fwp)throws AppException;

	public boolean toDoFwRule(CloudFwPolicy fwp)throws AppException;

	@SuppressWarnings("rawtypes")
    public boolean getFwpByName(Map map)throws AppException;
	/**
	 * 根据策略ID获取规则集合
	 * @param fwpId
	 * @return
	 * @throws AppException
	 */
	public List<BaseCloudFwRule> getRuleByFwpId(String fwpId) throws AppException;
	/**
	 * 创建带规则的策略
	 * @param dcId
	 * @param prjId
	 * @param createName
	 * @param fwpName
	 * @param rules
	 * @return
	 * @throws AppException
	 */
	public BaseCloudFwPolicy addFwPolicyRule(String dcId, String prjId,String createName, String fwpName,String rules)throws AppException;

	/**
	 * 调整防火墙优先级
	 * @param fwp
	 * @param local 位置（前置，后置）
	 * @param target 目标规则
	 * @param reference 参照物
	 * @return
	 * @throws AppException
	 */
	public boolean updateRuleSequence(BaseCloudFwPolicy fwp,String local,String target,String reference) throws AppException;

	/**
	 * 解绑规则
	 * @param fwp
	 * @return
	 * @throws AppException
	 */
	public boolean releaseFwRule(CloudFwPolicy fwp) throws AppException;
	/**
	 * 查询单个策略
	 * @param fwpId
	 * @return
	 * @throws AppException
	 */
	public BaseCloudFwPolicy getFwpById(String fwpId) throws AppException;
	/**
	 * 添加策略本地不入库
	 * @param dcId
	 * @param prjId
	 * @param fwpName
	 * @return
	 * @throws AppException
	 */
	public FirewallPolicy addFwPolicyRule(String dcId, String prjId, String fwpName,String rule)throws AppException;
	
}
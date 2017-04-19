package com.eayun.virtualization.service;

import java.util.List;
import java.util.Map;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.eayunstack.model.FirewallRule;
import com.eayun.virtualization.model.BaseCloudFwRule;
import com.eayun.virtualization.model.CloudFwPolicy;
import com.eayun.virtualization.model.CloudFwRule;



public interface FireWallRuleService {

	public Page getFireWallRuleList(Page page, String prjId, String dcId,String fireRuleName, QueryMap queryMap,String fwpId)throws AppException;

	public List<CloudFwRule> getFwRulesByPrjId(String dcId, String prjId)throws AppException;

	public List<CloudFwRule> getFwRulesByfwpId(CloudFwPolicy fwp)throws AppException;

	public boolean deleteFwRule(CloudFwRule fwr)throws AppException;

	@SuppressWarnings("rawtypes")
    public BaseCloudFwRule addFwRule(String createName, Map map)throws AppException;

	@SuppressWarnings("rawtypes")
    public boolean getFwRuleByName(Map map)throws AppException;

	public boolean updateFwRule(CloudFwRule fwr)throws AppException;
	
	public List<BaseCloudFwRule> getFwRulesByDcId(String dcId);
	
	public boolean updateCloudFwRuleFromStack(BaseCloudFwRule cloudFwRule);

	/**
	 * 禁用启用规则
	 * @param cloudFwRule
	 * @return
	 * @throws AppException
	 */
	public boolean updateIsEnabled(BaseCloudFwRule cloudFwRule)throws AppException;
	/**
	 * 删除规则并解绑策略
	 * @param fwr
	 * @return
	 * @throws AppException
	 */
	public boolean deleteFwRuletoPolicy(CloudFwRule fwr) throws AppException;
	/**
	 * 添加规则不入库
	 * @param map
	 * @return
	 * @throws AppException
	 */
	@SuppressWarnings("rawtypes")
    public FirewallRule addFwRule(Map map)throws AppException;

}

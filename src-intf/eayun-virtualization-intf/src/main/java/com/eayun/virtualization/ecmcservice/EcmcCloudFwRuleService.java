package com.eayun.virtualization.ecmcservice;

import java.util.List;
import java.util.Map;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.eayunstack.model.FirewallRule;
import com.eayun.virtualization.model.BaseCloudFwRule;
import com.eayun.virtualization.model.CloudFwRule;

/**
 * @author jingang.liu@eayun.com to beijing
 * @date 2016年4月12日
 */
public interface EcmcCloudFwRuleService {

	/**
	 * 防火墙规则页面的展示
	 * @param page
	 * @param projectId
	 * @param name
	 * @param querymap
	 * @return
	 * @throws AppException
	 */
	public Page list(Page page ,String datacenterId,String projectId,String name,String cusName,QueryMap querymap,String fwpId) throws AppException;
	/**
	 * 检查防火墙规则是否重名
	 * @param datacenterId 数据中心
	 * @param projectId 项目
	 * @param name 名称
	 * @return 已存在返回true，否则返回false
	 * @throws AppException
	 */
	public boolean checkFwRuleName(String datacenterId,String projectId,String fwrName,String fwrId) throws AppException;
	
	/**
	 * 查询所有防火墙规则的信息，以下拉列表形式在页面展示
	 * @param projectId
	 * @return
	 * @throws AppException
	 */
	public List<BaseCloudFwRule> listForPolicy(String projectId,String datacenterId) throws AppException;
	
	/**
	 * 查询指定id的防火墙规则的信息
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public BaseCloudFwRule getById(String id) throws AppException;
	
	/**
	 * 创建防火墙规则
	 * @param cfr
	 * @throws AppException
	 */
	public FirewallRule createCloudFwRule(Map<String, String> parmes) throws AppException;
	
	/**
	 * 修改防火墙规则
	 * @param cfr
	 * @throws AppException
	 */
	public FirewallRule updateCloudFwRule(Map<String, String> parmes) throws AppException;
	
	/**
	 * 删除指定id的防火墙规则
	 * @param datacenterId
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public boolean delete(String datacenterId,String projectId, String id) throws AppException;
	/**
	 * 删除规则并解绑策略
	 * @param datacenterId
	 * @param projectId
	 * @param id
	 * @param fwpId
	 * @return
	 * @throws AppException
	 */
	public boolean deleteFwRuletoPolicy(String datacenterId,String projectId, String id,String fwpId) throws AppException;
	/**
	 * 根据数据中心和项目查询防火墙规则
	 * @param dcId
	 * @param prjId
	 * @return
	 * @throws AppException
	 */
	public List<CloudFwRule> getFwRulesByPrjId(String dcId, String prjId) throws AppException;
	/**
	 * 修改禁用启用
	 * @param parmes
	 * @return
	 * @throws AppException
	 */
	public FirewallRule isEnabled(Map<String, String> parmes) throws AppException;
	/**
	 * 创建防火墙规则并绑定到指定策略上
	 * @param parmes
	 * @return
	 * @throws AppException
	 */
	public FirewallRule createFwRuleToPoliy(Map<String, String> parmes) throws AppException;
}

package com.eayun.virtualization.ecmcservice;

import java.util.List;
import java.util.Map;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.virtualization.model.BaseCloudSecurityGroup;
import com.eayun.virtualization.model.BaseCloudVmSgroup;
import com.eayun.virtualization.model.CloudSecurityGroup;

public interface EcmcCloudSecurityGroupService {
	
	/**
	 * 添加安全组
	 * @author zengbo
	 * @param baseCloudSecurityGroup
	 * @return
	 */
	public BaseCloudSecurityGroup updateToDB(BaseCloudSecurityGroup baseCloudSecurityGroup);
	/**
	 * 校验安全组名是否重复
	 * @param dcId
	 * @param prjId
	 * @param sgName
	 * @param sgId
	 * @return
	 * @throws AppException
	 */
	public boolean checkSecurityGroupName(String dcId, String prjId, String sgName, String sgId) throws AppException;
	
	/**
	 * 添加安全组
	 * @param dcId
	 * @param prjId
	 * @param name
	 * @param description
	 * @return
	 * @throws AppException
	 */
	public BaseCloudSecurityGroup addSecurityGroup(String dcId, String prjId, String name, String description, String ceateName) throws AppException;
	
	/**
	 * 根据ID查询安全组使用量
	 * @author zengbo
	 * @param prjId
	 * @return
	 */
	public int getCountByPrjId(String prjId);
	
	/**
	 * 根据项目ID查询安全组
	 * @param prjId
	 * @return
	 */
	public List<BaseCloudSecurityGroup> getByPrjId(String prjId);
	
	/**
	 * 分页查询安全组
	 * @param datacenterId
	 * @param prjName
	 * @param cusName
	 * @param name
	 * @param queryMap
	 * @return
	 * @throws AppException
	 */
	public Page getSecurityGroupList(String datacenterId,String prjName, String cusOrg,String name, QueryMap queryMap) throws AppException;
	
	/**
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @return
	 * @throws AppException
	 */
	public List<CloudSecurityGroup> listAllGroups(String datacenterId, String projectId) throws AppException;
	
	/**
	 * 删除安全组
	 * @param datacenterId
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public boolean deleteSecurityGroup(String datacenterId, String id) throws AppException;
	
	/**
	 * 修改安全组
	 * @param cloudSecurityGroup
	 * @return
	 * @throws AppException
	 */
	public boolean updateSecurityGroup(CloudSecurityGroup cloudSecurityGroup) throws AppException;
	/**
	 * 根据安全组ID查找本地安全组
	 * @param sgId
	 * @return
	 * @throws AppException
	 */
	public Map<String, Object> getBaseCloudSecurityGroupById(String sgId) throws AppException;
	/**
	 * 根据安全组id查寻其所有本地库的规则
	 * @param datacenterId
	 * @param projectId
	 * @param groupId
	 * @param queryMap
	 * @return
	 * @throws AppException
	 */
	public Page getSecurityGroupRulesBySgId(String datacenterId,String projectId,String groupId, QueryMap queryMap) throws AppException;
	/**
	 * 根据sgId获取BaseCloudSecurityGroup
	 * ***/
	public BaseCloudSecurityGroup getGroupBySgId(String sgId);
	
	
	/**
	 * 根据prjid 获取当前安全组内正在创建的云主机
	 * */
	
	public List<BaseCloudVmSgroup> getVmByPrjId(String sgid);
	
	
	
	/**
	 * 修改线上默认安全组
	 * */
	public Object updateEcscSecurityGroup();
	
}

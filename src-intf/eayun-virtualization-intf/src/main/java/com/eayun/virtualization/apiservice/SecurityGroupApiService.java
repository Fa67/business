package com.eayun.virtualization.apiservice;

import java.util.List;

import com.eayun.common.exception.AppException;
import com.eayun.virtualization.model.BaseCloudVmSgroup;
import com.eayun.virtualization.model.CloudSecurityGroup;
import com.eayun.virtualization.model.CloudSecurityGroupRule;
import com.eayun.virtualization.model.CloudVm;

public interface SecurityGroupApiService {
	/**
	 * 云主机加入安全组
	 * @param cloudvm
	 * @param cloudSecurityGroup
	 * @throws AppException
	 */
	public void instanceJoinSecurityGroup(CloudVm cloudVm, CloudSecurityGroup cloudSecurityGroup) throws AppException;
	
	/**
	 * 云主机离开安全组
	 * @param cloudvm
	 * @param cloudSecurityGroup
	 * @throws AppException
	 */
	public void instanceLeaveSecurityGroup(CloudVm cloudVm, CloudSecurityGroup cloudSecurityGroup) throws AppException;
	
	/**
	 * 根据云主机ID和安全组ID获取云主机和安全组关系实体
	 * @param instaceId
	 * @param securityGroupId
	 * @return -- 当vmId为空时，查询条件只有securityGroupId
	 * 		   -- 当securityGroupId为空时，查询条件只有vmId
	 * 		   -- 当vmId和securityGroupId都为空时，返回null对象
	 * 		   -- 当vmId和securityGroupId都不为空时，查询条件有securityGroupId和vmId
	 * @throws AppException
	 */
	public List<BaseCloudVmSgroup> getVmSgroupByVmIdAndSecurityGropId(String vmId, String securityGroupId) throws AppException;

	/**
	 * 根据安全组ID获取安全组信息 -- 用于判断该安全组是否属于某客户
	 * @param securityGroupId
	 * @return
	 */
	public CloudSecurityGroup getSecurityGroupBySgId(String securityGroupId) throws AppException;
	/**
	 * 根据条件查询安全组列表
	 * @param securityGroupIds -- 安全组IDs,中间用","隔开
	 * @param searchWord -- 安全组名称，模糊查询
	 * @param offset -- 偏移量
	 * @param limit  -- 数据限制
	 * @return -- 如果没有符合条件的结果，则返回null对象
	 * @throws AppException
	 */
	public List<CloudSecurityGroup> getGroupList(String dcId, String cusId, String [] securityGroupId, String searchWord, 
					String offset, String limit) throws AppException;
	
	/**
	 * 根据安全组ID获取安全组的规则
	 * @param securityGroupRuleId
	 * @return -- 如果没有符合条件的结果，则返回null对象
	 * @throws AppException
	 */
	public List<CloudSecurityGroupRule> getGroupRuleListByGroupId(String securityGroupRuleId) throws AppException;
	
	/**
	 * 查询项目下默认安全组类型的安全组<br>
	 * -------------------------------------
	 * 
	 * @author zhouhaitao
	 * @param prjId				项目ID
	 * @param defaultType		默认类型
	 * @return
	 */
	public String querySecurityGroupByDefaultAndPrjId(String prjId,String defaultType);
}

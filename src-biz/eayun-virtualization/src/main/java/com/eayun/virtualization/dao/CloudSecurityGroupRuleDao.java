package com.eayun.virtualization.dao;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.virtualization.model.BaseCloudSecurityGroupRule;
import com.eayun.virtualization.model.BaseCloudVmSgroup;

public interface CloudSecurityGroupRuleDao extends
		IRepository<BaseCloudSecurityGroupRule, String> {
	
	/**
	 * 根据安全组删除规则组
	 * @author zengbo
	 * @param sgId
	 */
	@Modifying
	@Query("delete from BaseCloudSecurityGroupRule where sgId = ?")
	public void deleteBySgId(String sgId);
	/**
	 * 查询安全组规则
	 * @author liyanchao
	 * @param sgId
	 */
	@Query(" from BaseCloudSecurityGroupRule where sgrId = ?")
	public BaseCloudSecurityGroupRule getBaseGroupRuleBySgrId(String sgrId);
	
	@Modifying
	@Query("delete from BaseCloudVmSgroup where vmId = ? and sgId=?")
	public void deletedvmsgroup(String vmid,String sgid);
	
	@Query("from BaseCloudVmSgroup where vmId = ? and sgId=?")
	public BaseCloudVmSgroup queryvmsgroup(String vmid,String sgid);
	
	@Modifying
	@Query("delete from BaseCloudSecurityGroupRule where remoteGroupId=?")
	public void deletedsgrule(String sgid);
	

}

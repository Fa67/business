package com.eayun.virtualization.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.common.exception.AppException;
import com.eayun.virtualization.model.BaseCloudFwPolicy;
import com.eayun.virtualization.model.BaseCloudFwRule;

public interface CloudFwPolicyDao extends IRepository<BaseCloudFwPolicy, String> {
	//@Query("from BaseCloudFwPolicy t where dcId=:dcId and prjId=:prjId")
	//public List<BaseCloudFwPolicy> getFwpListByPrjId(@Param("dcId") String dcId, @Param("prjId") String prjId);
	
	/**
	 * 根据名称查询
	 * @param name
	 * @return
	 */
	@Query(" from BaseCloudFwPolicy bcfp where bcfp.fwpName = ? ")
    public List<BaseCloudFwPolicy> checkFwPolicyName(String name);
	/**
	 * 根据项目ID查询id和name,用于下拉列表
	 * @param prjId
	 * @return
	 */
	@SuppressWarnings("rawtypes")
    @Query("select new map(fwp.fwpId as fwpID, fwp.fwpName as fwpName) from BaseCloudFwPolicy fwp where fwp.fwId is null and fwp.prjId=?")
    public List queryIdandName(String prjId);
	/**
	 * 获取已选择的规则列表
	 * @param fwpId
	 * @return
	 * @throws AppException
	 */
	@Query(" from BaseCloudFwRule as fwr where fwr.fwpId = ? order by fwr.priority")
	public List<BaseCloudFwRule> getByFwrId(String fwpId) throws AppException;
	
	/**
	 * 获取未被策略选择的防火墙规则
	 * @param projectId
	 * @return
	 * @throws AppException
	 */
	@Query(" from BaseCloudFwRule as fwr where fwr.fwpId is null and fwr.prjId = ? ")
	public List<BaseCloudFwRule> getByprojectId(String projectId) throws AppException;
	
	/**
	 * 删除防火墙策略
	 * @param swpId
	 * @return
	 * @throws AppException
	 */
	@Query(nativeQuery=true,value = " update cloud_fwrule fr set fr.fwp_id = null where fr.fwp_id = ? ")
	public int deletePolicy(String swpId) throws AppException;
	
}

package com.eayun.virtualization.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eayun.common.dao.IRepository;
import com.eayun.virtualization.model.BaseCloudFwRule;

public interface CloudFwRuleDao extends IRepository<BaseCloudFwRule, String> {
	
	@Query("from BaseCloudFwRule t where dcId=:dcId and prjId=:prjId and fwpId is null")
	public List<BaseCloudFwRule> getFwRulesByPrjId(@Param("dcId") String dcId, @Param("prjId") String prjId);
	
	@Query("from BaseCloudFwRule t where dcId=:dcId and prjId=:prjId and fwpId=:fwpId")
	public List<BaseCloudFwRule> getFwRulesByfwpId(@Param("dcId") String dcId, @Param("prjId") String prjId, @Param("fwpId") String fwpId);
	
	@Query("update BaseCloudFwRule fwr set fwr.fwpId = null where fwr.fwpId = ? ")
	public int removePolicy(String policyId);
   
}

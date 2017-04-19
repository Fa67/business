package com.eayun.virtualization.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.virtualization.model.BaseCloudLdMember;

public interface CloudLdMemberDao extends IRepository<BaseCloudLdMember, String> {
	@Query("select count(*) from BaseCloudLdMember t where t.prjId= ? ")
	public int getCountByPrjId(String prjId);

	public void deleteByPoolId(String poolId);
	
	@Query("select count(m.memberId) from BaseCloudLdMember m"
			+ " where m.memberAddress in(?1) and m.protocolPort = ?2"
			+ " and (?3 = null or ?3 = '' or m.memberId <> ?3)")
	public int countMemberPort(List<String> addresses, Long protocolPort, String memberId);
	
	public int countByPoolId(String poolId);
}

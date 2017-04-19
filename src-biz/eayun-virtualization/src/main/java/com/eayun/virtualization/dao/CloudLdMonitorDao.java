package com.eayun.virtualization.dao;

import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.virtualization.model.BaseCloudLdMonitor;

public interface CloudLdMonitorDao  extends IRepository<BaseCloudLdMonitor, String> {
	 @Query("select count(*) from BaseCloudLdMonitor t where t.prjId= ? ")
	 public int getCountByPrjId(String prjId);
	 
	 @Query("select count(t.ldmId) from BaseCloudLdMonitor t where t.prjId = ?1 and binary(t.ldmName) = ?2 and (?3 = null or ?3 = '' or t.ldmId <> ?3)")
	 public int countMultMonitorName(String prjId, String ldmName, String ldmId);
}

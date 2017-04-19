package com.eayun.database.instance.dao;

import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.database.instance.model.BaseCloudRDSInstance;

public interface CloudRDSInstanceDao extends IRepository<BaseCloudRDSInstance, String> {

	 @Query("select count(*) from BaseCloudRDSInstance where prjId = ? and isVisible = '1' and isDeleted = '0' and isMaster = '1'")
	 public int getCountByPrjId(String prjId);
	 
	 @Query("select count(*) from BaseCloudRDSInstance where masterId = ? and isVisible = '1' and isDeleted = '0' and isMaster = '0'")
	 public int getSlaveCountByMasterId(String masterId);
	 
	 @Query("select count(*) from BaseCloudRDSInstance where prj_id = ? and isVisible = '1' and isDeleted = '0' and isMaster = '0'")
	 public int getSlaveCountByPrjId(String prjId);
	 
	 
}

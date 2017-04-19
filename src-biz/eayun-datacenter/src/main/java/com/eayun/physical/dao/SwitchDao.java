package com.eayun.physical.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.physical.model.BaseDcSwitch;

public interface SwitchDao extends IRepository<BaseDcDataCenter, String>{
	
	@Modifying
	@Query("delete from BaseDcSwitch where id=?")
	void delete(String id);
	
	@Query("from BaseDcSwitch where id=?")
	BaseDcSwitch queryById(String id);

	@Query("from BaseDcSwitch where name=? and dataCenterId=?")
	List<BaseDcSwitch> checkNameExist(String name,String dcid);
	
	@Query("from BaseDcSwitch where name=? and dataCenterId=? and id<>?")
	List<BaseDcSwitch> checkNameExist(String name,String dcid,String id);
	
	
	/**
	 * 2016-04-012
	 * */
	@Query(" select count(*) from BaseDcSwitch where dataCenterId=?")
	int getcountswitch(String id);
}

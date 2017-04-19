package com.eayun.physical.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.physical.model.BaseDcStorage;

public interface DcStorageDao extends IRepository<BaseDcDataCenter, String>{

	@Query("from BaseDcStorage  where name=? and dataCenterId=?")
	List<BaseDcStorage> checkNameExist(String name,String dcid);
	
	
	@Query("from BaseDcStorage  where name=? and id!=? and  dataCenterId=?")
	List<BaseDcStorage> checkNameExistOfEdit(String name,String id,String dcid);
	
	
	
	/**
	 * 2016-04-12
	 * */
	@Query(" select count(*) from BaseDcStorage where dataCenterId=?")
	int getcountstorage(String id);
	
	@Query("from BaseDcStorage where name= ?")
	List<BaseDcStorage> queryname(String name);
	
	@Query("from BaseDcStorage where 1=1 and id = ?")
	BaseDcStorage getstoragebyid(String id);
	
	
	
	@Modifying
	@Query("delete from BaseDcStorage where id=?")
	void deletestorage(String id);
	
}

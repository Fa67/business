package com.eayun.physical.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.physical.model.BaseDcFirewall;

public interface FirewallDao  extends IRepository<BaseDcDataCenter, String>{
	
	@Query("from BaseDcFirewall where id=?")
	BaseDcFirewall queryById(String id);
	
	@Query("from BaseDcFirewall where name=? and dataCenterId=?")
	List<BaseDcFirewall> queryById(String name,String id);
	
	@Query("from BaseDcFirewall where name=? and dataCenterId=? and id<>?")
	List<BaseDcFirewall> queryById(String name,String datacenterid,String id);
	
	
	
	/**
	 * 2016-04-12
	 * */
	@Query(" select count(*) from BaseDcFirewall where dataCenterId=?")
	int getcountfirewall(String id);
	
	
	

}



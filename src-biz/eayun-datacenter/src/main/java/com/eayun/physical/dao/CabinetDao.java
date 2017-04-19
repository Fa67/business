package com.eayun.physical.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eayun.common.dao.IRepository;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.physical.model.BaseDcCabinet;

public interface CabinetDao  extends IRepository<BaseDcDataCenter, String>{
	
	@SuppressWarnings("rawtypes")
    @Query("from BaseDcCabinet where name in (:name) and dataCenterId= :datacenterId")
	public List<BaseDcCabinet> checkName(@Param("name") List list,@Param("datacenterId")String datacenterid);
	
	@Query("from BaseDcCabinet where name=? and dataCenterId=? and id<>?")
	public List<BaseDcCabinet> checkName(String name,String datacenterid,String id);
	
	@Query("from BaseDcCabinet where dataCenterId=?")
	public BaseDcCabinet checkName(String datacenterid);
	
	
	@Query("from BaseDcCabinet where dataCenterId=?")
	public List<BaseDcCabinet> getCountDateCenterById(String datacenterid);
	
	 @Query(" from BaseDcCabinet t where dataCenterId=? and id=? ")
	    public List<BaseDcDataCenter> getcountbyid(String id,String cabinetid);
	
	 @Query("select name from BaseDcCabinet where id=?")
	    public String getcabinetName(String id);
	 
	 /**
	  * 2016-04-12
	  * 
	  * */
	 @Query("select count(*) from BaseDcCabinet where dataCenterId=?")
	  public int getcountcabinet(String id);
	 
	 @Modifying
	 @Query("delete from BaseDcCabinet where id=?")
	 void deletecabinet(String id);
	 
	 @Query("from BaseDcCabinet b where id=? ")
	 BaseDcCabinet getcabinetByid(String cabinetid);
	 
	 @Modifying
	 @Query("update BaseDcCabinet set totalCapacity=?,data_center_id=?,name=? where id=?")
	 void updatecabinet(int totalCapacity,String datacenterid,String cabinetName,String id);
}

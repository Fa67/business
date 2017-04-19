package com.eayun.physical.dao;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.datacenter.model.BaseDcDataCenter;

public interface DcCabinetRfDao  extends IRepository<BaseDcDataCenter, String>{
	
	@Query("select count(*)  from BaseDcCabinetRf where data_center_id= ? and cabinetId= ? and flag='1'")
		int getdccabinetrf(String datacenterID,String cabinetId);
	
	@Modifying
	@Query("delete from BaseDcCabinetRf where cabinetId= ? and data_center_id= ? ")
	void deletecabinetrf(String cabinetID,String dcid);
	
	@Query("select data_center_id  from BaseDcCabinetRf where cabinetId=?  and flag ='1'")
	String[] updatacabinetORcelectcabinetrf(String cabinetid);
	
	@Query("select count(*)  from  BaseDcCabinetRf where cabinetId=? and data_center_id=?")
	int updatacabinetORcelectcabinetrf(String cabinetid ,String dcid);

	
	@Modifying
	@Query("delete from BaseDcCabinetRf where cabinetId=? and data_center_id=? and flag='0' and location>?")
	void deletecabinetrflocation(String cabinet,String dcid,int location);
	
	
	
	@Modifying
	@Query("update BaseDcCabinetRf set flag='0',reId='',reType='' where  reId=?")
	void updatefirewallORcabinetrf(String reid);

	@Modifying
	@Query("update BaseDcCabinetRf set flag='1',reId= ? ,reType='1' where cabinetId= ? and data_center_id= ? and location= ?")
	void updatefirewallORcabinetrf(String reid,String cabinetid,String dcid,int location);

	
	@Modifying
	@Query("update BaseDcCabinetRf set flag='1',reId=?,reType='2' where cabinetId=? and data_center_id=? and location=?")
	void updatesotageORcabinetrf(String reid,String cabinetid,String dcid,int location);
	
	@Modifying
	@Query("update BaseDcCabinetRf set flag='1',reId=?,reType='3' where cabinetId=? and data_center_id=? and location=?")
	void updateswitchORcabinetrf(String reid,String cabinetid,String dcid,int location);
}





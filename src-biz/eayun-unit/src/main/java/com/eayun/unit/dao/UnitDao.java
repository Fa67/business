package com.eayun.unit.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.unit.model.BaseUnitInfo;


public interface UnitDao extends IRepository<BaseUnitInfo, String>{
	@Query("from BaseUnitInfo where cusId=?")
	public List<BaseUnitInfo> getUnitInfoList(String cusid);
	
	@Query("from BaseUnitInfo where unitId=?")
	public BaseUnitInfo getUnitInfoByid(String unitId);
	
	@Modifying
	@Query("update BaseUnitInfo set recordNo=? where  unitId=? ")
	public void updatedetail(String no,String id);
	
	


}

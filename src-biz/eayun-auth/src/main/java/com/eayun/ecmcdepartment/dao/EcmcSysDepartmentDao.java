package com.eayun.ecmcdepartment.dao;


import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.ecmcdepartment.model.BaseEcmcSysDepartment;

public interface EcmcSysDepartmentDao extends IRepository<BaseEcmcSysDepartment, String> {
	
	@Query("from BaseEcmcSysDepartment where name=?")
	public BaseEcmcSysDepartment findDepartmentByName(String departmentName);
	
	@Query("select new BaseEcmcSysDepartment(id, name, description, parentId)"
	        + " from BaseEcmcSysDepartment"
	        + " where enableflag = '1' order by createTime desc")
	public List<BaseEcmcSysDepartment> findAllBaseInfo();
	
	@Query("from BaseEcmcSysDepartment d order by d.createTime desc")
	public List<BaseEcmcSysDepartment> findAllOrderByCreateTimeDesc();
	
	@Query("from BaseEcmcSysDepartment where parentId = ?")
	public List<BaseEcmcSysDepartment> findByParentId(String parentId);
	
	@Query("select count(d.code) from BaseEcmcSysDepartment d where binary(d.code) = ?1 and (?2 = null or ?2 = '' or d.id <> ?2)")
	public int countDepartCode(String code, String id);
	
}

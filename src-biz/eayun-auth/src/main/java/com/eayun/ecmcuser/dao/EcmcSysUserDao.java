package com.eayun.ecmcuser.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eayun.common.dao.IRepository;
import com.eayun.ecmcuser.model.BaseEcmcSysUser;

public interface EcmcSysUserDao extends IRepository<BaseEcmcSysUser, String> {

	@Query("from BaseEcmcSysUser where account=:userAccount and (:id = null or id != :id)")
	public BaseEcmcSysUser findUserByAccount(@Param("userAccount")String userAccount, @Param("id")String id);
	
	@Query("from BaseEcmcSysUser where name=?")
	public BaseEcmcSysUser findUserByName(String userName);
	
	@Query("from BaseEcmcSysUser where account=?")
	public BaseEcmcSysUser findUserByAccount(String userAccount);
	
	@Query("from BaseEcmcSysUser where departId=?")
	public List<BaseEcmcSysUser> findUserByDepartmentId(String departmentId);
	
}

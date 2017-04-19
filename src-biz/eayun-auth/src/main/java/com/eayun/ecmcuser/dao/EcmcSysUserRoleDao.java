package com.eayun.ecmcuser.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.ecmcuser.model.BaseEcmcSysUserRole;

public interface EcmcSysUserRoleDao extends IRepository<BaseEcmcSysUserRole, String> {

	@Modifying
	@Query("delete from BaseEcmcSysUserRole where userId=?")
	public void delUserRoleByUserId(String userId);
	
	@Query("from BaseEcmcSysUserRole where userId=?")
	public Iterable<BaseEcmcSysUserRole> findUserRoleByUserId(String userId);
	
	@Query("from BaseEcmcSysUserRole where roleId=?")
	public List<BaseEcmcSysUserRole> findUserRoleByRoleId(String roleId);
	
	@Modifying
	public void deleteByRoleId(String roleId);
	
	public int countByUserId(String userId);
	
	public int countByRoleId(String roleId);
	
	@Query("select ur.roleId from BaseEcmcSysUserRole ur where ur.userId = ?")
	public List<String> findRoleIdsByUserId(String userId);
}

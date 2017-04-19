package com.eayun.ecmcauthority.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eayun.common.dao.IRepository;
import com.eayun.ecmcauthority.model.BaseEcmcSysRoleAuth;

/**
* @Author fangjun.yang
* @Date 2016年3月1日
*/
public interface EcmcSysRoleAuthDao extends IRepository<BaseEcmcSysRoleAuth, String>{
	
	@Query(" from BaseEcmcSysRoleAuth where roleId = ? ")
	public List<BaseEcmcSysRoleAuth> findByRoleId(String roleId);
	
	@Modifying
	@Query("delete from BaseEcmcSysRoleAuth where roleId = ? ")
	public int deleteSysRoleAuthByRoleId(String roleId);
	
	@Modifying
	@Query("delete from BaseEcmcSysRoleAuth where authId = ? ")
	public int deleteSysRoleAuthByAuthId(String authId);
	
	@Query(" select authId from BaseEcmcSysRoleAuth where roleId = ? ")
	public List<String> findAuthIdsByRoleId(String roleId);
	
	@Query(" select distinct authId from BaseEcmcSysRoleAuth where roleId in (:roleIds) ")
	public List<String> findAuthIdsByRoleIds(@Param("roleIds")List<String> roleIds);
	
	@Query("select count(a.id) from BaseEcmcSysRoleAuth a where a.roleId in(:roleIds)")
    public int countAuthByRoleIds(@Param("roleIds")List<String> roleIds);
}


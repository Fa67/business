package com.eayun.ecmcrole.dao;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eayun.common.dao.IRepository;
import com.eayun.ecmcrole.model.BaseEcmcSysRole;

public interface EcmcSysRoleDao extends IRepository<BaseEcmcSysRole, String> {
	
	@Query("from BaseEcmcSysRole where name=?")
	public BaseEcmcSysRole  findRoleByName(String roleName);
	
	@Query("select new map(ur.userId as userId, r.id as id, r.name as name) from BaseEcmcSysRole as r, BaseEcmcSysUserRole as ur where r.id=ur.roleId and ur.userId in (:userIds)")
	public List<Map<String, Object>>  findRolesByUserIds(@Param("userIds") List<String> userIds);

	@Query("from BaseEcmcSysRole r order by r.createTime desc")
	public List<BaseEcmcSysRole> findAllEcmcSysRole();
	
	@Query("from BaseEcmcSysRole r where r.enableFlag = '1' and r.hide = '0' order by r.createTime desc")
	public List<BaseEcmcSysRole> findAllEnableEcmcSysRole();
}

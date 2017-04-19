package com.eayun.ecmcauthority.dao;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eayun.common.dao.IRepository;
import com.eayun.ecmcauthority.model.BaseEcmcSysAuthority;

/**
* @Author fangjun.yang
* @Date 2016年3月1日
*/
public interface EcmcSysAuthorityDao extends IRepository<BaseEcmcSysAuthority, String>{
	
	public List<BaseEcmcSysAuthority> findByIdIn(List<String> authIds);
	
	@Modifying
	@Query("update BaseEcmcSysAuthority auth set auth.menuId = NULL where auth.menuId = ? ")
	public int setAuthorityMenuIdEmpty(String menuId);
	
    @Query("select new map(a.id as id,a.name as name,a.description as description,a.createTime as createTime,a.enableFlag as enableFlag,a.permission as permission,a.menuId as menuId,m.name as menuName,a.lock as lock) from BaseEcmcSysAuthority a, BaseEcmcSysMenu m where a.menuId = m.id and (m.id = :menuId or :menuId = null or :menuId = '')")
	public List<Map<String, Object>> findByMenuId(@Param("menuId")String menuId);

	@Query("select distinct a from BaseEcmcSysAuthority as a, BaseEcmcSysRoleAuth as ra where ra.authId=a.id and ra.roleId in (:roleIds))")
	public List<BaseEcmcSysAuthority> findByRoleIds(@Param("roleIds")List<String> roleIds);

	
	@Query("select a from BaseEcmcSysAuthority a,BaseEcmcSysRoleAuth ra where a.id = ra.authId and ra.roleId = ?")
	public List<BaseEcmcSysAuthority> findByRoleId(String roleId);

	public int countByMenuId(String menuId);
	
	@Query("from BaseEcmcSysAuthority a where a.enableFlag = '1'")
	public List<BaseEcmcSysAuthority> findAllEnableAuthority();
}


package com.eayun.ecmcmenu.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eayun.common.dao.IRepository;
import com.eayun.ecmcmenu.model.BaseEcmcSysMenu;

/**
 * @Author fangjun.yang
 * @Date 2016年3月1日
 */
public interface EcmcSysMenuDao extends IRepository<BaseEcmcSysMenu, String> {

	@Query("select distinct m from BaseEcmcSysMenu as m, BaseEcmcSysRoleMenu as rm "
	        + " where m.id = rm.menuId and rm.roleId in (:roleIds))")
	public List<BaseEcmcSysMenu> findByRoleIds(@Param("roleIds") List<String> roleIds);
	
	@Query("select m from BaseEcmcSysMenu as m, BaseEcmcSysRoleMenu as rm where m.id = rm.menuId and rm.roleId = ?)")
	public List<BaseEcmcSysMenu> findByRoleId(String roleId);
	
	@Query("select m from BaseEcmcSysMenu m")
	public List<BaseEcmcSysMenu> findAllMenu();
	
	public int countByParentId(String parentId);

}

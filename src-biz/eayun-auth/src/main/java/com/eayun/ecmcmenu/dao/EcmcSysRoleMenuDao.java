package com.eayun.ecmcmenu.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.ecmcmenu.model.BaseEcmcSysRoleMenu;

/**
* @Author fangjun.yang
* @Date 2016年3月1日
*/
public interface EcmcSysRoleMenuDao extends IRepository<BaseEcmcSysRoleMenu, String>{
	
	public void deleteByMenuId(String menuId);
	
	@Modifying
	@Query("delete from BaseEcmcSysRoleMenu where roleId = ?")
	public void deleteByRoleId(String roleId);
	
	public List<BaseEcmcSysRoleMenu> findByRoleId(String roleId);
	
	
	
	
}


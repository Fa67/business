package com.eayun.ecmcmenu.service;

import java.util.List;

import com.eayun.common.exception.AppException;
import com.eayun.ecmcmenu.model.BaseEcmcSysMenu;
import com.eayun.ecmcmenu.model.BaseEcmcSysRoleMenu;
import com.eayun.ecmcmenu.model.EcmcSysMenuTreeGrid;

/**
* @Author:fangjun.yang
* @Date:2016年3月1日
*/
public interface EcmcSysMenuService {

	/**
	 * 添加菜单
	 * @param ecmcSysMenu
	 * @throws Exception
	 */
	public void addSysMenu(BaseEcmcSysMenu ecmcSysMenu) throws Exception;
	
	/**
	 * 查询所有菜单
	 * @return
	 * @throws Exception
	 */
	public List<BaseEcmcSysMenu> getAllSysMenuList() throws Exception;
	
	/**
	 * 删除菜单
	 * @param menuId
	 * @throws Exception
	 */
	public void deleteSysMenu(String menuId) throws Exception;
	
	/**
	 * 是否存在子菜单
	 * @param parentId
	 * @return
	 * @throws AppException
	 */
	public boolean hasSubMenu(String parentId) throws AppException;
	/**
	 * 包含权限
	 * @param menuId
	 * @return
	 * @throws AppException
	 */
	public boolean existsAuths(String menuId) throws AppException;
	
	/**
	 * 修改菜单
	 * @param ecmcSysMenu
	 * @throws Exception
	 */
	public void updateSysMenu(BaseEcmcSysMenu ecmcSysMenu) throws Exception;
	
	/**
	 * 获取单个菜单
	 * @param menuId
	 * @return
	 * @throws Exception
	 */
	public BaseEcmcSysMenu getSysMenuById(String menuId) throws Exception;
	
	/**
	 * 查询角色菜单信息
	 * @param roleId
	 * @return
	 * @throws Exception
	 */
	public List<BaseEcmcSysRoleMenu> findRoleMenuByRoleId(String roleId) throws Exception;
	
	/**
	 * 查询多角色菜单信息
	 * @param roleIds
	 * @return
	 * @throws Exception
	 */
	public List<BaseEcmcSysMenu> findRoleMenuByRoleIds(List<String> roleIds) throws Exception;
	
	/**
	 * 获取角色菜单树形列表，数据项根据父子关系和排序号 排序。嵌套结构
	 * @param roleIds
	 * @return
	 * @throws Exception
	 */
	public List<EcmcSysMenuTreeGrid> getMenuGridyByRoleIds(List<String> roleIds) throws Exception;
	
	/**
	 * 获取菜单树形列表，数据项根据父子关系和排序号 排序。嵌套结构
	 * @return
	 * @throws AppException
	 */
	public List<EcmcSysMenuTreeGrid> getEcmcSysMenuGridList() throws AppException;
	
	/**
	 * 查询单个菜单
	 * @param menuId
	 * @return
	 * @throws AppException
	 */
	public BaseEcmcSysMenu getMenuById(String menuId) throws AppException;
}


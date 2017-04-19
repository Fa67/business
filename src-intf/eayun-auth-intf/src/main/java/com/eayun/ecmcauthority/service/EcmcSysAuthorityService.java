package com.eayun.ecmcauthority.service;

import java.util.List;

import com.eayun.common.exception.AppException;
import com.eayun.ecmcauthority.model.BaseEcmcSysAuthority;
import com.eayun.ecmcauthority.model.EcmcSysAuthority;

/**
* @Author fangjun.yang
* @Date 2016年3月1日
*/
public interface EcmcSysAuthorityService {
	
	/**
	 * 添加权限
	 * @param auth
	 */
	public void addSysAuthority(BaseEcmcSysAuthority auth)  throws AppException;
	
	/**
	 * 查询角色拥有的权限
	 * @param roleId
	 * @return
	 * @throws Exception
	 */
	public List<BaseEcmcSysAuthority> getSysAutorityListByRoleId(String roleId) throws Exception;
	
	/**
	 * 查询角色集合拥有的权限
	 * @param roleId
	 * @return
	 * @throws Exception
	 */
	public List<BaseEcmcSysAuthority> getSysAuthorityListByRoleIds(List<String> roleIds) throws Exception;
	
	/**
	 * 删除权限
	 * @param authId
	 * @throws Exception
	 */
	public void deleteSysAuthority(String authId) throws AppException;
	
	/**
	 * 修改权限
	 * @param auth
	 * @throws Exception
	 */
	public void updateSysAuthority(BaseEcmcSysAuthority auth) throws AppException;
	
	/**
	 * 查询单个权限
	 * @param authId
	 * @return
	 * @throws Exception
	 */
	public BaseEcmcSysAuthority findSysAuthorityById(String authId) throws Exception;
	
	/**
	 * 查询某菜单下的权限列表,如果menuId为空，则查询所有权限
	 * @param menuId
	 * @return
	 * @throws Exception
	 */
	public List<EcmcSysAuthority> getSysAuthorityList(String menuId) throws AppException;
	
	/**
	 * 查询所有权限
	 * @return
	 * @throws AppException
	 */
	public List<EcmcSysAuthority> getAllEnableAuthList() throws AppException;
}


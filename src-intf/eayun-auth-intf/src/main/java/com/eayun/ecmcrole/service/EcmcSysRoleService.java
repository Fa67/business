package com.eayun.ecmcrole.service;

import java.util.List;

import com.eayun.common.exception.AppException;
import com.eayun.ecmcrole.model.EcmcSysRole;

public interface EcmcSysRoleService {

    /**
     * 添加角色信息
     * @param 角色对象
     * @return
     */
    public EcmcSysRole addRole(EcmcSysRole role) throws AppException;

    /**
     * 删除角色信息
     * @param 角色id
     * @return
     */
    public void delRole(String roleId) throws AppException;
    
    /**
     * 判断角色下是否存在用户
     * @param roleId
     * @return
     */
    public boolean hasUserByRoleId(String roleId);

    /**
     * 更新角色信息
     * @param 角色对象
     * @return
     */
    public boolean updateRole(EcmcSysRole role) throws AppException;

    /**
     * 验证角色名称
     * @param 角色名称
     * @return
     */
    public boolean checkRoleName(String roleName) throws AppException;

    /**
     * 根据ID查询角色
     * @param 角色ID
     * @return
     */
    public EcmcSysRole findRoleById(String roleId) throws AppException;

    /**
     * 根据所有角色
     * @param 
     * @return
     */
    public List<EcmcSysRole> findAllRole() throws AppException;

    /**
     * 查询角色下拉列表
     * @return
     * @throws AppException
     */
    public List<EcmcSysRole> findRoleSelectList() throws AppException;

    /**
     * 根据用户查询角色
     * @param 
     * @return
     */
    public List<EcmcSysRole> findRolesByUserId(String userId) throws AppException;
    /**
     * 验证角色名称是否重复
     * @param name
     * @param roleId
     * @return
     * @throws AppException
     */
    public boolean checkRoleName(String name, String roleId) throws AppException;

    /**
     * 保存/修改 角色菜单 和权限
     * @param sysRoleMenuList
     * @throws Exception
     */
    public void saveSysRoleMenuAndAuthority(String roleId, List<String> menuIds, List<String> authIds) throws Exception;
}

package com.eayun.ecmcuser.service;

import java.util.List;
import java.util.Map;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.ecmcuser.model.EcmcSysUser;

public interface EcmcSysUserService {

	/**
	 * 添加用户信息
	 * @param 用户对象
	 * @return
	 */
	public EcmcSysUser addUser(Map<String, Object> map) throws AppException;

	/**
	 * 删除用户信息
	 * @param 用户ID
	 * @return
	 */
	public void delUser(String userId) throws AppException;

	/**
	 * 更新用户信息
	 * @param 用户对象
	 * @return
	 */
	@SuppressWarnings("rawtypes")
    public EcmcSysUser updateUser(Map map) throws AppException;

	/**
	 * 验证用户名称
	 * @param 用户名称
	 * @return
	 */
	public boolean checkUserAccount(String userAccount, String id) throws AppException;

	/**
	 * 通过用户名查询用户
	 * @param 用户名称
	 * @return
	 */
	public EcmcSysUser findUserByName(String userName) throws AppException;

	/**
	 * 根据部门ID查询用户
	 * @param 部门ID
	 * @return
	 */
	public List<EcmcSysUser> findUserByDepartmentId(String departmentId) throws AppException;

	/**
	 * 根据ID查询用户
	 * @param 用户ID
	 * @return
	 */
	public EcmcSysUser findUserById(String userId) throws AppException;

	/**
	 * 查询部门所有用户
	 * @param 用户ID
	 * @return
	 */
	public Page findPageUserByDepartId(String departId, QueryMap  queryMap) throws AppException;
	
	/**
	 * 根据 权限许可 查询用户
	 * @param permission
	 * @return
	 */
	public List<EcmcSysUser> findUserByPermission(String userName, String permission);
	
	/**
	 * 角色下是否有用户
	 * @param 角色ID
	 * @return
	 */
	public boolean hasUserByRoleId(String roleId);
	
	/**
	 * 修改密码
	 * @param origPwd 加密的原始密码
	 * @param newPwd 加密的新密码
	 * @param passKey 加密验证串
	 * @param user 会话用户
	 */
	public EayunResponseJson changePass(String origPwd, String newPwd, String passKey, EcmcSysUser user);
	
	/**
	 * 登陆
	 * @param codenum 用户输入验证码
	 * @param correctNum 正确验证码
	 * @param userName 用户名
	 * @param userPasswd 用户密码
	 * @return
	 */
	public EayunResponseJson login(String codenum, String correctNum, String userAccount, String userPasswd, String passKey);

	/**
	 * 查询是否存在有负责的未完成工单
	 * @param workHeadUser
	 * @return
	 * @throws AppException
	 */
	public boolean hasUnfinishedWorkOrder(String workHeadUser) throws AppException;
	
	/**
	 * 查询ECMC系统下所有“管理员”角色的用户信息
	 * @return
	 */
	public List<EcmcSysUser> findAllAdminUsers() ;
	
}

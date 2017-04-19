package com.eayun.database.account.service;

import java.util.Date;
import java.util.List;

import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.database.account.model.CloudRDSAccount;
import com.eayun.database.database.model.CloudRDSDatabase;
import com.eayun.database.relation.model.CloudRDSRelation;

/**
 * 
 * @filename: RDSAccountService.java
 * @description:
 * @version: 1.0
 * @author: gaoxiang
 * @email: xiang.gao@eayun.com
 * @history: <br>
 * <li>Date: 2017年2月21日</li>
 * <li>Version: 1.0</li>
 * <li>Content: create</li>
 */
public interface RDSAccountService {
    /**
     * 获取数据库账户分页列表
     * @param page
     * @param paramsMap
     * @return
     * @throws Exception
     */
    public Page getAccountPageList (Page page, ParamsMap paramsMap) throws Exception;
    /**
     * 检验某实例下的账户名称是否重名
     * @param account
     * @return
     */
    public boolean checkAccountNameExist (CloudRDSAccount account) ;
    /**
     * 创建数据库账户
     * @param account
     * @return
     * @throws AppException
     */
    public CloudRDSAccount createAccount (CloudRDSAccount account) throws AppException;
    /**
     * 创建数据库账户上层业务
     * @param account
     * @param isFromStack
     * @param date
     * @return
     */
    public CloudRDSAccount createAccountInDB (CloudRDSAccount account, boolean isFromStack, Date date);
    /**
     * 创建数据库账户root
     * @param root
     * @return
     */
    public CloudRDSAccount createAccountRoot (CloudRDSAccount root) throws AppException;
    /**
     * 重置数据库账户root密码
     * @param root
     * @param isReset
     * @return
     * @throws AppException
     */
    public boolean resetRootPassword (CloudRDSAccount root, boolean isReset) throws AppException;
    /**
     * 删除数据库账户
     * @param account
     * @return
     * @throws AppException
     */
    public boolean deleteAccount (CloudRDSAccount account) throws AppException;
    /**
     * 删除数据库账户上层业务
     * @param account
     * @param isFromStack
     * @return
     */
    public boolean deleteAccountInDB (CloudRDSAccount account, boolean isFromStack);
    /**
     * 修改数据库账户权限
     * @param account
     * @param isFromStack
     * @return
     * @throws AppException
     */
    public boolean updateAccessAccount (CloudRDSAccount account, boolean isFromStack) throws AppException;
    /**
     * 修改数据库账户密码
     * @param account
     * @return
     * @throws AppException
     */
    public boolean updatePasswordAccount (CloudRDSAccount account) throws AppException;
    /**
     * 获取实例下的数据库名称列表
     * @param instanceId
     * @return
     */
    public List<CloudRDSDatabase> getAllDatabaseListByInstanceId (String instanceId) throws Exception;
    /**
     * 获取实例特定账户管理的数据库列表
     * @param accountId
     * @return
     */
    public List<CloudRDSRelation> getDatabaseListManagedByAccount (String accountId);
    /**
     * 提供给实例删除时调用，删除所在实例下的全部账户和关联关系
     * @param instanceId
     * @return
     */
    public void deleteAllAccountByInstanceId (String instanceId) throws AppException;
    /**
     * 通过主库创建从库，或者备份创建主库的时候，同步底层的账户数据到上层
     * @param dcId
     * @param prjId
     * @param instanceId
     * @throws Exception
     */
    public void synchronAccountCreate (String dcId, String prjId, String instanceId) throws Exception;
}
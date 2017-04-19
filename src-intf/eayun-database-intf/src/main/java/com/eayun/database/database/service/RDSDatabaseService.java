package com.eayun.database.database.service;

import java.util.List;

import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.database.database.model.CloudRDSDatabase;

/**
 * 
 * @filename: RDSDatabaseService.java
 * @description:
 * @version: 1.0
 * @author: gaoxiang
 * @email: xiang.gao@eayun.com
 * @history: <br>
 * <li>Date: 2017年2月21日</li>
 * <li>Version: 1.0</li>
 * <li>Content: create</li>
 */
public interface RDSDatabaseService {
    /**
     * 获取数据库数据分页列表
     * @param page
     * @param paramsMap
     * @return
     * @throws Exception
     */
    public Page getDatabasePageList (Page page, ParamsMap paramsMap) throws Exception;
    /**
     * 获取数据库数据列表
     * @param instanceId
     * @return
     * @throws Exception
     */
    public List<CloudRDSDatabase> getDatabaseList (String instanceId) throws Exception;
    /**
     * 检验某实例下数据库名称是否重名
     * @param database
     * @return
     */
    public boolean checkDBNameExist (CloudRDSDatabase database) throws Exception;
    /**
     * 创建数据库数据
     * @param database
     * @return
     * @throws AppException
     */
    public CloudRDSDatabase createDatabase (CloudRDSDatabase database) throws AppException;
    /**
     * 删除数据库数据
     * @param database
     * @return
     * @throws AppException
     */
    public boolean deleteDatabase (CloudRDSDatabase database) throws AppException;
    /**
     * 删除数据库的上层业务
     * @param databaseId
     * @return
     */
    public boolean delDatabaseInDB (String databaseId);
    /**
     * 通过id获取名字，主要为调用底层接口做准备
     * @param id
     * @return
     */
    public String getDBNameById (String id);
    /**
     * 通过instanceId和数据库名称，获取id，主要为底层同步做准备
     * @param instanceId
     * @param name
     * @return
     */
    public String getDBIdByName (String instanceId, String name);
    /**
     * 提供给删除实例时调用，删除所在实例下的所有数据库
     * @param instanceId
     * @throws Exception
     */
    public void deleteAllDatabaseByInstanceId (String instanceId) throws Exception;
    /**
     * 通过主库创建从库，或者备份创建主库的时候，同步底层的数据库数据到上层
     * @param dcId
     * @param prjId
     * @param instanceId
     * @throws Exception
     */
    public void synchronDBCreate (String dcId, String prjId, String instanceId) throws Exception;
}
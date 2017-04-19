package com.eayun.eayunstack.service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.database.configgroup.model.datastore.Datastores;
import com.eayun.eayunstack.model.Vm;

/**
 * Created by Administrator on 2017/2/20.
 */
public interface OpenstackConfigurationGroupService extends OpenstackBaseService<Object> {

    /**
     * 查询全部的配置组
     * @param dcId  数据中心ID
     * @param projectId 项目ID编号
     * @return
     */
    public JSONObject listConfigurationGroups(String dcId, String projectId) throws AppException ;

    /**
     * 查询全部的Datastore对象
     * @param dcId 数据中心ID
     * @param projectId 项目ID编号
     * @return
     * @throws AppException
     */
    public Datastores listDatastores(String dcId, String projectId) throws AppException;

    /**
     * 根据指定的GroupId编号查询对应的配置组的详细信息
     * @param dcId  数据中心ID
     * @param projectId 项目ID编号
     * @param groupId   配置组ID编号
     * @return
     */
    public JSONObject queryConfigurationGroup(String dcId, String projectId, String groupId) throws AppException ;

    /**
     * 根据指定的配置组ID编号，查询此配置组已经应用到的实例
     * @param dcId  数据中心ID
     * @param projectId 项目ID编号
     * @param groupId   配置组编号
     * @return
     */
    public JSONObject queryConfigurationGroupInstances(String dcId, String projectId, String groupId) throws AppException ;

    /**
     * 创建配置组对象
     * @param dcId  数据中心ID
     * @param projectId 项目ID编号
     * @param postContent 具体的请求参数
     * @return
     * @throws AppException
     */
    public JSONObject createConfigurationGroup(String dcId, String projectId, JSONObject dataStore, String fileName, JSONObject configParams) throws AppException ;

    /**
     * 删除指定配置组ID编号的配置组对象
     * @param dcId  数据中心ID
     * @param projectId 项目ID
     * @param groupId   配置组ID编号
     * @return
     * @throws AppException
     */
    public boolean deleteConfigurationGroup(String dcId, String projectId, String groupId) throws AppException ;

    /**
     * 更改指定配置组编号中的某些自定义配置参数信息
     * @param dcId  数据中心
     * @param projectId 项目ID
     * @param groupId   配置组ID编号
     * @param postContent   提交的自定义配置参数以及对应值组成的请求内容
     * @return
     * @throws Exception
     */
    public JSONObject updateConfigurationGroup(String dcId, String projectId, String groupId, JSONObject postContent) throws AppException ;

    /**
     * 为指定的数据库实例绑定一个指定的配置组对象
     * @param dcId  数据中心ID
     * @param projectId 项目ID编号
     * @param groupId   配置组编号
     * @param instanceId    实例ID编号
     * @return
     * @throws AppException
     */
    public JSONObject attachConfigurationGroupToInstance(String dcId, String projectId, String groupId, String instanceId) throws AppException ;

    /**
     * 解绑指定编号实例对象上所绑定的配置组
     * @param dcId
     * @param projectId
     * @param instanceId
     * @return
     * @throws AppException
     */
    public JSONObject detachConfigurationGroupToInstance(String dcId, String projectId, String instanceId) throws AppException ;
}

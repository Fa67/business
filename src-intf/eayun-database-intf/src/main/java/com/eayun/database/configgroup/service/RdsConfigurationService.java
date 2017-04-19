package com.eayun.database.configgroup.service;

/**
 * Created by Administrator on 2017/2/21.
 */

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.database.configgroup.model.configfile.CloudRdsconfigfile;

import java.util.List;

/**
 * Rds Configuration Manager Service
 */
public interface RdsConfigurationService {
    /**
     * 创建客户配置文件
     * @param cloudRdsconfigfile
     * @return
     * @throws Exception
     */
    public void createCusConfigFile(CloudRdsconfigfile cloudRdsconfigfile) throws Exception ;

    /**
     * 删除客户配置文件
     * @param configGroupId
     * @return
     * @throws Exception
     */
    public void deleteCusConfigFile(String configGroupId) throws Exception ;

    /**
     * 更新客户配置文件
     * @param configGroupId
     * @param editParams
     * @return
     * @throws Exception
     */
    public void updateCusConfigFile(String configGroupId, String editParams) throws Exception ;

    /**
     * 根据配置文件ID查看配置参数详情，返回JSON格式数据
     * @param configGroupId
     * @return
     * @throws Exception
     */
    public EayunResponseJson queryConfigFile(String configGroupId) throws Exception ;

    /**
     * 管理控制台分页查询所有的配置文件
     * @param page
     * @param paramsMap
     * @return
     * @throws Exception
     */
    public Page ecscListConfigFile(Page page, ParamsMap paramsMap) throws Exception ;

    /**
     * 根据数据库版本获取JSON格式Datastore信息
     * @param version
     * @return
     * @throws Exception
     */
    public JSONObject getConfigurationGroupDatastoreByVersion(String version) throws Exception ;

    /**
     * 根据指定的配置组查询当前项目下所应用到的所有实例
     * @param dcId
     * @param projectId
     * @param groupId
     * @return
     *
     * {
        "instances": [
                {
                    "id": "23f504c3-c194-4691-bce4-da077b22a3c1",
                    "name": "ZhouHaiTao"
                },
                ...
            ]
        }
     *
     * @throws Exception
     */
    public JSONArray queryConfigurationGroupInstances(String dcId, String projectId, String groupId)throws Exception ;

    /**
     * 根据项目ID以及数据库版本查询配置文件信息
     * @param projectId 项目ID
     * @param version   数据库版本
     * @return
     * @throws Exception
     */
    public EayunResponseJson queryConfigFileByPrjAndVersion(String projectId, String version) ;

    /**
     * 根据具体的数据库版本名称查询默认的配置参数数据信息
     * @param versionName
     * @return
     * @throws Exception
     */
    public String queryDefaultParamValueByVersionName(String versionName) throws Exception ;


    public CloudRdsconfigfile find(String id) ;

    /**
     * 将最原始的参数内容字符串，做属性的精简，转换为创建配置组时真正能够使用的JSON格式
     * @param origin
     * @return
     * @throws Exception
     */
    public JSONObject changeStoreParamValueToOpenStackConfigurationValue(String origin) throws Exception ;

    /**
     * 查询获取当前系统中存在的数据库版本
     * @return
     * @throws Exception
     */
    public JSONArray getAllDatabaseVersion() throws Exception ;

    /**
     * 前台创建配置组时候，需要根据版本选择指定的依赖配置文件
     * @return
     * @throws Exception
     */
    public List<CloudRdsconfigfile> queryConfigFileForPage(String dcId, String projectId, String versionId) throws Exception ;

    /**
     * 根据具体配置组的ID编号查询对应具体的配置参数信息
     * @param groupId
     * @return
     * @throws Exception
     */
    public JSONArray queryConfigParamsByGroupId(String groupId) throws Exception ;

    /**
     * 验证在同一个数据中心、同一个项目之下是否存在同名文件
     * @param dcId
     * @param projectId
     * @param newFileName
     * @return
     * @throws Exception
     */
    public EayunResponseJson queryCusSelfConfigFileByFilename(String dcId, String projectId, String newFileName) throws Exception ;

    /**
     * 根绝配置组的名称查找对应绑定了该配置组的所有实例信息
     * @param configId
     * @return
     * @throws Exception
     */
    public JSONArray queryInstanceNamesByConfig(String configId) throws Exception ;

    /**
     * 因为当更改默认配置文件参数的时候，在界面上需要显示旧的实例对象信息，故这边需要查询完整的对象信息
     * @param version
     * @param projectId
     * @return
     * @throws Exception
     */
    public JSONArray queryInstanceNamesByDefaultConfig(String version, String projectId) throws Exception ;

    /**
     * 查询所有状态为非“Active”的实例列表信息
     * @param configId
     * @return
     * @throws Exception
     */
    public String queryNotActiveInstanceNamesByConfig(String configId) throws Exception ;

    /**
     * 判断指定的配置文件是否存在
     * @param configId
     * @return
     * @throws Exception
     */
    public boolean existConfigFile(String configId)throws Exception;
}
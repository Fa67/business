package com.eayun.database.configgroup.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.StringUtil;
import com.eayun.customer.ecmcservice.EcmcCustomerService;
import com.eayun.database.configgroup.dao.DatastoreVersionDao;
import com.eayun.database.configgroup.dao.RdsconfigfileDao;
import com.eayun.database.configgroup.dao.RdsconfigparamsDao;
import com.eayun.database.configgroup.dao.RdsdefaultconfigparamsDao;
import com.eayun.database.configgroup.model.configfile.*;
import com.eayun.database.configgroup.model.configgroup.ConfigurationGroup;
import com.eayun.database.configgroup.service.RdsConfigurationService;
import com.eayun.database.instance.model.BaseCloudRDSInstance;
import com.eayun.database.instance.model.CloudRDSInstance;
import com.eayun.database.instance.service.RDSInstanceService;
import com.eayun.datacenter.service.DataCenterService;
import com.eayun.eayunstack.model.RDSInstance;
import com.eayun.eayunstack.service.OpenstackConfigurationGroupService;
import com.eayun.project.ecmcservice.EcmcProjectService;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/**
 * Created by Administrator on 2017/2/28.
 */
@Service
@Transactional
public class BaseRdsConfigurationService {
    @Autowired
    private RdsconfigfileDao    rdsconfigfileDao ;
    @Autowired
    private RdsconfigparamsDao  rdsconfigparamsDao ;
    @Autowired
    private DatastoreVersionDao datastoreVersionDao ;
    @Autowired
    private RdsdefaultconfigparamsDao rdsdefaultconfigparamsDao ;
    @Autowired
    private DataCenterService   dataCenterService ;
    @Autowired
    private OpenstackConfigurationGroupService groupService ;
    @Autowired
    private EcmcProjectService  ecmcProjectService ;
    @Autowired
    private EcmcCustomerService ecmcCustomerService ;
    @Autowired
    private RDSInstanceService rdsInstanceService ;

    private static final String CONFIGURATION_TAG = "configuration" ;
    private static final String INSTANCES_TAG     = "instances" ;
    private static final String DB_VERSION = "version" ;
    private static final String DB_NAME = "type" ;
    private static final String TAG_NODENAMEEN = "nodeNameEn" ;
    private static final String TAG_NODENAME = "nodeName" ;
    private static Logger logger = LoggerFactory.getLogger(BaseRdsConfigurationService.class) ;

    /**
     * 判断当前指定的配置文件是否存在
     * @param configId
     * @return
     * @throws Exception
     */
    public boolean existConfigFile(String configId)throws Exception{
        if (rdsconfigfileDao.findOne(configId) == null){
            return false ;
        }else {
            return true ;
        }
    }
    /**
     * 创建客户配置文件
     * @param cloudRdsconfigfile
     * @throws Exception
     */
    public void createCusConfigFile(CloudRdsconfigfile cloudRdsconfigfile) throws Exception {
        String dependentParams = rdsconfigparamsDao.findOne(cloudRdsconfigfile.getConfigId()).getConfigContent();
        JSONObject datastore = getConfigurationGroupDatastoreByVersion(cloudRdsconfigfile.getConfigVersion()) ;
        ConfigurationGroup group = JSONObject.parseObject(groupService.createConfigurationGroup(cloudRdsconfigfile.getConfigDatacenterid(), cloudRdsconfigfile.getConfigProjectid(), datastore, cloudRdsconfigfile.getConfigName(), changeStoreParamValueToOpenStackConfigurationValue(dependentParams)).getJSONObject(CONFIGURATION_TAG).toJSONString(), ConfigurationGroup.class);
        cloudRdsconfigfile.setConfigId(group.getId());
        cloudRdsconfigfile.setConfigType("2");
        cloudRdsconfigfile.setConfigVersionname(datastore.getString(DB_NAME) + datastore.getString(DB_VERSION));
        rdsconfigfileDao.save(cloudRdsconfigfile);
        CloudRdsconfigparams cloudRdsconfigparams = new CloudRdsconfigparams();
        cloudRdsconfigparams.setConfigId(group.getId());
        cloudRdsconfigparams.setConfigContent(dependentParams);
        rdsconfigparamsDao.save(cloudRdsconfigparams);
    }

    public CloudRdsconfigfile find(String id){
        return rdsconfigfileDao.findOne(id) ;
    }

    /**
     * 删除客户配置文件
     * @param configGroupId
     * @throws Exception
     */
    
    public void deleteCusConfigFile(String configGroupId) throws Exception {
        CloudRdsconfigfile rdsconfigfile = rdsconfigfileDao.findOne(configGroupId);
        groupService.deleteConfigurationGroup(rdsconfigfile.getConfigDatacenterid(), rdsconfigfile.getConfigProjectid(), configGroupId);
        rdsconfigfileDao.delete(rdsconfigfile);
        rdsconfigparamsDao.delete(configGroupId);
    }

    /**
     * 更新客户配置文件
     * @param configGroupId
     * @param editParams
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public void updateCusConfigFile(String configGroupId, String editParams) throws Exception{

        CloudRdsconfigfile rdsconfigfile = rdsconfigfileDao.findOne(configGroupId) ;
        Map<String,Object> rs = distinct(configGroupId, editParams);
        JSONObject needJson = (JSONObject) rs.get("lastJson");
        Boolean updateParamIsNeedRestart = (Boolean) rs.get("updateParamIsNeedRestart");
        groupService.updateConfigurationGroup(rdsconfigfile.getConfigDatacenterid(),
                rdsconfigfile.getConfigProjectid(),
                configGroupId,
                needJson);
        //配置文件更新的时候不更新创建时间
//        rdsconfigfile.setConfigDate(new Date());
        rdsconfigfileDao.saveOrUpdate(rdsconfigfile);
        CloudRdsconfigparams cloudRdsconfigparams = new CloudRdsconfigparams() ;
        cloudRdsconfigparams.setConfigId(configGroupId);
        cloudRdsconfigparams.setConfigContent(editParams);
//        rdsconfigparamsDao.saveOrUpdate(cloudRdsconfigparams);
        rdsconfigparamsDao.merge(cloudRdsconfigparams);
        if (updateParamIsNeedRestart){
            // TODO: 2017/4/5 修改了需要重启才能生效的配置项，需要重启对应的实例才能生效
//            json.put("rdsId", rds.getRdsId());
//            json.put("dcId",  rds.getDcId());
//            json.put("prjId", rds.getPrjId());
            //重启所有绑定指定配置文件的实例
            List<CloudRDSInstance> applyInstance = queryInstances(configGroupId) ;
            //将这些实例的状态全不能改为需重启
            rdsInstanceService.updateRdsInstanceStatus(applyInstance,"RESTART_REQUIRED");
            for (CloudRDSInstance cloudRDSInstance : applyInstance){
                try {
                    rdsInstanceService.restart(cloudRDSInstance);
                }catch (Exception e){
                    logger.error(e.getMessage(),e);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> distinct(String configGroupId, String editParams)throws Exception{
        Map<String, Object> operateResult = new HashMap<>() ;
        //配置参数是否需要重启生效的状态标识 （"restart" -> "1"）
        Boolean updateParamIsNeedRestart = false ;
        List<JSONObject> historyParams =
                JSONObject.parseObject(rdsconfigparamsDao.findOne(configGroupId).getConfigContent(),List.class) ;
        JSONObject historyParamsJson = new JSONObject();
        JSONObject isNeedRestartJson = new JSONObject();
        for (JSONObject p : historyParams){
            if ("int".equals(p.getString("type"))){
                historyParamsJson.put(p.getString("name"), Integer.parseInt(p.getString("currentParamValue")));
            }
            if ("string".equals(p.getString("type"))){
                historyParamsJson.put(p.getString("name"), p.getString("currentParamValue"));
            }
            isNeedRestartJson.put(p.getString("name"), p.getString("restart"));
        }
        //界面编辑过后的值
        JSONObject currentParams = changeStoreParamValueToOpenStackConfigurationValue(editParams) ;
        JSONObject lastJson = new JSONObject();
        for (String nameKey : currentParams.keySet()){
            if (!currentParams.getString(nameKey).equals(historyParamsJson.getString(nameKey))){
                lastJson.put(nameKey, currentParams.get(nameKey));
                if ("1".equals(isNeedRestartJson.getString(nameKey))){
                    //配置参数中有修改后需要重启才能生效的参数项
                    updateParamIsNeedRestart = true ;
                }
            }
        }
        lastJson.size();
        operateResult.put("lastJson",lastJson);
        operateResult.put("updateParamIsNeedRestart",updateParamIsNeedRestart);
        return operateResult ;
    }

    /**
     * 根据指定的配置组ID返回配置文件信息
     * @param configGroupId
     * @return
     * @throws Exception
     */
    
    public EayunResponseJson queryConfigFile(String configGroupId) throws Exception{
        EayunResponseJson eayunResponseJson = new EayunResponseJson() ;
        try {
            CloudRdsconfigparams cloudRdsconfigparams = rdsconfigparamsDao.findOne(configGroupId);
            eayunResponseJson.setRespCode(ConstantClazz.SUCCESS_CODE);
            eayunResponseJson.setData(cloudRdsconfigparams);
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            eayunResponseJson.setRespCode(ConstantClazz.WARNING_CODE);
        }
        return eayunResponseJson ;
    }

    /**
     * 根据版本号查询JSONObject格式的Datastore对象
     * @param version
     * @return
     * @throws Exception
     */
    
    public JSONObject getConfigurationGroupDatastoreByVersion(String version) throws Exception {
        String sql = " select d.name as type, v.name as version from cloud_datastore d, cloud_datastoreversion v where d.id = v.datastore_id and v.id = '" + version + "' " ;
        List datastores = datastoreVersionDao.createSQLQuery(sql).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
        if (datastores != null && datastores.size() != 0){
            Map data = (Map) datastores.get(0) ;
            JSONObject ds = new JSONObject() ;
            ds.put(DB_NAME,    data.get(DB_NAME)) ;
            ds.put(DB_VERSION, data.get(DB_VERSION)) ;
            return ds ;
        }
        return null;
    }

    /**
     * 查询配置组应用到的实例信息
     * @param dcId
     * @param projectId
     * @param groupId
     * @return
     * @throws Exception
     */
    
    public JSONArray queryConfigurationGroupInstances(String dcId, String projectId, String groupId) throws Exception {
        JSONObject requestResult = groupService.queryConfigurationGroupInstances(dcId, projectId, groupId) ;
        return requestResult.getJSONArray(INSTANCES_TAG);
    }
    
    public EayunResponseJson queryConfigFileByPrjAndVersion(String projectId, String version) {
        List<CloudRdsconfigfile> cloudRdsconfigfiles = null ;
        EayunResponseJson eayunResponseJson = new EayunResponseJson() ;
        try {
            cloudRdsconfigfiles = rdsconfigfileDao.queryConfigFileByPrjAndVersion(projectId, version) ;
            eayunResponseJson.setRespCode(ConstantClazz.SUCCESS_CODE);
            eayunResponseJson.setData(cloudRdsconfigfiles);
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            eayunResponseJson.setRespCode(ConstantClazz.WARNING_CODE);
        }
        return eayunResponseJson ;
    }
    /**
     * 返回配置参数的各项参数内容--JSON格式字符串
     * @param versionName
     * @return
     * @throws Exception
     */
    
    public String queryDefaultParamValueByVersionName(String versionName) throws Exception {
        List<CloudRdsdefaultconfigparams> cloudRdsdefaultconfigparamses =
                rdsdefaultconfigparamsDao.queryDefaultParamValueByVersionName(versionName) ;
        for (int i=0 ; i<cloudRdsdefaultconfigparamses.size() ; i++){
            CloudRdsdefaultconfigparams c = cloudRdsdefaultconfigparamses.get(i) ;
            Rdsdefaultconfigparams rdsdefaultconfigparams = new Rdsdefaultconfigparams() ;
            try {
                BeanUtils.copyProperties(rdsdefaultconfigparams, c);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            rdsdefaultconfigparams.setCurrentParamValue(c.getDefaultValue());
            cloudRdsdefaultconfigparamses.set(i, rdsdefaultconfigparams) ;
        }
        if (cloudRdsdefaultconfigparamses.size() == 0){
            return null ;
        }else {
            return JSONObject.toJSONString(cloudRdsdefaultconfigparamses);
        }
    }
    /**
     * 获取实际创建配置组时用到的配置参数对象
     * @param origin
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    
    public JSONObject changeStoreParamValueToOpenStackConfigurationValue(String origin) throws Exception {
        //先获取到所有的配置参数详情内容
        JSONArray allParams = JSONObject.parseObject(origin, JSONArray.class) ;
        JSONObject realParams = new JSONObject() ;
        for (int i=0 ; i<allParams.size() ; i++){
            JSONObject eachParam = allParams.getJSONObject(i) ;
            if ("int".equals(eachParam.getString("type"))){
                //如果对应参数类型为整型
                realParams.put(eachParam.getString("name"), Integer.parseInt(eachParam.getString("currentParamValue"))) ;
            }
            if ("string".equals(eachParam.getString("type"))){
                //如果对应参数类型为字符串类型
                realParams.put(eachParam.getString("name"), eachParam.getString("currentParamValue")) ;
            }
        }
        logger.info(realParams.toJSONString());
        return realParams ;
    }
    
    public JSONArray getAllDatabaseVersion() throws Exception{
        String dataSql = "select CONCAT(d.name,v.name) as nodeName, v.id as nodeNameEn from cloud_datastore d, cloud_datastoreversion v where d.id = v.datastore_id" ;
        List versions = datastoreVersionDao.createSQLQuery(dataSql).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
        JSONArray jsonArray = new JSONArray() ;
        if (versions != null && versions.size() != 0){
            for (int i=0 ; i<versions.size() ; i++){
                JSONObject jsonObject = new JSONObject() ;
                Map data = (Map) versions.get(i) ;
                jsonObject.put(TAG_NODENAMEEN, data.get(TAG_NODENAMEEN)) ;
                jsonObject.put(TAG_NODENAME,   data.get(TAG_NODENAME)) ;
                jsonArray.add(jsonObject) ;
            }
            return jsonArray ;
        }
        return null;
    }
    @SuppressWarnings("unchecked")
    
    public List<CloudRdsconfigfile> queryConfigFileForPage(String dcId, String projectId, String versionId) throws Exception {
        StringBuilder hql = new StringBuilder();
        List<String> values = new ArrayList<String>();
        hql.append(" from CloudRdsconfigfile c where (( c.configType = '1' and c.configDatacenterid = ? ) or ( 1=1 ");
        values.add(dcId);
        if (!StringUtil.isEmpty(projectId)) {
            hql.append(" and c.configProjectid = ? ");
            values.add(projectId);
        }
        hql.append(" and c.configType = '2' )) ");
        if ((!StringUtil.isEmpty(versionId)) && (!"0".equals(versionId))) {
            hql.append(" and c.configVersion = ? ");
            values.add(versionId);
        }
        return rdsconfigfileDao.createQuery(hql.toString(), values.toArray()).list() ;
    }

    
    public JSONArray queryConfigParamsByGroupId(String groupId) throws Exception {
        String paramString = rdsconfigparamsDao.findOne(groupId).getConfigContent() ;
        return JSONObject.parseObject(paramString, JSONArray.class) ;
    }

    
    public EayunResponseJson queryCusSelfConfigFileByFilename(String dcId, String projectId, String newFileName) throws Exception {
        EayunResponseJson eayunResponseJson = new EayunResponseJson() ;
        try {
            List<CloudRdsconfigfile> cloudRdsconfigfiles =
                    rdsconfigfileDao.queryCusSelfConfigFileByFilename(dcId, projectId, newFileName) ;
            if (cloudRdsconfigfiles != null && cloudRdsconfigfiles.size()>0){
                eayunResponseJson.setRespCode(ConstantClazz.SUCCESS_CODE);
            }else {
                eayunResponseJson.setRespCode(ConstantClazz.ERROR_CODE);
            }
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            eayunResponseJson.setRespCode(ConstantClazz.ERROR_CODE);
        }
        return eayunResponseJson;
    }

    @SuppressWarnings("unchecked")
    
    public JSONArray queryInstanceNamesByConfig(String configId) throws Exception {
        String hql = "from BaseCloudRDSInstance where isVisible = '1' and isDeleted = '0' and configId = ?" ;
        List<String> params = new ArrayList<>() ;
        params.add(configId) ;
        List<BaseCloudRDSInstance> instances = rdsconfigfileDao.createQuery(hql, params.toArray()).list() ;
        JSONArray all = new JSONArray();
        for (BaseCloudRDSInstance instance : instances) {
            JSONObject obj = new JSONObject();
            obj.put("id", instance.getRdsId());
            obj.put("name", instance.getRdsName());
            all.add(obj);
        }
        return all;
    }

    public List<CloudRDSInstance> queryInstances(String configId) throws Exception {
        String hql = "from BaseCloudRDSInstance where isVisible = '1' and isDeleted = '0' and configId = ?" ;
        List<String> params = new ArrayList<>() ;
        params.add(configId) ;
        List<BaseCloudRDSInstance> instances = rdsconfigfileDao.createQuery(hql, params.toArray()).list() ;
        List<CloudRDSInstance> cloudInstances = new ArrayList<>();
        for (BaseCloudRDSInstance instance : instances){
            CloudRDSInstance cloudRDSInstance = new CloudRDSInstance() ;
            BeanUtils.copyProperties(cloudRDSInstance, instance);
            cloudInstances.add(cloudRDSInstance);
        }
        return cloudInstances ;
    }

    /**
     * 查询所有状态为非“Active”的实例列表
     * @param configId
     * @return
     * @throws Exception
     */
    public String queryNotActiveInstanceNamesByConfig(String configId) throws Exception {
        String hql = "from BaseCloudRDSInstance where isVisible = '1' and isDeleted = '0' and rdsStatus !='ACTIVE' and configId = ?" ;
        List<String> params = new ArrayList<>() ;
        params.add(configId) ;
        List<BaseCloudRDSInstance> instances = rdsconfigfileDao.createQuery(hql, params.toArray()).list();
        if (instances == null || instances.size() == 0) {
            return null;
        }else {
            int count = 0 ;
            StringBuilder builder = new StringBuilder();
            for (BaseCloudRDSInstance instance : instances) {
                count ++ ;
                builder.append(instance.getRdsName());
                if (count>=5){
                    builder.append("......");
                    break;
                }else {
                    builder.append("、");
                }
            }
            if (count>=5){
                return builder.toString();
            }else {
                return builder.toString().substring(0, builder.toString().length()-1);
            }
        }
    }
    
    public JSONArray queryInstanceNamesByDefaultConfig(String version, String projectId) throws Exception {
        String dataSql = " select i.rds_id as id, i.rds_name as name " +
                " from cloud_rdsinstance i, cloud_rdsconfigfile f " +
                " where i.config_id = f.config_id and i.is_visible = '1' and i.is_deleted = '0' and f.config_type != '2' and f.config_version = '"+version+"' and i.prj_id = '"+projectId+"' " ;
        List instances = rdsconfigfileDao.createSQLQuery(dataSql).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
        JSONArray jsonArray = new JSONArray() ;
        if (instances != null && instances.size() != 0){
            for (int i=0 ; i<instances.size() ; i++){
                JSONObject jsonObject = new JSONObject() ;
                Map data = (Map) instances.get(i) ;
                jsonObject.put("id", data.get("id")) ;
                jsonObject.put("name",   data.get("name")) ;
                jsonArray.add(jsonObject) ;
            }
            return jsonArray ;
        }
        return null;
    }
}
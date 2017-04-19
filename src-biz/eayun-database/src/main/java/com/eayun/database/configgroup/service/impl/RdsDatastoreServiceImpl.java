package com.eayun.database.configgroup.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.util.BeanUtils;
import com.eayun.database.configgroup.dao.*;
import com.eayun.database.configgroup.model.configfile.CloudRdsconfigfile;
import com.eayun.database.configgroup.model.configfile.CloudRdsconfigparams;
import com.eayun.database.configgroup.model.configfile.CloudRdsdefaultconfigparams;
import com.eayun.database.configgroup.model.configgroup.ConfigurationGroup;
import com.eayun.database.configgroup.model.datastore.*;
import com.eayun.database.configgroup.service.RdsConfigurationService;
import com.eayun.database.configgroup.service.RdsDatastoreService;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.datacenter.model.DcDataCenter;
import com.eayun.datacenter.service.DataCenterService;
import com.eayun.eayunstack.service.OpenstackConfigurationGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/2/24.
 */

/**
 * Sync Datastore with Database Relations
 */
@Service
@Transactional
public class RdsDatastoreServiceImpl implements RdsDatastoreService {

    @Autowired
    private DataCenterService dataCenterService ;   //数据中心Service
    @Autowired
    private OpenstackConfigurationGroupService openstackConfigurationGroupService ;     //底层配置组管理Service
    @Autowired
    private DatastoreDao datastoreDao ;     //Datastore Service
    @Autowired
    private DatastoreVersionDao datastoreVersionDao ;   //Datastore Version Service
    @Autowired
    private RdsconfigfileDao rdsconfigfileDao ;     //配置文件管理DAO
    @Autowired
    private RdsconfigparamsDao rdsconfigparamsDao ;
    @Autowired
    private RdsdefaultconfigparamsDao rdsdefaultconfigparamsDao ;   //默认配置文件管理DAO
    @Autowired
    private RdsConfigurationService baseRdsConfigurationService ;   //配置文件相关业务Service
    private static Logger logger = LoggerFactory.getLogger(RdsDatastoreServiceImpl.class) ;
    private static final String DEFAULT_CONFIG_SUFFIX_NAME = "DefaultConfigFile" ;//默认配置文件
    private static final String TAG_CONFIGURATION = "configuration" ;
    private static final String DB_VERSION = "version" ;
    private static final String DB_NAME = "type" ;

    @Override
    public List<CloudRdsconfigfile> syncDatastoreDatas(BaseDcDataCenter dcDataCenter) throws Exception{
        // TODO: 2017/3/7 定义需要保存而收集的默认配置文件基本信息
        List<CloudRdsconfigfile> cloudRdsconfigfiles = new ArrayList<>();
        // TODO: 2017/3/7 查询出系统当前所包含的所有数据中心
        List<DcDataCenter> dcDataCenters = dataCenterService.getAllList() ;

        // TODO: 2017/3/7 查询出当前对应数据中心下所包含的Datastore信息
        Datastores datastores = openstackConfigurationGroupService.listDatastores(dcDataCenter.getId(), dcDataCenter.getOsAdminProjectId());

        //遍历所有的Datastore信息，分情况处理
        for (Datastore datastore : datastores.getDatastores()) {
            // TODO: 2017/3/7 当前版本RDS云数据库只支持MySQL类型数据库
            if ("mysql".equals(datastore.getName())) {
                CloudDatastore cloudDatastore = new CloudDatastore();
                try {
                    BeanUtils.copyProperties(cloudDatastore, datastore);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                // TODO: 2017/3/7 保存或者更新对应的Datastore对象信息
                cloudDatastore.setDcId(dcDataCenter.getId());
                datastoreDao.saveOrUpdate(cloudDatastore);

                // TODO: 2017/3/7 遍历处理所有的Datastore Version，当前只考虑MySql 5.5与5.6两个版本
                for (DatastoreVersion datastoreVersion : datastore.getVersions()) {
                    if ("5.5".equals(datastoreVersion.getName()) || "5.6".equals(datastoreVersion.getName())) {
                        CloudDatastoreVersion cloudDatastoreVersion = new CloudDatastoreVersion();
                        try {
                            BeanUtils.copyProperties(cloudDatastoreVersion, datastoreVersion);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                        cloudDatastoreVersion.setDatastoreId(datastore.getId());
                        // TODO: 2017/3/7 保存或者更新最新的Datastore Version对象信息
                        datastoreVersionDao.saveOrUpdate(cloudDatastoreVersion);

                        // TODO: 2017/3/7 实例化对应信息的默认数据库配置文件
                        CloudRdsconfigfile cloudRdsconfigfile = new CloudRdsconfigfile();
                        //设置配置文件的数据中心ID
                        cloudRdsconfigfile.setConfigDatacenterid(dcDataCenter.getId());
                        //设置配置文件的Datastore Version ID
                        cloudRdsconfigfile.setConfigVersion(datastoreVersion.getId());
                        //组装一个默认的配置文件名称
                        StringBuilder fileName = new StringBuilder() ;

                        String dataStoreName = null ;
                        if ("mysql".equals(datastore.getName())){
                            dataStoreName = "MySQL" ;
                        }else {
                            dataStoreName = datastore.getName() ;
                        }
                        fileName.append(dataStoreName);       //数据库名称
                        fileName.append(datastoreVersion.getName());//数据库版本

//                        fileName.append(" ");
                        fileName.append(DEFAULT_CONFIG_SUFFIX_NAME);
                        //设置配置文件的名称
                        cloudRdsconfigfile.setConfigName(fileName.toString());
                        cloudRdsconfigfiles.add(cloudRdsconfigfile);
                    }
                }
            }
        }
        return cloudRdsconfigfiles ;
    }


    /**
     * 底层创建配置组不支持中文，但是上层根据产品要求需要显示中文默认配置文件信息名称
     * @param origin
     * @return
     */
    private String changeLanguageFromEnToCh(String origin){
        if ("MySQL5.5DefaultConfigFile".equals(origin)){
            return "MySQL5.5 默认配置文件" ;
        }
        if ("MySQL5.6DefaultConfigFile".equals(origin)){
            return "MySQL5.6 默认配置文件" ;
        }
        return null ;
    }


    /**
     * 具体同步底层Datastore与上层配置文件基本信息的业务处理
     * @throws Exception
     */
    @Override
    public void syncDatastoreDataFromStackToCloud(List<CloudRdsconfigfile> cloudRdsconfigfiles) throws Exception{

        logger.info(" ----- Sync Data Start ----- ");


        // TODO: 2017/3/7 继上述步骤得到对应的配置文件基本信息之后，需要将其与数据库中配置文件基本信息表进行同步
        //遍历所有的配置文件基本信息，按照指定的规则进行处理
        for (CloudRdsconfigfile rdsconfigfile : cloudRdsconfigfiles){

            // TODO: 2017/3/7 目前判断是否有新的默认配置文件的标记为按照“数据中心ID”、“配置文件名称”查询是否有结果进行判断 
            List<CloudRdsconfigfile> cloudRdsconfigfileList = rdsconfigfileDao.queryByDcWithName(rdsconfigfile.getConfigDatacenterid(),
                    changeLanguageFromEnToCh(rdsconfigfile.getConfigName())) ;
            if (cloudRdsconfigfileList == null || cloudRdsconfigfileList.size() == 0){

                // TODO: 2017/3/7 此时判断系统数据表当前没有存储对应的默认配置文件信息，需要重新插入该条数据
                JSONObject ds = baseRdsConfigurationService.getConfigurationGroupDatastoreByVersion(rdsconfigfile.getConfigVersion()) ;
                String paramsContent = baseRdsConfigurationService.queryDefaultParamValueByVersionName(ds.getString("type") + " " + ds.getString("version")) ;
                if (paramsContent == null){
                    //当前系统中还未录入对应版本的数据库默认参数，故不采取任何操作
                    continue;
                }
                ConfigurationGroup group = JSONObject.parseObject(
                        openstackConfigurationGroupService.createConfigurationGroup(
                                rdsconfigfile.getConfigDatacenterid(),  //数据中心ID编号
                                dataCenterService.getById(rdsconfigfile.getConfigDatacenterid()).getOsAdminProjectId(),     //该数据中心对应的管理项目ID
                                ds,  //生成实际创建过程使用到的Datastore JSONObject对象
                                rdsconfigfile.getConfigName(),
                                baseRdsConfigurationService.changeStoreParamValueToOpenStackConfigurationValue(paramsContent)
                        ).getJSONObject(TAG_CONFIGURATION).toJSONString(),
                ConfigurationGroup.class);
                rdsconfigfile.setConfigId(group.getId());

                // TODO: 2017/3/7 表示插入一条最新数据库默认配置文件
                rdsconfigfile.setConfigType("1");
                rdsconfigfile.setConfigDate(new Date());
                rdsconfigfile.setConfigName(changeLanguageFromEnToCh(rdsconfigfile.getConfigName()));
                rdsconfigfile.setConfigVersionname(ds.getString(DB_NAME) + ds.getString(DB_VERSION));
                // TODO: 2017/3/7 插入这一条新的默认配置文件信息
                rdsconfigfileDao.save(rdsconfigfile) ;
                if (rdsconfigparamsDao.findOne(group.getId()) == null) {
                    //配置文件详细信息不存在的时候，插入最新的数据
                    CloudRdsconfigparams cloudRdsconfigparams = new CloudRdsconfigparams();
                    cloudRdsconfigparams.setConfigId(group.getId());
                    cloudRdsconfigparams.setConfigContent(paramsContent);
                    rdsconfigparamsDao.save(cloudRdsconfigparams);
                }

                // TODO: 2017/3/7 ！！！！！有一个问题是否需要考虑底层Datastore已经存在的信息属性是否会改变的情况，
                // TODO: 2017/3/7 按照之前的调研结果来看，Datastore属于全局常量不会轻易改变
            }
        }

        logger.info(" ----- Sync Data End ----- ");

    }

    @Override
    public void syncDefaultMySqlParamsFromStaticFileToDatabase() throws Exception {
        CloudRdsdefaultconfigparams cloudRdsdefaultconfigparams = null ;
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream("D:\\DEFAULT_PARAM.csv")));
            String line = null ;
            int foot = 0 ;
            while ((line = bufferedReader.readLine()) != null){
                String[] lines = line.trim().split(",") ;//分割而成的参数字符串
                System.out.println("当前的IS Static变量为 ： " + ("null".equals(lines[9])?null:lines[9]));
                cloudRdsdefaultconfigparams = new CloudRdsdefaultconfigparams() ;
                cloudRdsdefaultconfigparams.setName("null".equals(lines[0].trim())?null:lines[0].trim()) ;
                cloudRdsdefaultconfigparams.setType("null".equals(lines[1].trim())?null:lines[1].trim()) ;
                cloudRdsdefaultconfigparams.setMinSize("null".equals(lines[2].trim())?null:lines[2].trim());
                cloudRdsdefaultconfigparams.setMaxSize("null".equals(lines[3].trim())?null:lines[3].trim());
                cloudRdsdefaultconfigparams.setOptionValues("null".equals(lines[4].trim())?null:lines[4].trim());
                cloudRdsdefaultconfigparams.setRestart("null".equals(lines[5].trim())?null:lines[5].trim());
                cloudRdsdefaultconfigparams.setOverride("null".equals(lines[6].trim())?null:lines[6].trim());
                cloudRdsdefaultconfigparams.setDefaultValue("null".equals(lines[7].trim())?null:lines[7].trim());
                cloudRdsdefaultconfigparams.setVersion("null".equals(lines[8].trim())?null:lines[8].trim());
                cloudRdsdefaultconfigparams.setIsStatic("null".equals(lines[9].trim())?null:lines[9].trim());
                rdsdefaultconfigparamsDao.save(cloudRdsdefaultconfigparams) ;
                foot ++ ;
            }
            bufferedReader.close();
        }catch (Exception e){
            System.out.println("----------------------------------------------");
            logger.info(cloudRdsdefaultconfigparams.getIsStatic());
            logger.error(e.getMessage(), e);
            System.out.println("----------------------------------------------");
        }
    }
}
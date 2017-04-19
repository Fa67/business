package com.eayun.database.configgroup.ecmccontroller;

import com.alibaba.fastjson.JSONArray;
import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.database.configgroup.model.configfile.CloudRdsconfigfile;
import com.eayun.database.configgroup.service.EcmcRdsConfigurationService;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.virtualization.model.BaseCloudProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/28.
 */
@Controller
@RequestMapping("/ecmc/rds/config")
@Scope("prototype")
public class EcmcRdsConfigurationController {

    //ECMC Controller Method

    @Autowired
    private EcmcRdsConfigurationService ecmcRdsConfigurationService ;
    @Autowired
    private EcmcLogService logService ;
    private static Logger logger = LoggerFactory.getLogger(EcmcRdsConfigurationController.class) ;
    /**
     * 获取当前所有数据库系统版本
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getAllDatabaseVersion", method = RequestMethod.POST)
    @ResponseBody
    public JSONArray getAllDatabaseVersion() throws Exception{
        return ecmcRdsConfigurationService.getAllDatabaseVersion() ;
    }

    /**
     * 按照指定条件查找配置文件信息
     * @param body
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/queryConfigFileForPage", method = RequestMethod.POST)
    @ResponseBody
    public List<CloudRdsconfigfile> queryConfigFileForPage(@RequestBody Map<String,Object> body) throws Exception{
        String dcId = String.valueOf(body.get("dcId")) ;
        String projectId = String.valueOf(body.get("projectId")) ;
        String versionId = String.valueOf(body.get("versionId")) ;
        List<CloudRdsconfigfile> cloudRdsconfigfiles = ecmcRdsConfigurationService.queryConfigFileForPage(dcId,projectId,versionId) ;
        return cloudRdsconfigfiles ;
    }

    /**
     * 创建客户配置文件
     * @param rdsconfigfile
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/createCusConfigFile", method = RequestMethod.POST)
    @ResponseBody
    public EayunResponseJson createCusConfigFile(@RequestBody CloudRdsconfigfile rdsconfigfile) throws Exception {
        rdsconfigfile.setConfigDate(new Date());
        EayunResponseJson json = new EayunResponseJson() ;
        try {
            ecmcRdsConfigurationService.createCusConfigFile(rdsconfigfile) ;
            json.setRespCode(ConstantClazz.SUCCESS_CODE);
            logService.addLog("创建配置文件", ConstantClazz.LOG_TYPE_RDS, rdsconfigfile.getConfigName(), rdsconfigfile.getConfigProjectid(), 1,rdsconfigfile.getConfigId(),null);
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            logService.addLog("创建配置文件", ConstantClazz.LOG_TYPE_RDS, rdsconfigfile.getConfigName(), rdsconfigfile.getConfigProjectid(), 0,rdsconfigfile.getConfigId(), e);
            if (e instanceof AppException){
                throw e ;
            }else {
                json.setRespCode(ConstantClazz.ERROR_CODE);
            }
        }
        return json ;
    }

    /**
     * 删除客户配置文件
     * @param body
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/deleteCusConfigFile", method = RequestMethod.POST)
    @ResponseBody
    public EayunResponseJson deleteCusConfigFile(@RequestBody Map<String, Object> body) throws Exception {
        String groupId = String.valueOf(body.get("groupId")) ;
        EayunResponseJson json = new EayunResponseJson() ;
        CloudRdsconfigfile cloudRdsconfigfile = ecmcRdsConfigurationService.find(groupId);
        try {
            ecmcRdsConfigurationService.deleteCusConfigFile(groupId);
            json.setRespCode(ConstantClazz.SUCCESS_CODE);
            logService.addLog("删除配置文件", ConstantClazz.LOG_TYPE_RDS, groupId, null , 1, cloudRdsconfigfile.getConfigName(),null);
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            logService.addLog("删除配置文件", ConstantClazz.LOG_TYPE_RDS, groupId, null , 0, cloudRdsconfigfile.getConfigName(), e);
            if (e instanceof AppException){
                throw e;
            }else {
                json.setRespCode(ConstantClazz.ERROR_CODE);
            }
        }
        return json ;
    }

    /**
     * 根据配置组查询应用实例信息
     * @param body
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/queryConfigurationGroupInstances", method = RequestMethod.POST)
    @ResponseBody
    public EayunResponseJson queryConfigurationGroupInstances(@RequestBody Map<String, Object> body) throws Exception {
        EayunResponseJson eayunResponseJson = new EayunResponseJson() ;
        String groupId = String.valueOf(body.get("groupId")) ;
        JSONArray jsonArray = ecmcRdsConfigurationService.queryInstanceNamesByConfig(groupId);
        if (jsonArray != null && jsonArray.size() != 0){
            eayunResponseJson.setRespCode(ConstantClazz.SUCCESS_CODE);
        }else {
            eayunResponseJson.setRespCode(ConstantClazz.ERROR_CODE);
        }
        return eayunResponseJson ;
    }

    /**
     * 根据指定配置组查询配置组参数
     * @param body
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/queryConfigParamsByGroupId", method = RequestMethod.POST)
    @ResponseBody
    public EayunResponseJson queryConfigParamsByGroupId(@RequestBody Map<String, Object> body) throws Exception {
        EayunResponseJson eayunResponseJson = new EayunResponseJson() ;
        String groupId = String.valueOf(body.get("groupId")) ;
        try {
            JSONArray cloudRdsconfigparams = ecmcRdsConfigurationService.queryConfigParamsByGroupId(groupId);
            eayunResponseJson.setRespCode(ConstantClazz.SUCCESS_CODE);
            eayunResponseJson.setData(cloudRdsconfigparams);
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            if (e instanceof AppException){
                throw e ;
            }else {
                eayunResponseJson.setRespCode(ConstantClazz.ERROR_CODE);
            }
        }
        return eayunResponseJson ;
    }

    /**
     * 更新客户配置文件
     * @param body
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/updateCusConfigFile", method = RequestMethod.POST)
    @ResponseBody
    public EayunResponseJson updateConfigurationGroup(@RequestBody Map<String, Object> body) throws Exception {
        String configGroupId = String.valueOf(body.get("configGroupId")) ;
        String editParams = String.valueOf(body.get("editParams")) ;
        EayunResponseJson json = new EayunResponseJson() ;

        String checkCondition = ecmcRdsConfigurationService.queryNotActiveInstanceNamesByConfig(configGroupId);
        if (checkCondition == null) {
            CloudRdsconfigfile cloudRdsconfigfile = ecmcRdsConfigurationService.find(configGroupId);
            try {
                ecmcRdsConfigurationService.updateCusConfigFile(configGroupId, editParams);
                json.setRespCode(ConstantClazz.SUCCESS_CODE);
                logService.addLog("编辑配置文件", ConstantClazz.LOG_TYPE_RDS, configGroupId, null, 1, cloudRdsconfigfile.getConfigName(), null);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                logService.addLog("编辑配置文件", ConstantClazz.LOG_TYPE_RDS, configGroupId, null, 0, cloudRdsconfigfile.getConfigName(), e);
                if (e instanceof AppException) {
                    throw e;
                } else {
                    json.setRespCode(ConstantClazz.ERROR_CODE);
                }
            }
            return json;
        }else {
            json.setRespCode(ConstantClazz.ERROR_CODE);
            json.setMessage("MySQL数据库 " + checkCondition + " 非运行中，请稍后重试！");
            return json ;
        }
    }

    /**
     * ECMC端展示默认配置文件
     * @param request
     * @param page
     * @param paramsMap
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/ecmcListDefaultConfigFile", method = RequestMethod.POST)
    @ResponseBody
    public Page ecmcListDefaultConfigFile(HttpServletRequest request, Page page, @RequestBody ParamsMap paramsMap) throws Exception{
        return ecmcRdsConfigurationService.ecmcListDefaultConfigFile(page, paramsMap) ;
    }

    /**
     * 判断指定的配置文件数据当前时刻是否存在
     * @param body
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/existConfigFile", method = RequestMethod.POST)
    @ResponseBody
    public EayunResponseJson existConfigFile(@RequestBody Map<String, Object> body) throws Exception {
        EayunResponseJson eayunResponseJson = new EayunResponseJson();
        String groupId = String.valueOf(body.get("groupId"));
        if (ecmcRdsConfigurationService.existConfigFile(groupId)){
            eayunResponseJson.setRespCode(ConstantClazz.SUCCESS_CODE);
        }else {
            eayunResponseJson.setRespCode(ConstantClazz.ERROR_CODE);
        }
        return eayunResponseJson;
    }

    /**
     * 更新默认配置文件
     * @param body
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/updateDefaultConfigFile", method = RequestMethod.POST)
    @ResponseBody
    public EayunResponseJson updateDefaultConfigFile(@RequestBody Map<String, Object> body) throws Exception {
        String configGroupId = String.valueOf(body.get("configGroupId")) ;
        String editParams = String.valueOf(body.get("editParams")) ;
        EayunResponseJson json = new EayunResponseJson() ;
        CloudRdsconfigfile cloudRdsconfigfile = ecmcRdsConfigurationService.find(configGroupId) ;
        try {
            ecmcRdsConfigurationService.updateDefaultConfigFile(configGroupId, editParams) ;
            json.setRespCode(ConstantClazz.SUCCESS_CODE);
            logService.addLog("编辑默认配置文件", ConstantClazz.LOG_TYPE_RDS, configGroupId, null , 1, cloudRdsconfigfile.getConfigName(),null);
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            logService.addLog("编辑默认配置文件", ConstantClazz.LOG_TYPE_RDS, configGroupId, null , 0, cloudRdsconfigfile.getConfigName(), e);
            if (e instanceof AppException){
                throw e ;
            }else {
                json.setRespCode(ConstantClazz.ERROR_CODE) ;
            }
        }
        return json ;
    }

    /**
     * ECMC端展示默认配置文件信息
     * @param request
     * @param page
     * @param paramsMap
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/ecmcListCusConfigFile", method = RequestMethod.POST)
    @ResponseBody
    public Page ecmcListCusConfigFile(HttpServletRequest request, Page page, @RequestBody ParamsMap paramsMap) throws Exception{
        return ecmcRdsConfigurationService.ecmcListCusConfigFile(page,paramsMap);
    }

    /**
     * 根据数据中心查询所归属的项目信息
     * @param body
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/queryProjectInformationsByDatacenter", method = RequestMethod.POST)
    @ResponseBody
    public EayunResponseJson queryProjectInformationsByDatacenter(@RequestBody Map<String, Object> body) throws Exception{
        String dcId = String.valueOf(body.get("dcId")) ; //获取页面传递来的数据中心ID
        EayunResponseJson eayunResponseJson = new EayunResponseJson() ;
        try {
            List<BaseCloudProject> projects = ecmcRdsConfigurationService.queryProjectInformationsByDatacenter(dcId) ;
            eayunResponseJson.setRespCode(ConstantClazz.SUCCESS_CODE);
            eayunResponseJson.setData(projects);
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            if (e instanceof AppException){
                throw e ;
            }else {
                eayunResponseJson.setRespCode(ConstantClazz.ERROR_CODE);
            }
        }
        return eayunResponseJson ;
    }

    /**
     * 创建客户配置文件时，判断是否有同名文件存在
     * @param body
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/queryCusSelfConfigFileByFilename", method = RequestMethod.POST)
    @ResponseBody
    public EayunResponseJson queryCusSelfConfigFileByFilename(@RequestBody Map<String, Object> body) throws Exception{
        String dcId = String.valueOf(body.get("dcId")) ;
        String projectId = String.valueOf(body.get("projectId")) ;
        String newFileName = String.valueOf(body.get("newFileName")) ;
        return ecmcRdsConfigurationService.queryCusSelfConfigFileByFilename(dcId, projectId, newFileName) ;
    }
}
package com.eayun.database.configgroup.controller;

import com.alibaba.fastjson.JSONArray;
import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.database.configgroup.model.configfile.CloudRdsconfigfile;
import com.eayun.database.configgroup.service.RdsConfigurationService;
import com.eayun.database.configgroup.service.RdsDatastoreService;
import com.eayun.log.service.LogService;
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
@RequestMapping("/rds/config")
@Scope("prototype")
public class RdsConfigurationController{

    //ECSC Controller Method

    @Autowired
    private RdsConfigurationService rdsConfigurationService ;
    @Autowired
    private LogService logService ;
    private static Logger logger = LoggerFactory.getLogger(RdsConfigurationController.class) ;

    /**
     * 展示配置文件列表信息
     * @param request
     * @param page
     * @param paramsMap
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/listConfigFile", method = RequestMethod.POST)
    @ResponseBody
    public Page ecscListConfigFile(HttpServletRequest request, Page page, @RequestBody ParamsMap paramsMap) throws Exception{
        return rdsConfigurationService.ecscListConfigFile(page, paramsMap) ;
    }

    /**
     * 获取当前所有数据库系统版本
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getAllDatabaseVersion", method = RequestMethod.POST)
    @ResponseBody
    public JSONArray getAllDatabaseVersion() throws Exception{
        return rdsConfigurationService.getAllDatabaseVersion() ;
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
        List<CloudRdsconfigfile> cloudRdsconfigfiles = rdsConfigurationService.queryConfigFileForPage(dcId,projectId,versionId) ;
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
            rdsConfigurationService.createCusConfigFile(rdsconfigfile) ;
            json.setRespCode(ConstantClazz.SUCCESS_CODE);
            logService.addLog("创建配置文件", ConstantClazz.LOG_TYPE_RDS, rdsconfigfile.getConfigName(), rdsconfigfile.getConfigProjectid(), ConstantClazz.LOG_STATU_SUCCESS,null);
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            logService.addLog("创建配置文件", ConstantClazz.LOG_TYPE_RDS, rdsconfigfile.getConfigName(), rdsconfigfile.getConfigProjectid(), ConstantClazz.LOG_STATU_ERROR, e);
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
        CloudRdsconfigfile cloudRdsconfigfile = rdsConfigurationService.find(groupId);
        try {
            rdsConfigurationService.deleteCusConfigFile(groupId);
            json.setRespCode(ConstantClazz.SUCCESS_CODE);
            logService.addLog("删除配置文件", ConstantClazz.LOG_TYPE_RDS, cloudRdsconfigfile.getConfigName(), cloudRdsconfigfile.getConfigProjectid() , ConstantClazz.LOG_STATU_SUCCESS,null);
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            logService.addLog("删除配置文件", ConstantClazz.LOG_TYPE_RDS, cloudRdsconfigfile.getConfigName(), cloudRdsconfigfile.getConfigProjectid(), ConstantClazz.LOG_STATU_ERROR, e);
            if (e instanceof AppException){
                throw e ;
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
        //当前是从数据表中直接查找对应信息
        EayunResponseJson eayunResponseJson = new EayunResponseJson() ;
        String groupId = String.valueOf(body.get("groupId")) ;
        JSONArray jsonArray = rdsConfigurationService.queryInstanceNamesByConfig(groupId);
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
        String groupId = String.valueOf(body.get("groupId"));
        try {
            JSONArray cloudRdsconfigparams = rdsConfigurationService.queryConfigParamsByGroupId(groupId);
            eayunResponseJson.setRespCode(ConstantClazz.SUCCESS_CODE);
            eayunResponseJson.setData(cloudRdsconfigparams);
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            if (e instanceof AppException){
                throw e;
            }else {
                eayunResponseJson.setRespCode(ConstantClazz.ERROR_CODE);
            }
        }
        return eayunResponseJson ;
    }

    @RequestMapping(value = "/existConfigFile", method = RequestMethod.POST)
    @ResponseBody
    public EayunResponseJson existConfigFile(@RequestBody Map<String, Object> body) throws Exception {
        EayunResponseJson eayunResponseJson = new EayunResponseJson();
        String groupId = String.valueOf(body.get("groupId"));
        if (rdsConfigurationService.existConfigFile(groupId)){
            eayunResponseJson.setRespCode(ConstantClazz.SUCCESS_CODE);
        }else {
            eayunResponseJson.setRespCode(ConstantClazz.ERROR_CODE);
        }
        return eayunResponseJson;
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
        EayunResponseJson json = new EayunResponseJson();

        String checkCondition = rdsConfigurationService.queryNotActiveInstanceNamesByConfig(configGroupId);
        if (checkCondition == null) {
            CloudRdsconfigfile cloudRdsconfigfile = rdsConfigurationService.find(configGroupId);
            try {
                rdsConfigurationService.updateCusConfigFile(configGroupId, editParams);
                json.setRespCode(ConstantClazz.SUCCESS_CODE);
                logService.addLog("编辑配置文件", ConstantClazz.LOG_TYPE_RDS, cloudRdsconfigfile.getConfigName(), cloudRdsconfigfile.getConfigProjectid(), ConstantClazz.LOG_STATU_SUCCESS, null);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                logService.addLog("编辑配置文件", ConstantClazz.LOG_TYPE_RDS, cloudRdsconfigfile.getConfigName(), cloudRdsconfigfile.getConfigProjectid(), ConstantClazz.LOG_STATU_ERROR, e);
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
        return rdsConfigurationService.queryCusSelfConfigFileByFilename(dcId, projectId, newFileName) ;
    }
}
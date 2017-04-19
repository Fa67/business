package com.eayun.database.instance.ecmccontroller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.util.StringUtil;
import com.eayun.database.instance.model.CloudOrderRDSInstance;
import com.eayun.database.instance.model.CloudRDSInstance;
import com.eayun.database.instance.service.EcmcCloudRDSInstanceService;
import com.eayun.ecmcuser.util.EcmcSessionUtil;
import com.eayun.log.ecmcsevice.EcmcLogService;

@Controller
@RequestMapping("/ecmc/rds/instance")
@Scope("prototype")
public class EcmcRDSInstanceController {
    private static final Logger log = LoggerFactory.getLogger(EcmcRDSInstanceController.class);
    @Autowired
    private EcmcCloudRDSInstanceService instanceService;
    @Autowired
    private EcmcLogService logService;
    
    @RequestMapping(value = "/getlist", method = RequestMethod.POST)
    @ResponseBody
    public String getInstanceList (HttpServletRequest request, Page page, @RequestBody ParamsMap paramsMap) throws Exception {
        log.info("查询数据库实例");
        page = instanceService.getList(page, paramsMap);
        return JSONObject.toJSONString(page);
    }
    
    @RequestMapping(value = "/detail", method = RequestMethod.POST)
    @ResponseBody
    public String getDetailByInstanceId (HttpServletRequest request, @RequestBody Map<String, String> map) throws Exception {
        log.info("数据库实例详情");
        EayunResponseJson json = new EayunResponseJson();
        String instanceId = null != map.get("instanceId") ? map.get("instanceId").toString() : null;
        CloudRDSInstance instance = instanceService.getInstanceById(instanceId);
        if (StringUtil.isEmpty(instance.getRdsId())) {
            json.setRespCode(ConstantClazz.ERROR_CODE);
        } else {
            json.setRespCode(ConstantClazz.SUCCESS_CODE);
            json.setData(instance);
        }
        return JSONObject.toJSONString(json);
    }
    
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String deleteInstance (HttpServletRequest request, @RequestBody CloudRDSInstance instance) throws Exception {
        log.info("删除数据库实例");
        EayunResponseJson json = new EayunResponseJson();
        try {
            json = instanceService.deleteRdsInstance(instance, EcmcSessionUtil.getUser().getAccount());
            logService.addLog("删除", ConstantClazz.LOG_TYPE_RDS, instance.getRdsName(), instance.getPrjId(), 1, instance.getRdsId(), null);
        } catch (AppException e) {
            log.error(e.getMessage(), e);
            logService.addLog("删除", ConstantClazz.LOG_TYPE_RDS, instance.getRdsName(), instance.getPrjId(), 0, instance.getRdsId(), e);
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            logService.addLog("删除", ConstantClazz.LOG_TYPE_RDS, instance.getRdsName(), instance.getPrjId(), 0, instance.getRdsId(), e);
            throw e;
        }
        return JSONObject.toJSONString(json);
    }
    
    @RequestMapping(value = "/reboot", method = RequestMethod.POST)
    @ResponseBody
    public String rebootInstance (HttpServletRequest request, @RequestBody CloudRDSInstance instance) throws Exception {
        log.info("重启数据库实例");
        EayunResponseJson json = new EayunResponseJson();
        try {
            instanceService.restart(instance);
            json.setRespCode(ConstantClazz.SUCCESS_CODE);
            logService.addLog("重启", ConstantClazz.LOG_TYPE_RDS, instance.getRdsName(), instance.getPrjId(), 1, instance.getRdsId(), null);
        } catch (AppException e) {
            log.error(e.getMessage(), e);
            logService.addLog("重启", ConstantClazz.LOG_TYPE_RDS, instance.getRdsName(), instance.getPrjId(), 0, instance.getRdsId(), e);
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            logService.addLog("重启", ConstantClazz.LOG_TYPE_RDS, instance.getRdsName(), instance.getPrjId(), 0, instance.getRdsId(), e);
            throw e;
        }
        return JSONObject.toJSONString(json);
    }
    
    @RequestMapping(value = "/detachreplica", method = RequestMethod.POST)
    @ResponseBody
    public String detachReplica (HttpServletRequest request, @RequestBody CloudRDSInstance instance) throws Exception {
        log.info("数据库实例从库升级主库");
        EayunResponseJson json = new EayunResponseJson();
        try {
            instanceService.detachReplica(instance);
            json.setRespCode(ConstantClazz.SUCCESS_CODE);
            logService.addLog("升为主库", ConstantClazz.LOG_TYPE_RDS, instance.getRdsName(), instance.getPrjId(), 1, instance.getRdsId(), null);
        } catch (AppException e) {
            log.error(e.getMessage(), e);
            logService.addLog("升为主库", ConstantClazz.LOG_TYPE_RDS, instance.getRdsName(), instance.getPrjId(), 0, instance.getRdsId(), e);
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            logService.addLog("升为主库", ConstantClazz.LOG_TYPE_RDS, instance.getRdsName(), instance.getPrjId(), 0, instance.getRdsId(), e);
            throw e;
        }
        return JSONObject.toJSONString(json);
    }
    
    @RequestMapping(value = "/checkquota", method = RequestMethod.POST)
    @ResponseBody
    public String checkInstanceQuota (HttpServletRequest request, @RequestBody CloudOrderRDSInstance instance) {
        EayunResponseJson json = new EayunResponseJson();
        try {
            String respMsg = instanceService.checkInstanceQuota(instance);
            if ("OUT_OF_QUOTA".equals(respMsg)) {
                json.setRespCode(ConstantClazz.WARNING_CODE);
                json.setMessage(respMsg);
            } else {
                json.setRespCode(ConstantClazz.SUCCESS_CODE);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        return JSONObject.toJSONString(json);
    } 
    
    @RequestMapping(value = "/notexist", method = RequestMethod.POST)
    @ResponseBody
    public String checkNameNotExist (HttpServletRequest request, @RequestBody CloudRDSInstance instance) {
        log.info("校验数据库实例名称是否不存在");
        EayunResponseJson json = new EayunResponseJson();
        boolean flag = instanceService.checkRdsNameExist(instance.getRdsId(), instance.getRdsName(), instance.getPrjId());
        json.setRespCode(ConstantClazz.SUCCESS_CODE);
        json.setData(flag);
        return JSONObject.toJSONString(json);
    }
    
    @RequestMapping(value = "/modify", method = RequestMethod.POST)
    @ResponseBody
    public String modifyRdsInstance (HttpServletRequest request, @RequestBody CloudRDSInstance instance) throws Exception {
        log.info("更改数据库实例信息");
        EayunResponseJson json = new EayunResponseJson();
        try {
            instanceService.modifyRdsInstance(instance);
            json.setRespCode(ConstantClazz.SUCCESS_CODE);
        } catch (AppException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        return JSONObject.toJSONString(json);
    }
    
    @RequestMapping(value = "/getconfiglist", method = RequestMethod.POST)
    @ResponseBody
    public String getConfigList (HttpServletRequest request, @RequestBody Map<String, String> map) {
        log.info("获取数据库配置列表");
        EayunResponseJson json = new EayunResponseJson();
        String prjId = null != map.get("prjId") ? map.get("prjId").toString() : null;
        String versionId = null != map.get("versionId") ? map.get("versionId").toString() : null;
        try {
            json = instanceService.getConfigList(prjId, versionId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        return JSONObject.toJSONString(json);
    }
    
    @RequestMapping(value = "/modifyconfig", method = RequestMethod.POST)
    @ResponseBody
    public String modifyConfigFile (HttpServletRequest request, @RequestBody CloudRDSInstance instance) throws AppException {
        log.info("修改数据库配置文件");
        EayunResponseJson json = new EayunResponseJson();
        try {
            json = instanceService.modifyRdsInstanceConfiguraion(instance);
            if (ConstantClazz.SUCCESS_CODE.equals(json.getRespCode())) {
                logService.addLog("更改配置文件", ConstantClazz.LOG_TYPE_RDS, instance.getRdsName(), instance.getPrjId(), 1, instance.getRdsId(), null);
            }
        } catch (AppException e) {
            log.error(e.getMessage(), e);
            logService.addLog("更改配置文件", ConstantClazz.LOG_TYPE_RDS, instance.getRdsName(), instance.getPrjId(), 0, instance.getRdsId(), e);
            throw e;
        }
        return JSONObject.toJSONString(json);
    }
    
    @RequestMapping(value = "/getallversion", method = RequestMethod.POST)
    @ResponseBody
    public String getAllVersion (HttpServletRequest request) {
        EayunResponseJson json = new EayunResponseJson();
        List<JSONObject> list = instanceService.getAllDBVersion();
        json.setRespCode(ConstantClazz.SUCCESS_CODE);
        json.setData(list);
        return JSONObject.toJSONString(json);
    }
}

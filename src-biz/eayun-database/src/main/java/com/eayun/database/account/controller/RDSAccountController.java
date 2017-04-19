package com.eayun.database.account.controller;

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
import com.eayun.common.controller.BaseController;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.database.account.model.CloudRDSAccount;
import com.eayun.database.account.service.RDSAccountService;
import com.eayun.database.instance.model.CloudRDSInstance;
import com.eayun.database.instance.service.RDSInstanceService;
import com.eayun.log.service.LogService;

@Controller
@RequestMapping("/rds/account")
@Scope("prototype")
public class RDSAccountController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(RDSAccountController.class);
    @Autowired
    private RDSAccountService accountService;
    @Autowired
    private LogService logService;
    @Autowired
    private RDSInstanceService instanceService;
    
    @RequestMapping(value = "/getlist", method = RequestMethod.POST)
    @ResponseBody
    public String getDBAccountList (HttpServletRequest request, Page page, @RequestBody ParamsMap paramsMap) throws Exception {
        page = accountService.getAccountPageList(page, paramsMap);
        return JSONObject.toJSONString(page);
    }
    
    @RequestMapping(value = "/checkname", method = RequestMethod.POST)
    @ResponseBody
    public String checkAccountName (HttpServletRequest request, @RequestBody CloudRDSAccount account) {
        boolean isExisted = accountService.checkAccountNameExist(account);
        return JSONObject.toJSONString(isExisted);
    }
    
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public String createDBAccount (HttpServletRequest request, @RequestBody CloudRDSAccount account) throws AppException {
        try {
            account = accountService.createAccount(account);
            logService.addLog("创建账号",
                    ConstantClazz.LOG_TYPE_RDS, 
                    account.getInstanceName(), 
                    account.getPrjId(), 
                    ConstantClazz.LOG_STATU_SUCCESS, 
                    null);
        } catch (AppException e) {
            logService.addLog("创建账号",
                    ConstantClazz.LOG_TYPE_RDS, 
                    account.getInstanceName(), 
                    account.getPrjId(), 
                    ConstantClazz.LOG_STATU_ERROR, 
                    e);
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logService.addLog("创建账号",
                    ConstantClazz.LOG_TYPE_RDS, 
                    account.getInstanceName(), 
                    account.getPrjId(), 
                    ConstantClazz.LOG_STATU_ERROR, 
                    e);
            log.error(e.getMessage(), e);
            throw e;
        }
        return JSONObject.toJSONString(account);
    }
    
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String deleteDBAccount (HttpServletRequest request, @RequestBody CloudRDSAccount account) throws AppException {
        EayunResponseJson json = new EayunResponseJson();
        boolean flag = false;
        try {
            flag = accountService.deleteAccount(account);
            logService.addLog("删除账号",
                    ConstantClazz.LOG_TYPE_RDS, 
                    account.getInstanceName(), 
                    account.getPrjId(), 
                    ConstantClazz.LOG_STATU_SUCCESS, 
                    null);
            json.setRespCode(ConstantClazz.SUCCESS_CODE);
            json.setData(flag);
        } catch (AppException e) {
            if ("account does not exist".equals(e.getMessage())) {
                json.setRespCode(ConstantClazz.WARNING_CODE);
                json.setData(flag);
                json.setMessage(e.getMessage());
                return JSONObject.toJSONString(json);
            }
            logService.addLog("删除账号",
                    ConstantClazz.LOG_TYPE_RDS, 
                    account.getInstanceName(), 
                    account.getPrjId(), 
                    ConstantClazz.LOG_STATU_ERROR, 
                    e);
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logService.addLog("删除账号",
                    ConstantClazz.LOG_TYPE_RDS, 
                    account.getInstanceName(), 
                    account.getPrjId(), 
                    ConstantClazz.LOG_STATU_ERROR, 
                    e);
            log.error(e.getMessage(), e);
            throw e;
        }
        return JSONObject.toJSONString(json);
    }
    
    @RequestMapping(value = "/updateaccess", method = RequestMethod.POST)
    @ResponseBody
    public String updateAccessDBAccount (HttpServletRequest request, @RequestBody CloudRDSAccount account) throws AppException {
        EayunResponseJson json = new EayunResponseJson();
        boolean flag = false;
        try {
            flag = accountService.updateAccessAccount(account, false);
            logService.addLog("修改权限",
                    ConstantClazz.LOG_TYPE_RDS, 
                    account.getInstanceName(), 
                    account.getPrjId(), 
                    ConstantClazz.LOG_STATU_SUCCESS, 
                    null);
            json.setRespCode(ConstantClazz.SUCCESS_CODE);
            json.setData(flag);
        } catch (AppException e) {
            if ("database does not exist".equals(e.getMessage())) {
                json.setRespCode(ConstantClazz.WARNING_CODE);
                json.setData(false);
                json.setMessage(e.getMessage());
                return JSONObject.toJSONString(json);
            }
            logService.addLog("修改权限",
                    ConstantClazz.LOG_TYPE_RDS, 
                    account.getInstanceName(), 
                    account.getPrjId(), 
                    ConstantClazz.LOG_STATU_ERROR, 
                    e);
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logService.addLog("修改权限",
                    ConstantClazz.LOG_TYPE_RDS, 
                    account.getInstanceName(), 
                    account.getPrjId(), 
                    ConstantClazz.LOG_STATU_ERROR, 
                    e);
            log.error(e.getMessage(), e);
            throw e;
        }
        return JSONObject.toJSONString(json);
    }
    
    @RequestMapping(value = "/updatepw", method = RequestMethod.POST)
    @ResponseBody
    public String updatePasswordDBAccount (HttpServletRequest request, @RequestBody CloudRDSAccount account) throws AppException {
        boolean flag = false;
        try {
            flag = accountService.updatePasswordAccount(account);
            logService.addLog("修改密码",
                    ConstantClazz.LOG_TYPE_RDS, 
                    account.getInstanceName(), 
                    account.getPrjId(), 
                    ConstantClazz.LOG_STATU_SUCCESS, 
                    null);
        } catch (AppException e) {
            logService.addLog("修改密码",
                    ConstantClazz.LOG_TYPE_RDS, 
                    account.getInstanceName(), 
                    account.getPrjId(), 
                    ConstantClazz.LOG_STATU_ERROR, 
                    e);
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logService.addLog("修改密码",
                    ConstantClazz.LOG_TYPE_RDS, 
                    account.getInstanceName(), 
                    account.getPrjId(), 
                    ConstantClazz.LOG_STATU_ERROR, 
                    e);
            log.error(e.getMessage(), e);
            throw e;
        }
        return JSONObject.toJSONString(flag);
    }
    
    @RequestMapping(value = "/resetpw", method = RequestMethod.POST)
    @ResponseBody
    public String resetRootPassword (HttpServletRequest request, @RequestBody CloudRDSAccount root) throws AppException {
        boolean flag = false;
        try {
            flag = accountService.resetRootPassword(root, true);
            logService.addLog("root重置密码",
                    ConstantClazz.LOG_TYPE_RDS, 
                    root.getInstanceName(), 
                    root.getPrjId(), 
                    ConstantClazz.LOG_STATU_SUCCESS, 
                    null);
        } catch (AppException e) {
            logService.addLog("root重置密码",
                    ConstantClazz.LOG_TYPE_RDS, 
                    root.getInstanceName(), 
                    root.getPrjId(), 
                    ConstantClazz.LOG_STATU_ERROR, 
                    e);
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logService.addLog("root重置密码",
                    ConstantClazz.LOG_TYPE_RDS, 
                    root.getInstanceName(), 
                    root.getPrjId(), 
                    ConstantClazz.LOG_STATU_ERROR, 
                    e);
            log.error(e.getMessage(), e);
            throw e;
        }
        return JSONObject.toJSONString(flag);
    }
    
    @RequestMapping(value = "/getalldblist", method = RequestMethod.POST)
    @ResponseBody
    public String getAllDatabaseListByInstanceId (HttpServletRequest request, @RequestBody Map<String, String> map) throws Exception {
        String instanceId = map.get("instanceId");
        EayunResponseJson json = new EayunResponseJson();
        json.setRespCode(ConstantClazz.SUCCESS_CODE);
        json.setData(accountService.getAllDatabaseListByInstanceId(instanceId));
        return JSONObject.toJSONString(json);
    }
    
    @RequestMapping(value = "/getdblistmanaged", method = RequestMethod.POST)
    @ResponseBody
    public String getDatabaseListManagedByDBAccount (HttpServletRequest request, @RequestBody Map<String, String> map) {
        String accountId = map.get("accountId");
        EayunResponseJson json = new EayunResponseJson();
        json.setRespCode(ConstantClazz.SUCCESS_CODE);
        json.setData(accountService.getDatabaseListManagedByAccount(accountId));
        return JSONObject.toJSONString(json);
    }
    
}

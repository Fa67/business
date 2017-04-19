package com.eayun.database.database.controller;

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
import com.eayun.database.database.model.CloudRDSDatabase;
import com.eayun.database.database.service.RDSDatabaseService;
import com.eayun.log.service.LogService;

@Controller
@RequestMapping("/rds/database")
@Scope("prototype")
public class RDSDatabaseController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(RDSDatabaseController.class);
    @Autowired
    private RDSDatabaseService databaseService;
    @Autowired
    private LogService logService;
    
    @RequestMapping(value = "/getlist", method = RequestMethod.POST)
    @ResponseBody
    public String getDatabaseList (HttpServletRequest request, Page page, @RequestBody ParamsMap paramsMap) throws Exception {
        page = databaseService.getDatabasePageList(page, paramsMap);
        return JSONObject.toJSONString(page);
    }
    
    @RequestMapping(value = "/checkname", method = RequestMethod.POST)
    @ResponseBody
    public String checkDBName (HttpServletRequest request, @RequestBody CloudRDSDatabase database) throws Exception {
        boolean isExisted = databaseService.checkDBNameExist(database);
        return JSONObject.toJSONString(isExisted);
    }
    
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public String createDatabase (HttpServletRequest request, @RequestBody CloudRDSDatabase database) throws AppException {
        EayunResponseJson json = new EayunResponseJson();
        try {
            databaseService.createDatabase(database);
            logService.addLog("创建数据库",
                    ConstantClazz.LOG_TYPE_RDS,
                    database.getInstanceName(),
                    database.getPrjId(),
                    ConstantClazz.LOG_STATU_SUCCESS,
                    null);
        } catch (AppException e) {
            logService.addLog("创建数据库",
                    ConstantClazz.LOG_TYPE_RDS,
                    database.getInstanceName(),
                    database.getPrjId(),
                    ConstantClazz.LOG_STATU_ERROR,
                    e);
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logService.addLog("创建数据库",
                    ConstantClazz.LOG_TYPE_RDS,
                    database.getInstanceName(),
                    database.getPrjId(),
                    ConstantClazz.LOG_STATU_ERROR,
                    e);
            log.error(e.getMessage(), e);
            throw e;
        }
        json.setRespCode(ConstantClazz.SUCCESS_CODE);
        return JSONObject.toJSONString(json);
    }
    
    @RequestMapping(value = "/delete")
    @ResponseBody
    public String deleteDatabase (HttpServletRequest request, @RequestBody CloudRDSDatabase database) throws AppException {
        EayunResponseJson json = new EayunResponseJson();
        boolean flag = false;
        try {
            flag = databaseService.deleteDatabase(database);
            logService.addLog("删除数据库",
                    ConstantClazz.LOG_TYPE_RDS,
                    database.getInstanceName(),
                    database.getPrjId(),
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
            logService.addLog("删除数据库",
                    ConstantClazz.LOG_TYPE_RDS,
                    database.getInstanceName(),
                    database.getPrjId(),
                    ConstantClazz.LOG_STATU_ERROR,
                    e);
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logService.addLog("删除数据库",
                    ConstantClazz.LOG_TYPE_RDS,
                    database.getInstanceName(),
                    database.getPrjId(),
                    ConstantClazz.LOG_STATU_ERROR,
                    e);
            log.error(e.getMessage(), e);
            throw e;
        }
        return JSONObject.toJSONString(flag);
    }
}

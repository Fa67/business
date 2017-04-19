package com.eayun.syssetup.controller;

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
import com.eayun.common.controller.BaseController;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.log.service.LogService;
import com.eayun.syssetup.model.CloudModel;
import com.eayun.syssetup.service.CloudModelService;

/**
 * 业务管理
 * @Filename: CloudModelController.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2015年9月21日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Controller
@RequestMapping("/syssetup")
@Scope("prototype")
public class CloudModelController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(CloudModelController.class);
    
    @Autowired
    private CloudModelService cloudModelService;
    
    @Autowired
    private LogService logService;
    
    
    /**
     * 传入用户的id，返回该用户已创建的主机模型列表
     * @param request
     * @param modelCusid
     * @return
     */
    @RequestMapping(value= "/getModelListByCustomer" , method = RequestMethod.POST)
    @ResponseBody
    public String getModelListByCustomer(HttpServletRequest request) {
        log.info("云主机类型列表查询开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String modelCusid = sessionUser.getCusId();
        List<CloudModel> modelList = cloudModelService.getModelListByCustomer(modelCusid);
        return JSONObject.toJSONString(modelList);
    }
    
    /**
     * 添加一条云主机类型记录
     * @param request
     * @param cloudModel
     * @return
     */
    @RequestMapping(value = "/addCloudModel" , method = RequestMethod.POST)
    @ResponseBody
    public String addCloudModel(HttpServletRequest request, @RequestBody CloudModel cloudModel) {
        log.info("创建云主机类型开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String modelCusid = sessionUser.getCusId();
        cloudModel.setModelCusid(modelCusid);
        CloudModel model = new CloudModel();
        try {
            model = cloudModelService.addCloudModel(cloudModel);
            logService.addLog("创建云主机类型", ConstantClazz.LOG_TYPE_SYSSETUP, model.getModelName(), null, ConstantClazz.LOG_STATU_SUCCESS,null);
        } catch (Exception e) {
            log.error("创建云主机类型失败", e);
            logService.addLog("创建云主机类型", ConstantClazz.LOG_TYPE_SYSSETUP, cloudModel.getModelName(), null, ConstantClazz.LOG_STATU_ERROR,e);
            throw e;
        }
        return JSONObject.toJSONString(model);
    }
    
    /**
     * 修改云主机类型记录
     * @param request
     * @param cloudModel
     * @return
     */
    @RequestMapping(value = "/updateCloudModel" , method = RequestMethod.POST)
    @ResponseBody
    public String updateCloudModel(HttpServletRequest request, @RequestBody CloudModel cloudModel) {
        log.info("编辑云主机类型开始");
        CloudModel model = null;
        try {
            model = cloudModelService.updateCloudModel(cloudModel);
            logService.addLog("编辑云主机类型", ConstantClazz.LOG_TYPE_SYSSETUP, model.getModelName(), null,  ConstantClazz.LOG_STATU_SUCCESS,null);
        } catch (Exception e) {
            log.error("编辑云主机类型失败", e);
            logService.addLog("编辑云主机类型", ConstantClazz.LOG_TYPE_SYSSETUP, cloudModel.getModelName(), null,  ConstantClazz.LOG_STATU_ERROR,e);
            throw e;
        }
        return JSONObject.toJSONString(model);
    }
    
    /**
     * 删除云主机类型记录
     * @param request
     * @param cloudModel
     * @return
     */
    @RequestMapping(value = "/deleteCloudModel" , method = RequestMethod.POST)
    @ResponseBody
    public String deleteCloudModel(HttpServletRequest request, @RequestBody Map params) {
        log.info("删除云主机类型开始");
        String modelId = params.get("modelId").toString();
        String modelName = params.get("modelName").toString();
        boolean isok = false;
        try {
            cloudModelService.deleteCloudModel(modelId);
            logService.addLog("删除云主机类型", ConstantClazz.LOG_TYPE_SYSSETUP, modelName, null, ConstantClazz.LOG_STATU_SUCCESS,null);
            isok = true;
        } catch (Exception e) {
            log.error("删除云主机类型失败", e);
            logService.addLog("删除云主机类型", ConstantClazz.LOG_TYPE_SYSSETUP, modelName, null, ConstantClazz.LOG_STATU_ERROR,e);
            throw e;
        }
        return JSONObject.toJSONString(isok);
    }
    
    /**
     * 查询一条云主机类型记录
     * @param request
     * @param modelId
     * @return
     */
    @RequestMapping(value = "/findCloudModelById" , method = RequestMethod.POST)
    @ResponseBody
    public String findCloudModelById(HttpServletRequest request, @RequestBody Map params) {
        log.info("获取单个云主机类型信息开始");
        String modelId = params.get("modelId").toString();
        CloudModel cloudModel = cloudModelService.findCloudModelById(modelId);
        return JSONObject.toJSONString(cloudModel);
    }
    /**
     * 检查该用户已建云主机是否超过三台
     * @param request
     * @param modelCusid
     * @return
     */
    @RequestMapping(value = "/checkCloudNumlByCus" , method = RequestMethod.POST)
    @ResponseBody
    public String checkCloudNumlByCus(HttpServletRequest request, @RequestBody Map params) {
        log.info("校验客户云主机个数开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String modelCusid = sessionUser.getCusId();
        boolean isAllowAdd = cloudModelService.checkCloudNumlByCus(modelCusid);
        return JSONObject.toJSONString(isAllowAdd);
    }
    /**
     * 检查该用户的填写的主机名是否重复
     * @param request
     * @param modelCusid
     * @param modelName
     * @return
     */
    @RequestMapping(value = "/checkNamelByCusAndName" , method = RequestMethod.POST)
    @ResponseBody
    public String checkNamelByCusAndName(HttpServletRequest request, @RequestBody Map params) {
        log.info("校验云主机名称开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String modelCusid = sessionUser.getCusId();
        String modelName = params.get("modelName").toString();
        String modelId = params.get("modelId").toString();
        boolean isUpdateName = cloudModelService.checkNamelByCusAndName(modelCusid, modelName, modelId); 
        return JSONObject.toJSONString(isUpdateName);
    }
}
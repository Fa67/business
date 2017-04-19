/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.ecmcauthority.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eayun.common.ConstantClazz;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.ecmcauthority.model.BaseEcmcSysAuthority;
import com.eayun.ecmcauthority.service.EcmcSysAuthorityService;
import com.eayun.ecmcuser.util.EcmcSessionUtil;
import com.eayun.log.ecmcsevice.EcmcLogService;

/**
 *                       
 * @Filename: EcmcAuthorityController.java
 * @Description: 
 * @Version: 1.0
 * @Author: fangjun.yang
 * @Email: fangjun.yang@eayun.com
 * @History:<br>
 *<li>Date: 2016年3月29日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Controller
@RequestMapping("/ecmc/system/authority")
@Scope("prototype")
public class EcmcAuthorityController {

    private final static Logger     log           = LoggerFactory.getLogger(EcmcAuthorityController.class);

    private final static String     LOG_TYPE_AUTH = "权限管理";

    private final static String     LOG_OPT_ADD   = "创建权限";

    private final static String     LOG_OPT_MOD   = "编辑权限";

    private final static String     LOG_OPT_DEL   = "删除权限";

    @Autowired
    private EcmcSysAuthorityService ecmcSysAuthorityService;

    @Autowired
    private EcmcLogService          ecmcLogService;

    /**
     * 查询权限列表
     * @param httpRequest
     * @return
     * @throws AppException
     */
    @RequestMapping("/findauthoritylist")
    @ResponseBody
    public Object findAuthorityList(@RequestBody Map<String, String> params) throws AppException {
        return ecmcSysAuthorityService.getSysAuthorityList(params.get("menuId"));
    }

    /**
     * 创建权限
     * @param auth
     * @return
     * @throws AppException
     */
    @RequestMapping("/createauthority")
    @ResponseBody
    public Object createAuthority(HttpServletRequest httpRequest, @RequestBody BaseEcmcSysAuthority auth) throws AppException {
        Map<String, Object> resultJson = new HashMap<>();
        try {
            auth.setCreatedBy(EcmcSessionUtil.getUser().getId());
            ecmcSysAuthorityService.addSysAuthority(auth);
            resultJson.put("respCode", ConstantClazz.SUCCESS_CODE);
            ecmcLogService.addLog(LOG_OPT_ADD, LOG_TYPE_AUTH, auth.getName(), null, 1, auth.getId(), null);
        } catch (AppException e) {
            log.error(e.toString(), e);
            resultJson.put("respCode", ConstantClazz.ERROR_CODE);
            ecmcLogService.addLog(LOG_OPT_ADD, LOG_TYPE_AUTH, auth.getName(), null, 0, auth.getId(), e);
        }
        return resultJson;
    }

    /**
     * 修改权限
     * @param auth
     * @return
     * @throws AppException
     */
    @RequestMapping("/modifyauthority")
    @ResponseBody
    public Object modifyAuthority(@RequestBody BaseEcmcSysAuthority auth) throws AppException {
        Map<String, Object> resultJson = new HashMap<>();
        try {
            ecmcSysAuthorityService.updateSysAuthority(auth);
            resultJson.put("respCode", ConstantClazz.SUCCESS_CODE);
            ecmcLogService.addLog(LOG_OPT_MOD, LOG_TYPE_AUTH, auth.getName(), null, 1, auth.getId(), null);

        } catch (AppException e) {
            log.error(e.toString(), e);
            resultJson.put("respCode", ConstantClazz.ERROR_CODE);
            ecmcLogService.addLog(LOG_OPT_MOD, LOG_TYPE_AUTH, auth.getName(), null, 0, auth.getId(), e);
        }
        return resultJson;
    }

    /**
     * 删除权限
     * @param authorityId
     * @return
     * @throws AppException
     */
    @RequestMapping("/deleteauthority")
    @ResponseBody
    public Object deleteAuthority(@RequestBody Map<String, String> params) throws AppException {
        Map<String, Object> resultJson = new HashMap<>();
        try {
            ecmcSysAuthorityService.deleteSysAuthority(params.get("authorityId"));
            resultJson.put("respCode", ConstantClazz.SUCCESS_CODE);
            ecmcLogService.addLog(LOG_OPT_DEL, LOG_TYPE_AUTH, null, null, 1, params.get("authorityId"), null);
        } catch (AppException e) {
            log.error(e.toString(), e);
            resultJson.put("respCode", ConstantClazz.ERROR_CODE);
            ecmcLogService.addLog(LOG_OPT_DEL, LOG_TYPE_AUTH, null, null, 0, params.get("authorityId"), e);
        }
        return resultJson;
    }

    /**
     * 查询所有权限
     * @return
     * @throws AppException
     */
    @RequestMapping("/getallenableauth")
    @ResponseBody
    public Object getAllEnableAuth() throws AppException {
        EayunResponseJson responseJson = new EayunResponseJson();
        responseJson.setData(ecmcSysAuthorityService.getAllEnableAuthList());
        responseJson.setRespCode(ConstantClazz.SUCCESS_CODE);
        return responseJson;
    }
}

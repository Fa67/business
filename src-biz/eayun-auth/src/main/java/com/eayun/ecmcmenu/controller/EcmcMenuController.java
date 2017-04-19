/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.ecmcmenu.controller;

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
import com.eayun.common.util.BeanUtils;
import com.eayun.ecmcmenu.model.BaseEcmcSysMenu;
import com.eayun.ecmcmenu.service.EcmcSysMenuService;
import com.eayun.ecmcuser.util.EcmcSessionUtil;
import com.eayun.log.ecmcsevice.EcmcLogService;

/**
 *                       
 * @Filename: EcmcMenutController.java
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
@RequestMapping("/ecmc/system/menu")
@Scope("prototype")
public class EcmcMenuController {

    private final static Logger log           = LoggerFactory.getLogger(EcmcMenuController.class);

    private final static String LOG_TYPE_MENU = "菜单";

    private final static String LOG_OPT_ADD   = "创建菜单";

    private final static String LOG_OPT_MOD   = "修改菜单";

    private final static String LOG_OPT_DEL   = "删除菜单";

    @Autowired
    private EcmcSysMenuService  menuService;

    @Autowired
    private EcmcLogService      ecmcLogService;

    /**
     * 获取菜单树形列表，数据项根据父子关系和排序号 排序。嵌套结构
     * @return
     * @throws AppException
     */
    @RequestMapping("/getmenutreegrid")
    @ResponseBody
    public Object getMenuTreeGrid() throws AppException {
        return menuService.getEcmcSysMenuGridList();
    }

    /**
     * 创建菜单
     * @param menu
     * @return
     * @throws AppException
     */
    @RequestMapping("/createmenu")
    @ResponseBody
    public Object createMenu(HttpServletRequest httpRequest, @RequestBody BaseEcmcSysMenu menu) throws AppException {
        EayunResponseJson resultJson = new EayunResponseJson();
        try {
            menu.setCreatedBy(EcmcSessionUtil.getUser().getId());
            menuService.addSysMenu(menu);
            resultJson.setRespCode(ConstantClazz.SUCCESS_CODE);
            ecmcLogService.addLog(LOG_OPT_ADD, LOG_TYPE_MENU, menu.getName(), null, 1, menu.getId(), null);
            return resultJson;
        } catch (Exception e) {
            log.error(e.toString(), e);
            ecmcLogService.addLog(LOG_OPT_ADD, LOG_TYPE_MENU, menu.getName(), null, 0, menu.getId(), e);
            throw new AppException(e.toString(), e);
        }
    }

    /**
     * 修改菜单
     * @param menu
     * @return
     * @throws AppException
     */
    @RequestMapping("/modifymenu")
    @ResponseBody
    public Object modifyMenu(@RequestBody Map<String, Object> map) throws AppException {
        EayunResponseJson resultJson = new EayunResponseJson();
        try {
            BaseEcmcSysMenu menu = new BaseEcmcSysMenu();
            BeanUtils.mapToBean(menu, map);
            menuService.updateSysMenu(menu);
            resultJson.setRespCode(ConstantClazz.SUCCESS_CODE);
            ecmcLogService.addLog(LOG_OPT_MOD, LOG_TYPE_MENU, menu.getName(), null, 1, menu.getId(), null);
            return resultJson;
        } catch (Exception e) {
            log.error(e.toString(), e);
            ecmcLogService.addLog(LOG_OPT_MOD, LOG_TYPE_MENU, (String) map.get("name"), null, 0, (String) map.get("id"), e);
            throw new AppException(e.toString(), e);
        }
    }

    /**
     * 删除菜单
     * @param menuId
     * @return
     * @throws AppException
     */
    @RequestMapping("/deletemenu")
    @ResponseBody
    public Object deleteMenu(@RequestBody Map<String, String> params) throws AppException {
        EayunResponseJson resultJson = new EayunResponseJson();
        try {
            menuService.deleteSysMenu(params.get("menuId"));
            resultJson.setRespCode(ConstantClazz.SUCCESS_CODE);
            ecmcLogService.addLog(LOG_OPT_DEL, LOG_TYPE_MENU, null, null, 1, params.get("menuId"), null);
            return resultJson;
        } catch (Exception e) {
            log.error(e.toString(), e);
            ecmcLogService.addLog(LOG_OPT_DEL, LOG_TYPE_MENU, null, null, 0, params.get("menuId"), null);
            throw new AppException(e.getMessage(), e);
        }
    }

    /**
     * 判断是否可删除，是：true，否：false
     * @param params
     * @return
     * @throws AppException
     */
    @RequestMapping("/checkfordel")
    @ResponseBody
    public Object checkForDel(@RequestBody Map<String, String> params) throws AppException {
        EayunResponseJson resultJson = new EayunResponseJson();
        if (menuService.hasSubMenu(params.get("menuId"))) {
            resultJson.setData(false);
            resultJson.setMessage("存在子菜单，不可删除");
        } else if (menuService.existsAuths(params.get("menuId"))) {
            resultJson.setData(false);
            resultJson.setMessage("菜单下存在权限，请先删除权限再删除菜单");
        } else {
            resultJson.setData(true);
        }
        resultJson.setRespCode(ConstantClazz.SUCCESS_CODE);
        return resultJson;
    }

    /**
     * 查询单个菜单
     * @param params
     * @return
     * @throws AppException
     */
    @RequestMapping("/getmenubyid")
    @ResponseBody
    public Object getMenuById(@RequestBody Map<String, String> params) throws AppException {
        EayunResponseJson responseJson = new EayunResponseJson();
        responseJson.setData(menuService.getMenuById(params.get("menuId")));
        responseJson.setRespCode(ConstantClazz.SUCCESS_CODE);
        return responseJson;
    }
}

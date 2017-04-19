package com.eayun.ecmcrole.controller;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
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
import com.eayun.common.util.StringUtil;
import com.eayun.ecmcrole.model.EcmcSysRole;
import com.eayun.ecmcrole.service.EcmcSysRoleService;
import com.eayun.log.ecmcsevice.EcmcLogService;

/**
 * 角色控制
 * 
 * @author zengbo
 *
 */
@Controller
@RequestMapping("/ecmc/system/role")
@Scope("prototype")
public class EcmcSysRoleController {

    private static final Logger log              = LoggerFactory.getLogger(EcmcSysRoleController.class);

    private static final String LOG_TYPE_ROLE    = "角色";

    private static final String LOG_OPT_ADD      = "创建角色";

    private static final String LOG_OPT_MOD      = "编辑角色";

    private static final String LOG_OPT_DEL      = "删除角色";

    private static final String LOG_OPT_SET_AUTH = "配置权限";

    @Autowired
    private EcmcSysRoleService  ecmcSysRoleService;

    @Autowired
    private EcmcLogService      ecmcLogService;

    @RequestMapping(value = "/findrolelist")
    @ResponseBody
    public Object findRoleList() {
        log.info("查询角色列表");
        EayunResponseJson reJson = new EayunResponseJson();
        try {
            reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
            reJson.setData(ecmcSysRoleService.findAllRole());
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            reJson.setRespCode(ConstantClazz.ERROR_CODE);
        }
        return reJson;
    }

    @RequestMapping("/findroleselectlist")
    @ResponseBody
    public Object findRoleSelectList() throws AppException {
        log.info("查询角色下拉列表开始");
        EayunResponseJson reJson = new EayunResponseJson();
        reJson.setData(ecmcSysRoleService.findRoleSelectList());
        reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
        return reJson;
    }

    @RequestMapping(value = "/getroledetail")
    @ResponseBody
    public Object getRoleDetail(@RequestBody Map<String, Object> requstMap) {
        log.info("查询角色详细信息");
        EayunResponseJson reJson = new EayunResponseJson();
        reJson.setRespCode(ConstantClazz.ERROR_CODE);
        String roleId = MapUtils.getString(requstMap, "roleId");
        if (!StringUtil.isEmpty(roleId)) {
            reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
            reJson.setData(ecmcSysRoleService.findRoleById(roleId));
        }
        return reJson;
    }

    @RequestMapping(value = "/createrole")
    @ResponseBody
    public Object createRole(@RequestBody Map<String, Object> requestMap) {
        log.info("创建角色");
        EayunResponseJson reJson = new EayunResponseJson();
        reJson.setRespCode(ConstantClazz.ERROR_CODE);
        try {
            EcmcSysRole role = new EcmcSysRole();
            BeanUtils.mapToBean(role, requestMap);
            reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
            reJson.setData(ecmcSysRoleService.addRole(role));
            ecmcLogService.addLog(LOG_OPT_ADD, LOG_TYPE_ROLE, role.getName(), null, 1, role.getId(), null);
            return reJson;
        } catch (Exception e) {
            log.info("添加用户时发生异常：{}", e.getMessage());
            ecmcLogService.addLog(LOG_OPT_ADD, LOG_TYPE_ROLE, (String) requestMap.get("name"), null, 0, null, e);
            throw e;
        }
    }

    @RequestMapping(value = "/modifyrole")
    @ResponseBody
    public Object modifyRole(@RequestBody Map<String, Object> requestMap) {
        log.info("更改角色信息");
        EayunResponseJson reJson = new EayunResponseJson();
        reJson.setRespCode(ConstantClazz.ERROR_CODE);
        try {
            EcmcSysRole role = new EcmcSysRole();
            BeanUtils.mapToBean(role, requestMap);
            reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
            reJson.setData(ecmcSysRoleService.updateRole(role));
            ecmcLogService.addLog(LOG_OPT_MOD, LOG_TYPE_ROLE, role.getName(), null, 1, role.getId(), null);
        } catch (Exception e) {
            log.info("更新用户时发生异常：{}", e.getMessage());
            ecmcLogService.addLog(LOG_OPT_MOD, LOG_TYPE_ROLE, (String) requestMap.get("name"), null, 0, (String) requestMap.get("id"), e);
        }
        return reJson;
    }

    @RequestMapping(value = "/deleterole")
    @ResponseBody
    public Object deleteRole(@RequestBody Map<String, Object> requestMap) {
        log.info("删除角色");
        EayunResponseJson reJson = new EayunResponseJson();
        reJson.setRespCode(ConstantClazz.ERROR_CODE);
        String roleId = MapUtils.getString(requestMap, "roleId");
        if (StringUtil.isEmpty(roleId)) {
            reJson.setRespCode(ConstantClazz.ERROR_CODE);
            reJson.setMessage("参数：【角色ID】为空！");
            ecmcLogService.addLog(LOG_OPT_DEL, LOG_TYPE_ROLE, null, null, 0, roleId, null);
            return reJson;
        }
        //存在绑定的用户，不允许删除
        if (ecmcSysRoleService.hasUserByRoleId(roleId)) {
            reJson.setRespCode(ConstantClazz.ERROR_CODE);
            reJson.setMessage("角色已被用户使用，请解绑后操作");
            ecmcLogService.addLog(LOG_OPT_DEL, LOG_TYPE_ROLE, null, null, 0, roleId, null);
            return reJson;
        }
        ecmcSysRoleService.delRole(roleId);
        reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
        reJson.setMessage("删除角色成功！");
        ecmcLogService.addLog(LOG_OPT_DEL, LOG_TYPE_ROLE, null, null, 1, roleId, null);
        return reJson;
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/saveroleauth")
    @ResponseBody
    public Object updateRoleAuthAndMenu(@RequestBody Map<String, Object> requestMap) throws AppException {
        log.info("修改角色菜单和权限");
        EayunResponseJson reJson = new EayunResponseJson();
        reJson.setRespCode(ConstantClazz.ERROR_CODE);
        String roleId = MapUtils.getString(requestMap, "roleId");
        try {
            List<String> menuIds = (List<String>) MapUtils.getObject(requestMap, "menus");
            List<String> authorityIds = (List<String>) MapUtils.getObject(requestMap, "authorities");
            if (!StringUtil.isEmpty(roleId)) {
                ecmcSysRoleService.saveSysRoleMenuAndAuthority(roleId, menuIds, authorityIds);
                reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
                ecmcLogService.addLog(LOG_OPT_SET_AUTH, LOG_TYPE_ROLE, null, null, 1, roleId, null);
            } else {
                ecmcLogService.addLog(LOG_OPT_SET_AUTH, LOG_TYPE_ROLE, null, null, 0, roleId, null);
            }
            return reJson;
        } catch (Exception e) {
            log.info("保存角色菜单和权限时发生异常：{}", e.getMessage());
            ecmcLogService.addLog(LOG_OPT_SET_AUTH, LOG_TYPE_ROLE, null, null, 0, roleId, e);
            throw new AppException(e.getMessage(), e);
        }
    }

    /**
     * <pre>
     * 验证角色名称是否重复
     * 名称重复：true
     * 名称不重复：false
     * </pre>
     * @param params name 角色名称; id 角色id
     * @return
     * @throws AppException
     */
    @RequestMapping("/checkrolename")
    @ResponseBody
    public Object checkRoleName(@RequestBody Map<String, String> params) throws AppException {
        log.info("验证角色名称是否重复开始");
        EayunResponseJson responseJson = new EayunResponseJson();
        responseJson.setData(ecmcSysRoleService.checkRoleName(params.get("name"), params.get("id")));
        responseJson.setRespCode(ConstantClazz.SUCCESS_CODE);
        return responseJson;
    }

}

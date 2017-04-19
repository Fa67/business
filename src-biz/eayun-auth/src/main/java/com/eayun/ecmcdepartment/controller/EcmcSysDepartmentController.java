package com.eayun.ecmcdepartment.controller;

import java.io.UnsupportedEncodingException;
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
import com.eayun.ecmcdepartment.model.EcmcSysDepartment;
import com.eayun.ecmcdepartment.service.EcmcSysDepartmentService;
import com.eayun.ecmcuser.model.EcmcSysUser;
import com.eayun.ecmcuser.service.EcmcSysUserService;
import com.eayun.log.ecmcsevice.EcmcLogService;

/**
 * 机构管理
 * 
 * @author zengbo
 *
 */
@Controller
@RequestMapping("/ecmc/system/depart")
@Scope("prototype")
public class EcmcSysDepartmentController {

    private static final Logger      log             = LoggerFactory.getLogger(EcmcSysDepartmentController.class);

    private static final String      LOG_TYPE_DEPART = "组织机构";

    private static final String      LOG_OPT_ADD     = "创建机构";

    private static final String      LOG_OPT_MOD     = "编辑机构";

    private static final String      LOG_OPT_DEL     = "删除机构";

    @Autowired
    private EcmcSysDepartmentService departmentService;

    @Autowired
    private EcmcSysUserService       userService;

    @Autowired
    private EcmcLogService           ecmcLogService;

    @RequestMapping(value = "/getdeparttreegrid")
    @ResponseBody
    public Object getDepartTreeGrid() {
        log.info("查询所有机构的列表");
        EayunResponseJson reJson = new EayunResponseJson();
        try {
            reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
            reJson.setData(departmentService.findAllDepartmentTreeGrid());
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            reJson.setRespCode(ConstantClazz.ERROR_CODE);
        }
        return reJson;
    }

    @RequestMapping(value = "/getdeparttree")
    @ResponseBody
    public Object getDepartTree() {
        log.info("查询所有机构（树形结构）");
        EayunResponseJson reJson = new EayunResponseJson();
        try {
            reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
            reJson.setData(departmentService.findAllDepartmentTree());
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            reJson.setRespCode(ConstantClazz.ERROR_CODE);
        }
        return reJson;
    }

    @RequestMapping(value = "/getdepartbyid")
    @ResponseBody
    public Object getDepartById(@RequestBody Map<String, Object> requestMap) {
        log.info("根据ID查询机构");
        EayunResponseJson reJson = new EayunResponseJson();
        reJson.setRespCode(ConstantClazz.ERROR_CODE);
        String id = MapUtils.getString(requestMap, "id");
        if (!StringUtil.isEmpty(id)) {
            reJson.setRespCode(ConstantClazz.ERROR_CODE);
            reJson.setData(departmentService.findDepartmentById(id));
        }
        return reJson;
    }

    @RequestMapping(value = "/createdepart")
    @ResponseBody
    public Object createDepart(@RequestBody Map<String, Object> requestMap) throws AppException {
        log.info("创建机构");
        EayunResponseJson reJson = new EayunResponseJson();
        EcmcSysDepartment department = new EcmcSysDepartment();
        BeanUtils.mapToBean(department, requestMap);
        try {
            department = departmentService.addDepartment(department);
            reJson.setRespCode(department == null || department.getId() == null ? ConstantClazz.ERROR_CODE : ConstantClazz.SUCCESS_CODE);
            if (department != null && department.getId() != null) {
                ecmcLogService.addLog(LOG_OPT_ADD, LOG_TYPE_DEPART, department.getName(), null, 1, department.getId(), null);
            } else {
                ecmcLogService.addLog(LOG_OPT_ADD, LOG_TYPE_DEPART, null, null, 0, null, null);
            }
        } catch (Exception e) {
            ecmcLogService.addLog(LOG_OPT_ADD, LOG_TYPE_DEPART, department.getName(), null, 0, null, e);
            throw e;
        }
        return reJson;
    }

    @RequestMapping(value = "/modifydepart")
    @ResponseBody
    public Object modifyDepart(@RequestBody Map<String, Object> requestMap) throws AppException {
        log.info("修改机构");
        EayunResponseJson reJson = new EayunResponseJson();
        EcmcSysDepartment department = new EcmcSysDepartment();
        BeanUtils.mapToBean(department, requestMap);
        try {
            department = departmentService.updateDepartment(department);
            if (department != null && department.getId() != null) {
                reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
                ecmcLogService.addLog(LOG_OPT_MOD, LOG_TYPE_DEPART, department.getName(), null, 1, department.getId(), null);
            } else {
                reJson.setRespCode(ConstantClazz.ERROR_CODE);
                ecmcLogService.addLog(LOG_OPT_MOD, LOG_TYPE_DEPART, null, null, 0, null, null);
            }
        } catch (Exception e) {
            ecmcLogService.addLog(LOG_OPT_MOD, LOG_TYPE_DEPART, department != null ? department.getName() : null, department != null ? department.getId() : null, 0, null, e);
            throw e;
        }
        return reJson;
    }

    @RequestMapping(value = "/checkdepartname")
    @ResponseBody
    public Object checkDepartName(@RequestBody Map<String, Object> requestMap) throws AppException, UnsupportedEncodingException {
        log.info("校验机构名称");
        EayunResponseJson reJson = new EayunResponseJson();
        reJson.setRespCode(ConstantClazz.ERROR_CODE);
        String departName = MapUtils.getString(requestMap, "departName");
        if (!StringUtil.isEmpty(departName) && departmentService.checkDepartmentName(departName)) {
            reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
        }
        return reJson;
    }

    /**
     * 验证机构编号，重复返回：true，不重复返回false
     * @return
     * @throws AppException
     */
    @RequestMapping(value = "/checkdepartcode")
    @ResponseBody
    public Object checkDepartCode(@RequestBody Map<String, String> params) throws AppException {
        EayunResponseJson responseJson = new EayunResponseJson();
        responseJson.setData(departmentService.checkDepartCode(params.get("code"), params.get("id")));
        responseJson.setRespCode(ConstantClazz.SUCCESS_CODE);
        return responseJson;
    }

    @RequestMapping(value = "/deldepart")
    @ResponseBody
    public Object delDepartment(@RequestBody Map<String, Object> requestMap) {
        log.info("删除机构");
        String departmentId = MapUtils.getString(requestMap, "departmentId");
        EayunResponseJson reJson = new EayunResponseJson();
        reJson.setRespCode(ConstantClazz.ERROR_CODE);
        try {
            if (!StringUtil.isEmpty(departmentId)) {
                boolean hasChildren = departmentService.hasChildren(departmentId);
                boolean hasUser = false;
                List<EcmcSysUser> users = userService.findUserByDepartmentId(departmentId);
                if (users != null && users.size() > 0) {
                    hasUser = true;
                }
                if (hasChildren || hasUser) {
                    reJson.setRespCode(ConstantClazz.ERROR_CODE);
                    reJson.setMessage("该机构下存在" + (hasChildren ? "【子机构】" : "") + (hasUser ? "【用户】" : "") + "，不允许删除！");
                    ecmcLogService.addLog(LOG_OPT_DEL, LOG_TYPE_DEPART, null, null, 0, departmentId, null);
                    return reJson;
                }
                departmentService.delDepartment(departmentId);
                reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
                reJson.setMessage("删除机构成功！");
                ecmcLogService.addLog(LOG_OPT_DEL, LOG_TYPE_DEPART, null, null, 1, departmentId, null);
                return reJson;
            }
            reJson.setRespCode(ConstantClazz.ERROR_CODE);
            reJson.setMessage("参数：【机构ID】为空！");
            ecmcLogService.addLog(LOG_OPT_DEL, LOG_TYPE_DEPART, null, null, 0, departmentId, null);
        } catch (Exception e) {
            ecmcLogService.addLog(LOG_OPT_DEL, LOG_TYPE_DEPART, null, null, 0, departmentId, e);
            throw e;
        }
        return reJson;
    }

}

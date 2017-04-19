package com.eayun.customer.controller;

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
import com.eayun.customer.model.Role;
import com.eayun.customer.serivce.RoleService;
/**
 * 角色管理             
 * @Filename: RoleController.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2015年10月20日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Controller
@RequestMapping("/sys/role")
@Scope("prototype")
public class RoleController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(RoleController.class);
    
    @Autowired
    private RoleService roleService;
    

    /**
     * @param request
     * @param roleName
     * @param roleId
     * @return
     */
    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/checkRoleName" , method = RequestMethod.POST)
    @ResponseBody
    public boolean checkRoleName(HttpServletRequest request, @RequestBody Map map) {
        log.info("验证角色名重复开始");
        String roleName = map.get("roleName").toString();
        String roleId = map.get("roleId").toString();
        SessionUserInfo sessionUserInfo = (SessionUserInfo) request.getSession().getAttribute(
            ConstantClazz.SYS_SESSION_USERINFO);
        return roleService.checkRoleName(sessionUserInfo.getCusId(), roleName, roleId);
    }

    /**
     * 查询角色
     * @param request
     * @param role
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getListByCustomer" , method = RequestMethod.POST)
    @ResponseBody
    public String getListByCustomer(HttpServletRequest request) {
        log.info("得到当前客户下角色列表开始");
        SessionUserInfo sessionUserInfo = (SessionUserInfo) request.getSession().getAttribute(
            ConstantClazz.SYS_SESSION_USERINFO);
        List<Role> roleList = roleService.getListByCustomer(sessionUserInfo.getCusId());
        return JSONObject.toJSONString(roleList);
    }

    /**
     * 根据roleId查询角色
     * @param request
     * @param role
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/findRoleById" , method = RequestMethod.POST)
    @ResponseBody
    public String findRoleById(HttpServletRequest request, @RequestBody Map map) {
        log.info("根据id得到角色信息开始");
        String roleId = map.get("roleId").toString();
        Role role = roleService.findRoleById(roleId);
        return JSONObject.toJSONString(role);
    }

    /**
     * 添加角色
     * @param request
     * @param role
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/addRole" , method = RequestMethod.POST)
    @ResponseBody
    public String addRole(HttpServletRequest request, @RequestBody Role role) {
        log.info("添加角色开始");
        try {
            SessionUserInfo sessionUserInfo = (SessionUserInfo) request.getSession().getAttribute(
                ConstantClazz.SYS_SESSION_USERINFO);
            role.setCusId(sessionUserInfo.getCusId());
            role = roleService.addRole(role);
        } catch (Exception e) {
            log.error("添加角色失败", e);
            throw e;
        }
        return JSONObject.toJSONString(role);
    }

    /**
     * 修改角色
     * @param request
     * @param role
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/updateRole" , method = RequestMethod.POST)
    @ResponseBody
    public String updateRole(HttpServletRequest request, @RequestBody Role role) {
        log.info("修改角色开始");
        try {
            role = roleService.updateRole(role);
        } catch (Exception e) {
            log.error("修改角色失败", e);
            throw e;
        }
        return JSONObject.toJSONString(role);
    }

    /**
     * 删除角色
     * @param request
     * @param role
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/deleteRole" , method = RequestMethod.POST)
    @ResponseBody
    public String deleteRole(HttpServletRequest request , @RequestBody Map map) {
        log.info("删除角色开始");
        try {
            String roleId = map.get("roleId").toString();
            roleService.deleteRole(roleId);
        } catch (Exception e) {
            log.error("删除角色失败", e);
            throw e;
        }
        return JSONObject.toJSONString("success");
    }
}

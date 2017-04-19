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
import com.eayun.common.controller.BaseController;
import com.eayun.common.exception.AppException;
import com.eayun.customer.model.RolePower;
import com.eayun.customer.serivce.RolePowerService;

@Controller
@RequestMapping("/sys/rolePower")
@Scope("prototype")
public class RolePowerController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(RolePowerController.class);

    @Autowired
    private RolePowerService    rolePowerService;
    

    /**
     * 查询角色权限
     * @param request
     * @param rolePower
     * @return
     * @throws AppException
     */
    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/getListByRole" , method = RequestMethod.POST)
    @ResponseBody
    public String getListByRole(HttpServletRequest request , @RequestBody Map map) {
        log.info("得到指定角色下权限列表");
        String roleId = map.get("roleId").toString();
        List<RolePower> rolePowerList = rolePowerService.getListByRole(roleId);
        return JSONObject.toJSONString(rolePowerList);
    }

    /**
     * 设置角色权限
     * @param request
     * @param rolePower
     * @return
     * @throws AppException
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @RequestMapping(value = "/setRolePower" , method = RequestMethod.POST)
    @ResponseBody
    public String setRolePower(HttpServletRequest request , @RequestBody Map map) {
        log.info("设置角色权限");
        String roleId = map.get("roleId").toString();
        List<String> powerIds = (List<String>)map.get("powerIds");
        try {
            rolePowerService.setRolePower(roleId, powerIds);
        } catch (Exception e) {
            log.error("设置角色权限失败", e);
            throw e;
        }
        return JSONObject.toJSONString("success");
    }
}

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
import com.eayun.customer.model.Power;
import com.eayun.customer.serivce.PowerService;

@Controller
@RequestMapping("/sys/power")
@Scope("prototype")
public class PowerController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(PowerController.class);
    
    @Autowired
    private PowerService powerService;

    /**
     * 添加权限模块
     * @param request
     * @param power
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/addPower" , method = RequestMethod.POST)
    @ResponseBody
    public String addPower(HttpServletRequest request, @RequestBody Power power) throws Exception {
        power = powerService.addPower(power);
        return JSONObject.toJSONString(power);
    }

    /**
     * 修改权限模块
     * @param request
     * @param power
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/updatePower" , method = RequestMethod.POST)
    @ResponseBody
    public String updatePower(HttpServletRequest request, @RequestBody Power power) throws Exception {
        power = powerService.updatePower(power);
        return JSONObject.toJSONString(power);
    }

    /**
     * 删除权限模块
     * @param request
     * @param power
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/deletePower" , method = RequestMethod.POST)
    @ResponseBody
    public boolean deletePower(HttpServletRequest request , @RequestBody Map map) throws Exception {
        String powerId = map.get("powerId").toString();
        boolean b = powerService.deletePower(powerId);
        return b;
    }

    /**
     * 根据权限id查询权限模块
     * @param request
     * @param power
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/findPowerById" , method = RequestMethod.POST)
    @ResponseBody
    public String findPowerById(HttpServletRequest request , @RequestBody Map map) throws Exception {
        String powerId = map.get("powerId").toString();
        Power power = powerService.findPowerById(powerId);
        return JSONObject.toJSONString(power);
    }

    /**
     * 查询所有权限模块
     * @param request
     * @param power
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getListByPower" , method = RequestMethod.POST)
    @ResponseBody
    public String getListByPower(HttpServletRequest request, @RequestBody Power power) throws Exception {
        List<Power> powerList = null;
        try {
            powerList = powerService.getListByPower(power);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            throw e;
        }
        return JSONObject.toJSONString(powerList);
    }
    /**
     * 获取权限下级目录
     * @param request
     * @param map
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/getChildrenList" , method = RequestMethod.POST)
    @ResponseBody
    public String getChildrenList(HttpServletRequest request , @RequestBody Map map) throws Exception {
        log.info("获取权限下级目录开始");
        List<Power> powerList = null;
        String powerId = (null == map.get("powerId"))?null:map.get("powerId").toString();
        powerList = powerService.getChildrenList(powerId);
        return JSONObject.toJSONString(powerList);
    }
    
    /**
     * 返回角色所拥有的的权限标示
     * @param request
     * @param map
     * @return
     */
    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/getRoutesByRole" , method = RequestMethod.POST)
    @ResponseBody
    public String getRoutesByRole(HttpServletRequest request , @RequestBody Map map) {
        log.info("查询角色所拥有的的权限标示");
        String roleId = map.get("roleId").toString();
        List<String> routes = powerService.havePowerRoutes(roleId);
        return JSONObject.toJSONString(routes);
    }
    /**
     * 检验权限标示，不可重复
     * @param request
     * @param map
     * @return
     */
    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/checkRouteName" , method = RequestMethod.POST)
    @ResponseBody
    public String checkRouteName(HttpServletRequest request , @RequestBody Map map) {
        log.info("检验权限标示，不可重复");
        String powerRoute = map.get("powerRoute").toString();
        String powerId = map.get("powerId").toString();
        boolean isRight = powerService.checkByRouteName(powerRoute,powerId);
        return JSONObject.toJSONString(isRight);
    }
}

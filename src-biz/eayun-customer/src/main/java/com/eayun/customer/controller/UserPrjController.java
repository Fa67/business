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
import com.eayun.customer.model.UserPrj;
import com.eayun.log.service.LogService;
import com.eayun.project.service.UserPrjService;
/**
 * 用户-项目关联管理
 * @Filename: UserPrjController.java
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
@RequestMapping("/sys/userPrj")
@Scope("prototype")
public class UserPrjController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(UserPrjController.class);
    
    @Autowired
    private UserPrjService userPrjService;
    
    @Autowired
	private LogService logService;
    

    /**
     * 给用户设置项目
     * 
     * @param request
     * @param projectIds
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @RequestMapping(value = "/setUserProjects" , method = RequestMethod.POST)
    @ResponseBody
    public String setUserProjects(HttpServletRequest request , @RequestBody Map map) {
        log.info("给用户设置项目开始");
        String userId = map.get("userId").toString();
        String userAccount = map.get("userAccount").toString();
        List<String> projectIds = (List<String>)map.get("projectIds");
        try {
            userPrjService.setUserProjects(userId, projectIds);
            logService.addLog("管理数据中心", "用户管理", userAccount, null,ConstantClazz.LOG_STATU_ERROR, null);
        } catch (Exception e) {
            log.error("用户设置项目失败", e);
            logService.addLog("管理数据中心", "用户管理", userAccount, null,ConstantClazz.LOG_STATU_ERROR, e);
            throw e;
        }
        return JSONObject.toJSONString("success");
    }

    /**
     * 得到用户项目列表
     * 
     * @param request
     * @param userPrj
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/getListByUser" , method = RequestMethod.POST)
    @ResponseBody
    public String getListByUser(HttpServletRequest request , @RequestBody Map map) {
        log.info("得到指定用户下的项目列表开始");
        String userId = map.get("userId").toString();
        List<UserPrj> userPrjList = userPrjService.getListByUserId(userId);
        return JSONObject.toJSONString(userPrjList);
    }
}

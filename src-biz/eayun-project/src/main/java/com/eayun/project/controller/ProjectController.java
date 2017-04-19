package com.eayun.project.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.controller.BaseController;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.project.service.ProjectService;
import com.eayun.virtualization.model.BaseCloudProject;
import com.eayun.virtualization.model.CloudProject;
/**
 * 项目管理   
 * @Filename: ProjectController.java
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
@RequestMapping("/cloud/project")
@Scope("prototype")
public class ProjectController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    private ProjectService      projectService;

    /**
     * 根据客户得到项目列表
     * @param request
     * @param customer
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getListByCustomer" , method = RequestMethod.POST)
    @ResponseBody
    public String getListByCustomer(HttpServletRequest request) {
        log.info("根据所属客户查询项目列表开始");
        String cusId = "";
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        if(!StringUtils.isEmpty(sessionUser)){
        	 cusId = sessionUser.getCusId();
        }
        List<CloudProject> list = projectService.getProjectListByCustomer(cusId);
        return JSONObject.toJSONString(list);
    }
    /**
     * 根据客户得到项目列表,将当前的项目放到首位。
     * @param request
     * @param customer
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getListByCustomerAndPrjId" , method = RequestMethod.POST)
    @ResponseBody
    public String getListByCustomerAndPrjId(HttpServletRequest request,@RequestBody Map map) {
        log.info("根据所属客户查询项目列表开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String cusId = sessionUser.getCusId();
        String prjId = map.get("projectId").toString();
        List<BaseCloudProject> list = projectService.getListByCustomerAndPrjId(cusId,prjId);
        return JSONObject.toJSONString(list);
    }
    /**
     * 根据id查询指定项目
     * @param request
     * @param prjId
     * @return
     */
    @RequestMapping(value = "/findProjectByPrjId" , method = RequestMethod.POST)
    @ResponseBody
    public String findProjectByPrjId(HttpServletRequest request,@RequestBody String prjId){
    	CloudProject prj = projectService.findProject(prjId);
    	return JSONObject.toJSONString(prj);
    }
    /**
     * 当前客户下一个数据中心下有没有项目
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value = "/findProByDcId" , method = RequestMethod.POST)
    @ResponseBody
    public String findProByDcId(HttpServletRequest request,@RequestBody Map map){
        log.info("检查当前客户下一个数据中心下有没有项目");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String cusId = sessionUser.getCusId();
        
        String dcId = map.get("dcId").toString();
        boolean havePro = projectService.findProByDcId(cusId , dcId);
        return JSONObject.toJSONString(havePro);
    }
}

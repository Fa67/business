package com.eayun.virtualization.controller;

import java.util.List;

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
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.service.OverviewService;
@Controller
@RequestMapping("/sys/overview")
@Scope("prototype")
public class OverViewController extends BaseController{
	
	private static final Logger log = LoggerFactory.getLogger(OverViewController.class);
	
    @Autowired
    private OverviewService overviewService;
    /**
     * 统计项目配额是已使用量
     * @param request
     * @param prjId
     * @return
     */
    @RequestMapping(value = "/getStatisticsByPrjId", method = RequestMethod.POST)
    @ResponseBody
    public String getStatisticsByPrjId(HttpServletRequest request,@RequestBody String prjId){
    	log.info("查询项目配额使用量");
        CloudProject cloudProject=overviewService.findStatisticsByPrjId(prjId);
        return JSONObject.toJSONString(cloudProject);
    }
    /**
     * 获取该登录客户已创建有项目，且登录用户有权限的数据中心列表
     * @param request
     * @return
     */
    @RequestMapping(value = "/getvaliddclist", method = RequestMethod.POST)
    @ResponseBody
    public String getValidDcList(HttpServletRequest request){
    	log.info("获取该登录客户已创建有项目，且登录用户有权限的数据中心列表");
    	SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().
    			getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    	EayunResponseJson json = new EayunResponseJson();
        try {
			List<CloudProject> list=overviewService.getValidDcList(sessionUser);
			json.setData(list);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
		    log.error(e.toString(),e);
			json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.toString(),e);
			log.error(e.getMessage());
		}
        return JSONObject.toJSONString(json);
    }
    /**
     * 获取登录客户即将到期的资源列表
     * @param request
     * @param page
     * @param map
     * @return
     */
    @RequestMapping(value = "/gettoexpireresources", method = RequestMethod.POST)
    @ResponseBody
    public String getToExpireResources(HttpServletRequest request, Page page, @RequestBody ParamsMap map){
    	log.info("获取登陆客户即将到期的资源列表");
    	SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String cusId = sessionUser.getCusId();
        String prjId = null == map.getParams().get("prjId")?"":map.getParams().get("prjId").toString();
        
        int pageSize = map.getPageSize();
        pageSize = 5;
        int pageNumber = map.getPageNumber();
        
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        try {
			page = overviewService.getToExpireResources(page,queryMap,cusId,prjId);
		} catch (Exception e) {
			log.error(e.toString(),e);
		}
        return JSONObject.toJSONString(page);
    }
    
    @RequestMapping(value = "/gettopayorderpage", method = RequestMethod.POST)
    @ResponseBody
    public String getToPayOrderPage(HttpServletRequest request, Page page, @RequestBody ParamsMap map){
    	log.info("获取客户待支付订单列表");
    	SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String cusId = sessionUser.getCusId();
        
        int pageSize = map.getPageSize();
        pageSize = 5;
        int pageNumber = map.getPageNumber();
        
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        try {
			page = overviewService.getToPayOrderPage(page,queryMap,cusId);
		} catch (Exception e) {
			log.error(e.toString(),e);
		}
        return JSONObject.toJSONString(page);
    }
}

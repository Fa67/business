package com.eayun.virtualization.controller;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.util.DateUtil;
import com.eayun.virtualization.bean.CloudTypes;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.service.StatisticsService;

/**
 * 资源统计                 
 * @Filename: StatisticsController.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2015年11月9日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Controller
@RequestMapping("/statistics")
@Scope("prototype")
public class StatisticsController{

    private static final Logger log = LoggerFactory.getLogger(StatisticsController.class);
    
    @Autowired
    private StatisticsService statisticsService;
    
    
    @RequestMapping(value = "/getVmResources", method = RequestMethod.POST)
    @ResponseBody
    public String getVmResources(HttpServletRequest request, Page page, @RequestBody ParamsMap map) throws Exception{
        log.info("获取云主机资源列表");
        String dcId = map.getParams().get("dcId").toString();
        String start = map.getParams().get("startTime").toString();
        String end = map.getParams().get("endTime").toString();
        String sort = map.getParams().get("sort").toString();		//DESC、ASC
        String orderBy = map.getParams().get("orderBy").toString();	//CPU核数，内存大小，开始时间，累计时长
        
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String cusId = sessionUser.getCusId();
        
        
        Date startTime = DateUtil.timestampToDate(start);
        Date endTime = DateUtil.timestampToDate(end);
        int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
        
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        
        page = statisticsService.getCloudVmResources(page,dcId, cusId,startTime, endTime , sort , orderBy,queryMap);
        return JSONObject.toJSONString(page);
    }
    @RequestMapping(value = "/getVolumeResources", method = RequestMethod.POST)
    @ResponseBody
    public String getVolumeResources(HttpServletRequest request, Page page, @RequestBody ParamsMap map) throws Exception{
        log.info("获取云硬盘资源列表");
        String dcId = map.getParams().get("dcId").toString();
        String start = map.getParams().get("startTime").toString();
        String end = map.getParams().get("endTime").toString();
        String sort = map.getParams().get("sort").toString();		//DESC、ASC
        String orderBy = map.getParams().get("orderBy").toString();	//硬盘容量，开始时间，累计时长
        
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String cusId = sessionUser.getCusId();
        
        Date startTime = DateUtil.timestampToDate(start);
        Date endTime = DateUtil.timestampToDate(end);
        int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
        
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        
        page = statisticsService.getCloudVolumeResources(page,dcId, cusId,startTime, endTime , sort , orderBy,queryMap);
        return JSONObject.toJSONString(page);
    }
    @RequestMapping(value = "/getnetResources", method = RequestMethod.POST)
    @ResponseBody
    public String getnetResources(HttpServletRequest request, @RequestBody Map map) throws Exception{
        log.info("获取网络流量统计资源列表");
        String projectId = map.get("projectId").toString();
        String start = map.get("startTime").toString();
        String end = map.get("endTime").toString();
        Date startTime = DateUtil.timestampToDate(start);
        Date endTime = DateUtil.timestampToDate(end);
        
        CloudTypes type = statisticsService.getNet(projectId, startTime, endTime);
        return JSONObject.toJSONString(type);
    }
    @RequestMapping(value = "/getResourcesForExcel", method = RequestMethod.POST)
    @ResponseBody
    public String getResourcesForExcel(HttpServletRequest request, @RequestBody Map map) throws Exception{
        String projectId = map.get("projectId").toString();
        String dcId = map.get("dcId").toString();
        String start = map.get("startTime").toString();
        String end = map.get("endTime").toString();
        Date startTime = DateUtil.timestampToDate(start);
        Date endTime = DateUtil.timestampToDate(end);
        
        boolean isok = statisticsService.getResourcesForExcel(dcId,projectId, startTime, endTime);
        return JSONObject.toJSONString(isok);
    }
    /**
     * 导出excel
     */
    @RequestMapping("/createExcel")
    public void createExcel(HttpServletRequest request, HttpServletResponse response , String dcId , String projectId , 
                              String startTime , String endTime ,String browser , String orderBy ,String sort , 
                              String orderByVol ,String sortVol) throws Exception {
        Date start = DateUtil.timestampToDate(startTime);
        Date end = DateUtil.timestampToDate(endTime);
        String dcName = statisticsService.getDcNameById(dcId);
        
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String cusId = sessionUser.getCusId();
        
        String fileName = "";
        if("Firefox".equals(browser)){
            fileName = new String((dcName+"_资源统计报表.xls").getBytes(), "iso-8859-1");
        }else{
            fileName = URLEncoder.encode(dcName+"_资源统计报表.xls", "UTF-8") ;
        }
        
        response.setContentType("application/vnd.ms-excel");
        response.addHeader("Content-Disposition", "attachment;filename=" + fileName);
        try {
            statisticsService.exportSheets(response.getOutputStream(),dcId,cusId,projectId,start,end,sort,orderBy,sortVol,orderByVol);
        } catch (Exception e) {
            log.error("导出excel失败", e);
            throw e;
        }
    }
}

package com.eayun.log.controller;


import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.eayun.common.tools.ExportDataToExcel;
import com.eayun.common.util.DateUtil;
import com.eayun.log.bean.ExcelLog;
import com.eayun.log.model.SysLog;
import com.eayun.log.service.LogService;

@Controller
@RequestMapping("/sys/log")
public class LogController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(LogController.class);
    @Autowired
    private LogService          logService;

    /**
     * 操作日志查询的列表页面
     * 
     * @param request
     * @return
     */
    @RequestMapping(value = "/getLogList" , method = RequestMethod.POST)
    @ResponseBody
    public String getLogList(HttpServletRequest request, Page page, @RequestBody ParamsMap map)
                                                                                             throws Exception {
        log.info("获取日志列表开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String cusId = sessionUser.getCusId();
        
        String actItem = map.getParams().get("actItem").toString();
        String statu = map.getParams().get("statu").toString();
        String operator = map.getParams().get("operator").toString();
        String prjId = map.getParams().get("prjId").toString();
        String begin = map.getParams().get("beginTime").toString();
        String end = map.getParams().get("endTime").toString();
        //String cusId = map.getParams().get("cusId").toString();
        String resourceType = map.getParams().get("resourceType") == null ? null : map.getParams().get("resourceType").toString();
        String resourceName = map.getParams().get("resourceName") == null ? null : map.getParams().get("resourceName").toString();
        String ip = map.getParams().get("ip") == null ? null : map.getParams().get("ip").toString();
        
        Date beginTime = DateUtil.timestampToDate(begin);
        Date endTime = DateUtil.timestampToDate(end);
        int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
        
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        page=logService.getLogListMongo(page, beginTime, endTime, actItem, statu, prjId, cusId , resourceType,resourceName,ip ,operator, queryMap);
        return JSONObject.toJSONString(page);
    }
    /**
     * 导出excel
     */
    @RequestMapping("/createExcel")
    public String createExcel(HttpServletRequest request, HttpServletResponse response , String actItem , String statu , String prjId, 
                              String begin, String end ,String browser) throws Exception {
        log.info("导出Excel开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String cusId = sessionUser.getCusId();
        Date beginTime = DateUtil.timestampToDate(begin);
        Date endTime = DateUtil.timestampToDate(end);
        
        Properties props=System.getProperties();
        String os = props.getProperty("os.name").toLowerCase();
        if(os.indexOf("windows") !=-1){
        	actItem=new String(actItem.getBytes("ISO-8859-1"),"UTF-8");
        }else{
            actItem=new String(actItem.getBytes("ISO-8859-1"),"UTF-8");
        }
        try {
            List<ExcelLog> list = logService.queryLogExcelFromMongo(beginTime, endTime, actItem, statu, prjId, cusId);
            ExportDataToExcel<ExcelLog> excel = new ExportDataToExcel<ExcelLog>();
            response.setContentType("application/vnd.ms-excel");
            
            String fileName = "";
            if("Firefox".equals(browser)){
                fileName = new String("操作日志.xls".getBytes(), "iso-8859-1");
            }else{
                fileName = URLEncoder.encode("操作日志.xls", "UTF-8") ;
            }
            response.addHeader("Content-Disposition", "attachment;filename=" + fileName);
            if(list.size()==0){
            	ExcelLog msgExcel = new ExcelLog();
				list.add(msgExcel);
			}
            excel.exportData(list, response.getOutputStream(), "操作日志");
        } catch (Exception e) {
            log.error("导出日志excel失败", e);
            throw e;
        }
        return null;
    }
    
    /**
     * 获取客户最新的5条日志用于总览页显示
     * @param request
     * @param map
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/getlastlogs" , method = RequestMethod.POST)
    @ResponseBody
    public String getLastLogs(HttpServletRequest request, Map map) throws Exception {
    	log.info("获取登录客户最新8条日志");
    	EayunResponseJson json = new EayunResponseJson();
    	SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    	List<SysLog> logList = new ArrayList<SysLog>();
    	try {
			logList = logService.getLastLogs(sessionUser);
			json.setData(logList);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
		}
    	return JSONObject.toJSONString(json);
    }
}

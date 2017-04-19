package com.eayun.monitor.ecmccontroller;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.util.DateUtil;
import com.eayun.monitor.bean.EcmcVmIndicator;
import com.eayun.monitor.ecmcservice.EcmcInstanceMonitorService;

@Controller
@RequestMapping("/ecmc/monitor/instance")
public class EcmcInstanceMonitorController {

	private static final Logger log = LoggerFactory.getLogger(EcmcInstanceMonitorController.class);
	
	@Autowired
    private EcmcInstanceMonitorService ecmcInstanceMonitorService;
	
	
	@RequestMapping("/getinstancelistforecmclive")
    @ResponseBody
	public String getInstanceListforEcmcLive(HttpServletRequest request , Page page , @RequestBody ParamsMap map){
		log.info("获取数据库实例资源监控实时指标数据信息");
		String queryType = map.getParams().get("queryType").toString();
        String queryName = map.getParams().get("queryName").toString();
    	String orderBy = map.getParams().get("orderBy").toString();
        String sort = map.getParams().get("sort").toString();
        String dcName = map.getParams().get("dcName").toString();
        String versionId = map.getParams().get("versionId").toString();
        int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
        
        pageSize = 20;
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        
        page = ecmcInstanceMonitorService.getInstanceListforEcmcLive(page, queryMap, queryType, queryName , dcName,orderBy,sort,versionId);
		return JSONObject.toJSONString(page);
	}
	
	@RequestMapping("/getinstancelistforecmclast")
    @ResponseBody
	public String getInstanceListforEcmcLast(HttpServletRequest request , Page page , @RequestBody ParamsMap map){
		log.info("获取数据库实例资源监控历史指标数据信息开始");
		String queryType = map.getParams().get("queryType").toString();
        String queryName = map.getParams().get("queryName").toString();
        String end = map.getParams().get("endDate").toString();
        String interval = map.getParams().get("interval").toString();
        String orderBy = map.getParams().get("orderBy").toString();
        String sort = map.getParams().get("sort").toString();
        String dcName = map.getParams().get("dcName").toString();
        String versionId = map.getParams().get("versionId").toString();
        
        int mins = Integer.parseInt(interval);
        Date endDate = DateUtil.timestampToDate(end);
        
        int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
        
        pageSize = 20;
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        page = ecmcInstanceMonitorService.getInstanceListforEcmcLast(page, queryMap, queryType, queryName, endDate, mins, orderBy, sort , dcName,versionId);
		return JSONObject.toJSONString(page);
	}
	
	@RequestMapping("/getinstancedetailbyid")
    @ResponseBody
	public String getInstancedetailById(HttpServletRequest request , @RequestBody Map map){
		log.info("资源监控查询数据库实例详情开始");
		JSONObject json = new JSONObject();
		String instanceId = map.get("instanceId").toString();
        try {
			EcmcVmIndicator instance = ecmcInstanceMonitorService.getInstancedetailById(instanceId);
			json.put("data", instance);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
		}
		return json.toJSONString();
	}
}

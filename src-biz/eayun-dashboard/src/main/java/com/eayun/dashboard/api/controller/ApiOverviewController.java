package com.eayun.dashboard.api.controller;

import java.util.Date;
import java.util.List;
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
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.util.DateUtil;
import com.eayun.customer.model.Customer;
import com.eayun.dashboard.api.bean.ApiIndexDetail;
import com.eayun.dashboard.api.service.ApiOverviewService;

@Controller
@RequestMapping("/ecmc/api/overview")
public class ApiOverviewController {

	private static final Logger log = LoggerFactory.getLogger(ApiOverviewController.class);
	
	@Autowired
    private ApiOverviewService apiOverviewService;
	
	@RequestMapping("/getcuslistbyorg")
    @ResponseBody
    public String getCusListByOrg (HttpServletRequest request , Page page , @RequestBody Map map) {
        log.info("查询已通过审核已创建的客户列表");
        String cusOrg = null==map.get("cusOrg")?"":map.get("cusOrg").toString();
        EayunResponseJson json = new EayunResponseJson();
        
        try {
			List<Customer> list = apiOverviewService.getCusListByOrg(cusOrg);
			json.setData(list);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
        } catch (Exception e) {
        	json.setRespCode(ConstantClazz.ERROR_CODE);
        	json.setMessage(e.getMessage());
			log.error(e.getMessage(),e);
		}
        
        return JSONObject.toJSONString(json);
    }
	
	@SuppressWarnings("rawtypes")
	@RequestMapping("/getapioverviewdetails")
    @ResponseBody
    public String getApiOverviewDetails (HttpServletRequest request , @RequestBody Map map) {
        log.info("查看客户api指标概览折线图");
        EayunResponseJson json = new EayunResponseJson();
        String cusId = null==map.get("cusId")?"":map.get("cusId").toString();
        String startDate = null==map.get("startDate")?"":map.get("startDate").toString();
        String endDate = null==map.get("endDate")?"":map.get("endDate").toString();
        
        Date startTime = DateUtil.timestampToDate(startDate);
        Date endTime = DateUtil.timestampToDate(endDate);
        try {
			List<ApiIndexDetail> list = apiOverviewService.getApiOverviewDetails(cusId , startTime , endTime);
			json.setData(list);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
        } catch (Exception e) {
        	json.setRespCode(ConstantClazz.ERROR_CODE);
        	json.setMessage(e.getMessage());
			log.error(e.getMessage(),e);
		}
        
        return JSONObject.toJSONString(json);
    }
	
	@RequestMapping("/getapidetailspage")
    @ResponseBody
    public String getApiDetailsPage (HttpServletRequest request , Page page , @RequestBody ParamsMap map) {
        log.info("查看客户api指标信息列表");
        String cusId = map.getParams().get("cusId").toString();
        String startDate = map.getParams().get("startDate").toString();
        String endDate = map.getParams().get("endDate").toString();
        
        Date startTime = DateUtil.timestampToDate(startDate);
        Date endTime = DateUtil.timestampToDate(endDate);
        
        int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
        
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        page = apiOverviewService.getApiDetailsPage(page, queryMap, cusId, startTime, endTime);
        return JSONObject.toJSONString(page);
    }
}

package com.eayun.obs.ecmccontroller;

import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.controller.BaseController;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.customer.ecmcservice.EcmcCustomerService;
import com.eayun.customer.model.Customer;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.obs.ecmcmodel.EcmcObsEchartsBean;
import com.eayun.obs.ecmcservice.EcmcObsCustomerService;
import com.eayun.obs.model.ObsBucket;
import com.eayun.obs.model.ObsUsedType;

/*
 * ECMC1.1对象存储客户详情
 * 
 */
@Controller
@RequestMapping("/ecmc/obs/obscustomer")
public class EcmcObsCustomerController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(EcmcObsCustomerController.class);
	@Autowired
	private EcmcObsCustomerService ecmcObsCustomerService;
	@Autowired
	private EcmcCustomerService ecmcCustomerService;
	@Autowired
    private EcmcLogService ecmcLogService;

	/**
	 * 根据客户id获取该客户的全部bucket
	 * @param request
	 * @param cusId
	 * @return
	 */
	@RequestMapping(value = "/getAllBucketsByCusId", method = RequestMethod.POST)
	@ResponseBody
	public String getAllBucketsByCusId(HttpServletRequest request,@RequestBody Map<String, String> map) {
		String cusId = map.get("cusId");
		List<ObsBucket> buckerList = new ArrayList<ObsBucket>();
		try {
			buckerList = ecmcObsCustomerService.getBucketsByUserId(cusId);
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
		}
		return JSONObject.toJSONString(buckerList);

	}

	/**
	 * 获取指定用户：本月使用量
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/getObsInMonthUsed", method = RequestMethod.POST)
	@ResponseBody
	public String getObsInMonthUsed(HttpServletRequest request,@RequestBody Map<String, String> map)throws Exception {
		//已核对无误
		String cusId = map.get("cusId");
		ObsUsedType obsUsed = ecmcObsCustomerService.getObsInMonthUsed(cusId);
		return JSONObject.toJSONString(obsUsed);
	}
	
	 /**
	 * 用于对象存储获取历史账单
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/getObsHistoryResources", method = RequestMethod.POST)
	@ResponseBody             
	public String getObsHistoryResources(HttpServletRequest request,Page page,@RequestBody ParamsMap map) throws Exception {
		String cusId = "";
    	if(map.getParams().containsKey("cusId")){
    		if(!StringUtil.isEmpty(map.getParams().get("cusId").toString())){
    			cusId = map.getParams().get("cusId").toString();
        	}
    	}
    	int pageSize = map.getPageSize();
 		int pageNumber = map.getPageNumber();
 		QueryMap queryMap = new QueryMap();
 		queryMap.setPageNum(pageNumber);
 		queryMap.setCURRENT_ROWS_SIZE(pageSize);
    	
 		page = ecmcObsCustomerService.getObsHistoryResources(page, queryMap, cusId);
		return JSONObject.toJSONString(page);
	}
	
	/**
     * 获取配额
     */
    @ResponseBody
    @RequestMapping(value = "getQuota", method = RequestMethod.POST)
    public String getQuota(HttpServletRequest request,@RequestBody Map<String, String> map)throws Exception{
    	String cusId = map.get("cusId");
    	ObsUsedType result=ecmcObsCustomerService.getQuota(cusId);
        return JSONObject.toJSONString(result);
    }
    /**
     * 设置配额
     */
    @ResponseBody
    @RequestMapping(value = "setQuota", method = RequestMethod.POST)
    public String setQuota(HttpServletRequest request,@RequestBody Map<String, String> map)throws Exception{
    	String storage = map.get("storage");
    	String cusId = map.get("cusId");
    	Customer customer=ecmcCustomerService.getCustomerById(cusId);
    	String result=null;
    	try {
    		result=ecmcObsCustomerService.setQuota(cusId, storage, null, null);
    		ecmcLogService.addLog("配额设置", "对象存储", customer.getCusCpname(), null, 1, cusId, null);
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			ecmcLogService.addLog("配额设置", "对象存储", customer.getCusCpname(), null, 0, cusId, e);
		}
        return result;
    }
	
    /**
     * 对象存储客户详情--资源详情--折线图
     */
    @ResponseBody
    @RequestMapping(value="getObsUsedView" , method = RequestMethod.POST)
    public Object getObsUsedView(HttpServletRequest request , @RequestBody Map<String,String> map)throws Exception{
    	//type：storage/request/loadFlow为”存储量/请求次数/下载流量”
    	EcmcObsEchartsBean echartsBean = new EcmcObsEchartsBean();
    	String cusId = map.get("cusId");
    	String bucketName = map.get("bucketName");
    	String type = map.get("type");
    	String startTime = map.get("startTime");
    	String endTime = map.get("endTime");
    	
    	Date start = DateUtil.timestampToDate(startTime);
    	Date end = DateUtil.timestampToDate(endTime);
    	
    	String startString = DateUtil.dateToStr(start);
    	Date useStart = DateUtil.strToDate(startString);
    	String endString = DateUtil.dateToStr(end);
    	Date useEnd = DateUtil.strToDate(endString);
    	
    	echartsBean = ecmcObsCustomerService.getObsUsedView(bucketName, cusId, type, useStart, useEnd);
        return echartsBean;
    } 
    
    /**
	 * 用于对象存储--客户详情--消费统计
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/getObsResources" , method = RequestMethod.POST)
	@ResponseBody
	public String getObsResources(HttpServletRequest request,Page page,@RequestBody ParamsMap map) throws Exception {
		String cusId = "";
		String startTime ="";
		String endTime = "";
    	if(map.getParams().containsKey("cusId")){
    		if(!StringUtil.isEmpty(map.getParams().get("cusId").toString())){
    			cusId = map.getParams().get("cusId").toString();
        	}
    	}
    	if(map.getParams().containsKey("startDate")){
    		if(!StringUtil.isEmpty(map.getParams().get("startDate").toString())){
    			startTime = map.getParams().get("startDate").toString();
        	}
    	}
    	if(map.getParams().containsKey("endDate")){
    		if(!StringUtil.isEmpty(map.getParams().get("endDate").toString())){
    			endTime = map.getParams().get("endDate").toString();
        	}
    	}
    	int pageSize = map.getPageSize();
 		int pageNumber = map.getPageNumber();
 		QueryMap queryMap = new QueryMap();
 		queryMap.setPageNum(pageNumber);
 		queryMap.setCURRENT_ROWS_SIZE(pageSize);
 		
 		Date start = DateUtil.timestampToDate(startTime);
    	Date end = DateUtil.timestampToDate(endTime);
    	
    	String startString = DateUtil.dateToStr(start);
    	Date useStart = DateUtil.strToDate(startString);
    	
    	String endString = DateUtil.dateToStr(end);
    	Date useEnd = DateUtil.strToDate(endString);
 		
		page = ecmcObsCustomerService.getObsResources(page, queryMap, useStart, useEnd, cusId);
		return JSONObject.toJSONString(page);
	
	}
	/**
     * 对象存储--客户详情--获取obs客户信息
     */
    @ResponseBody
    @RequestMapping("getObsCustomer")
    public String getObsCustomer(HttpServletRequest request)throws Exception{
        List<Customer> cusList =  ecmcCustomerService.getObsCustomer();
        return JSONObject.toJSONString(cusList);
    }
	
	
}

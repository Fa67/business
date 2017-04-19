package com.eayun.obs.ecmccontroller;

import java.util.ArrayList;
import java.util.Date;
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
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.obs.ecmcmodel.EcmcObsEchartsBean;
import com.eayun.obs.ecmcservice.EcmcObsCdnService;
import com.eayun.obs.model.CdnBucket;
import com.eayun.obs.model.ObsUsedType;

/**
 * CDN-ECMC
 *                       
 * @Filename: EcmcObsCdnController.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2016年6月30日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Controller
@RequestMapping("/ecmc/obs/cdn")
@Scope("prototype")
public class EcmcObsCdnController extends BaseController{
	
	private static final Logger log = LoggerFactory.getLogger(EcmcObsCdnController.class);
	
	@Autowired
	private EcmcObsCdnService ecmcObsCdnService;
	
	/**
	 * 获取所有曾经开通过CDN服务的客户列表
	 * @Author: duanbinbin
	 * @param request
	 * @return
	 * @throws Exception
	 *<li>Date: 2016年6月30日</li>
	 */
	@ResponseBody
    @RequestMapping("/getobscdncustomer")
    public String getObsCdnCustomer(HttpServletRequest request)throws Exception{
		log.info("查询曾经开通过CDN服务的客户列表");
		EayunResponseJson json = new EayunResponseJson();
        List<CdnBucket> cdnList = new ArrayList<CdnBucket>();
		try {
			cdnList = ecmcObsCdnService.getObsCdnCusList();
			json.setData(cdnList);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
		}
        return JSONObject.toJSONString(json);
    }

	/**
	 * 获取客户下本月使用CDN加速流量中加速域名列表
	 * @Author: duanbinbin
	 * @param request
	 * @return
	 * @throws Exception
	 *<li>Date: 2016年6月30日</li>
	 */
	@ResponseBody
    @RequestMapping("/getmonthdomainpage")
    public String getMonthDomainPage(HttpServletRequest request ,Page page,@RequestBody ParamsMap map)throws Exception{
		log.info("查询客户本月内使用过的加速域名列表");
		String cusId = null == map.getParams().get("cusId")?"":map.getParams().get("cusId").toString();
        
        int pageSize = map.getPageSize();
 		int pageNumber = map.getPageNumber();
 		QueryMap queryMap = new QueryMap();
 		queryMap.setPageNum(pageNumber);
 		queryMap.setCURRENT_ROWS_SIZE(pageSize);
 		
		try {
			page = ecmcObsCdnService.getMonthDomainList(page,queryMap,cusId);
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
		}
        return JSONObject.toJSONString(page);
    }
	
	/**
	 * 获取客户下所有加速域名列表
	 * @Author: duanbinbin
	 * @param request
	 * @return
	 * @throws Exception
	 *<li>Date: 2016年6月30日</li>
	 */
	@SuppressWarnings("rawtypes")
    @ResponseBody
    @RequestMapping("/getalldomainlist")
    public String getAllDomainList(HttpServletRequest request , @RequestBody Map map)throws Exception{
		log.info("查询客户所有的加速域名列表");
		EayunResponseJson json = new EayunResponseJson();
		String cusId = null == map.get("cusId")?"":map.get("cusId").toString();
        List<CdnBucket> cdnList = new ArrayList<CdnBucket>();
		try {
			cdnList = ecmcObsCdnService.getAllDomainList(cusId);
			json.setData(cdnList);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
		}
        return JSONObject.toJSONString(json);
    }
	
	/**
	 * 获取加速域名一定时间范围内的CDN下载流量
	 * @Author: duanbinbin
	 * @param request
	 * @return
	 * @throws Exception
	 *<li>Date: 2016年6月30日</li>
	 */
	@SuppressWarnings("rawtypes")
    @ResponseBody
    @RequestMapping("/getdomaindata")
    public String getDomainData(HttpServletRequest request , @RequestBody Map map)throws Exception{
		log.info("查询加速域名的CDN下载流量图表显示");
		EayunResponseJson json = new EayunResponseJson();
		String cusId = null == map.get("cusId")?"":map.get("cusId").toString();
		String domain = null == map.get("domain")?"":map.get("domain").toString();
		String type = null == map.get("type")?"":map.get("type").toString();
		String start = map.get("startTime").toString();
        String end = map.get("endTime").toString();
        String queryType=null==map.get("queryType")?"":map.get("queryType").toString();
        Date startTime = DateUtil.timestampToDate(start);
        Date endTime = DateUtil.timestampToDate(end);
        
        String startString = DateUtil.dateToStr(startTime);
        startTime = DateUtil.strToDate(startString);
    	String endString = DateUtil.dateToStr(endTime);
    	endTime = DateUtil.strToDate(endString);
    	
        try {
			EcmcObsEchartsBean bean =  ecmcObsCdnService.getDomainData(cusId,domain, startTime, endTime , type,queryType);
			json.setData(bean);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
		}
        return JSONObject.toJSONString(json);
    }
	/**
	 * 获取本月客户的所有的CDN下载流量
	 * @Author: duanbinbin
	 * @param request
	 * @return
	 * @throws Exception
	 *<li>Date: 2016年6月30日</li>
	 */
	@SuppressWarnings("rawtypes")
    @ResponseBody
    @RequestMapping("/getmonthdomaindata")
    public String getMonthDomainData(HttpServletRequest request , @RequestBody Map map)throws Exception{
		log.info("获取本月客户的所有的CDN下载流量");
		EayunResponseJson json = new EayunResponseJson();
		String cusId = null == map.get("cusId")?"":map.get("cusId").toString();
		try {
			ObsUsedType data = ecmcObsCdnService.getMonthDomainData(cusId);
			json.setData(data);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
		}
        return JSONObject.toJSONString(json);
    }
	
	/**
	 * 用于CDN--资源详情--消费统计
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/getCDNResources" , method = RequestMethod.POST)
	@ResponseBody
	public String getCDNResources(HttpServletRequest request,Page page,@RequestBody ParamsMap map) throws Exception {
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
 		
		page = ecmcObsCdnService.getCDNResources(page, queryMap, useStart, useEnd, cusId);
		return JSONObject.toJSONString(page);
	
	}
	 /**
		 * 用于对象存储获取历史账单
		 * @param request
		 * @return
		 * @throws Exception
		 */
		@RequestMapping(value = "/getCdnHistoryResources", method = RequestMethod.POST)
		@ResponseBody             
		public String getCdnHistoryResources(HttpServletRequest request,Page page,@RequestBody ParamsMap map) throws Exception {
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
	    	
	 		page = ecmcObsCdnService.getCdnHistoryResources(page, queryMap, cusId);
			return JSONObject.toJSONString(page);
		}
}

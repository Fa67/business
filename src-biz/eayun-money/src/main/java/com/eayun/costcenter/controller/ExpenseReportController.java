package com.eayun.costcenter.controller;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.util.DateUtil;
import com.eayun.costcenter.model.MoneyRecord;
import com.eayun.costcenter.service.CostReportService;
import com.eayun.order.model.Order;
import com.eayun.order.service.OrderService;
/**
 * 费用中心-费用报表Controller
 * @author xiangyu.cao@eayun.com
 *
 */
@Controller
@RequestMapping("/costcenter/costreport")
public class ExpenseReportController extends BaseController{
	private static final Logger log = LoggerFactory.getLogger(ExpenseReportController.class);
	private SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMMdd");
	@Autowired
	private CostReportService costReportService;
	
	@ResponseBody
	@RequestMapping(value="/getreportlist" , method = RequestMethod.POST)
	public String getReportList(HttpServletRequest request,Page page, @RequestBody ParamsMap map) throws Exception{
		log.info("开始获取后付费费用报表");
		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
		String cusId=sessionUser.getCusId();
		String type=map.getParams().get("type").toString();
		String searchType=map.getParams().get("searchType")==null?null:map.getParams().get("searchType").toString();
		String begin=map.getParams().get("beginTime")==null?null:map.getParams().get("beginTime").toString();
		String end=map.getParams().get("endTime")==null?null:map.getParams().get("endTime").toString();
		String monMonth=map.getParams().get("monMonth")==null?null:map.getParams().get("monMonth").toString();
		String productName=map.getParams().get("productName")==null?null:map.getParams().get("productName").toString();
		String resourceName=map.getParams().get("resourceName")==null?null:map.getParams().get("resourceName").toString();
		Date beginTime = begin!=null&&begin.length()>0?DateUtil.timestampToDate(begin):null;
	    Date endTime = end!=null&&end.length()>0?DateUtil.timestampToDate(end):null;
	    int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
		QueryMap queryMap=new QueryMap();
		queryMap.setPageNum(pageNumber);
		queryMap.setCURRENT_ROWS_SIZE(pageSize);
		page=costReportService.getReportListPage(page, searchType, monMonth, beginTime, endTime, type, productName, resourceName,cusId,queryMap);
		return JSONObject.toJSONString(page);
	}

	@ResponseBody
	@RequestMapping(value="/gettotalcost" , method = RequestMethod.POST)
	public String getTotalCost(HttpServletRequest request,@RequestBody Map map) throws Exception{
		log.info("开始获取总计费用");
		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
		String cusId=sessionUser.getCusId();
		String type=map.get("type").toString();
		String searchType=map.get("searchType")==null?null:map.get("searchType").toString();
		String begin=map.get("beginTime")==null?null:map.get("beginTime").toString();
		String end=map.get("endTime")==null?null:map.get("endTime").toString();
		String monMonth=map.get("monMonth")==null?null:map.get("monMonth").toString();
		String productName=map.get("productName")==null?null:map.get("productName").toString();
		String resourceName=map.get("resourceName")==null?null:map.get("resourceName").toString();
		Date beginTime = begin!=null&&begin.length()>0?DateUtil.timestampToDate(begin):null;
	    Date endTime = end!=null&&end.length()>0?DateUtil.timestampToDate(end):null;
		String tatolCost=costReportService.getTotalCost(searchType, monMonth, beginTime, endTime, type, productName, resourceName,cusId);
		JSONObject json=new JSONObject();
		json.put("totalCost", tatolCost);
		return JSONObject.toJSONString(json);
	}
	
	/**
     * 导出excel
     */
    @RequestMapping("/createpostpayexcel")
    public void createPostPayExcel(HttpServletRequest request, HttpServletResponse response , String type , String searchType , String beginTime, 
                              String endTime, String monMonth ,String productName ,String resourceName ,String browser) throws Exception {
        log.info("导出Excel开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String cusId = sessionUser.getCusId();
        Date begin = beginTime==null?null:DateUtil.timestampToDate(beginTime);
        Date end = endTime==null?null:DateUtil.timestampToDate(endTime);
        
        Properties props=System.getProperties();
        String os = props.getProperty("os.name").toLowerCase();
        if(os.indexOf("windows") !=-1){
        	productName=productName==null?null:new String(productName.getBytes("ISO-8859-1"),"UTF-8");
        	resourceName=resourceName==null?null:new String(resourceName.getBytes("ISO-8859-1"),"UTF-8");
        }else{
        	productName=productName==null?null:new String(productName.getBytes("ISO-8859-1"),"UTF-8");
        	resourceName=resourceName==null?null:new String(resourceName.getBytes("ISO-8859-1"),"UTF-8");
        }
        String fileName="";
        String name = "后付费资源费用报表_"+simpleDateFormat.format(new Date())+".xls";
        if("Firefox".equals(browser)){
            fileName = new String(name.getBytes(), "iso-8859-1");
        }else{
            fileName = URLEncoder.encode(name, "UTF-8") ;
        }
        response.setContentType("application/vnd.ms-excel");
		response.addHeader("Content-Disposition", "attachment;filename=" + fileName);
        try {
        	costReportService.exportPostPayExcel(response.getOutputStream(),type, searchType, begin, end, monMonth, productName, resourceName, cusId);
        } catch (Exception e) {
            log.error("导出费用报表excel失败", e);
            throw e;
        }
    }
    /**
     * 导出excel
     */
    @RequestMapping("/createprepaymentexcel")
    public String createPrepaymentExcel(HttpServletRequest request, HttpServletResponse response , String type , String beginTime, 
                              String endTime ,String productName ,String browser) throws Exception {
        log.info("导出Excel开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String cusId = sessionUser.getCusId();
        Date begin = beginTime==null?null:DateUtil.timestampToDate(beginTime);
        Date end = endTime==null?null:DateUtil.timestampToDate(endTime);
        
        Properties props=System.getProperties();
        String os = props.getProperty("os.name").toLowerCase();
        if(os.indexOf("windows") !=-1){
        	productName=productName==null?null:new String(productName.getBytes("ISO-8859-1"),"UTF-8");
        }else{
        	productName=productName==null?null:new String(productName.getBytes("ISO-8859-1"),"UTF-8");
        }
        String fileName = "";
        String name = "预付费资源费用报表_"+simpleDateFormat.format(new Date())+".xls";
        if("Firefox".equals(browser)){
            fileName = new String(name.getBytes(), "iso-8859-1");
        }else{
            fileName = URLEncoder.encode(name, "UTF-8") ;
        }
        response.setContentType("application/vnd.ms-excel");
		response.addHeader("Content-Disposition", "attachment;filename=" + fileName);
        try {
        	costReportService.exportPrepaymentExcel(response.getOutputStream(),type, begin, end, productName, cusId);
        } catch (Exception e) {
            log.error("导出费用报表excel失败", e);
            throw e;
        }
        return null;
    }
    
    @ResponseBody
	@RequestMapping(value="/getpostpaydetail" , method = RequestMethod.POST)
	public String getPostpayDetail(HttpServletRequest request,@RequestBody Map map) throws Exception{
		log.info("开始获取报表详情");
		String id=map.get("id").toString();
		MoneyRecord moneyRecord=costReportService.getPostpayDetail(id);
		return JSONObject.toJSONString(moneyRecord);
	}
    @ResponseBody
    @RequestMapping(value="/getprepaymentdetail" , method = RequestMethod.POST)
    public String getPrepaymentDetail(HttpServletRequest request,@RequestBody Map map) throws Exception{
    	log.info("开始获取报表详情");
    	SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String cusId = sessionUser.getCusId();
    	String orderNo=map.get("orderNo").toString();
    	EayunResponseJson json = new EayunResponseJson();
    	try {
    		Order order=costReportService.getPrepaymentDetails(orderNo);
    		json.setData(order);
    		json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (AppException e) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			throw e;
		}
    	return JSONObject.toJSONString(json);
    }
    @ResponseBody
    @RequestMapping(value="/orderisbelong" , method = RequestMethod.POST)
    public String orderIsBelong(HttpServletRequest request,@RequestBody Map map) throws Exception{
    	SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String cusId = sessionUser.getCusId();
    	String orderNo=map.get("orderNo").toString();
    	EayunResponseJson json = new EayunResponseJson();
    	try {
    		boolean result=costReportService.orderIsBelong(orderNo);
    		json.setData(result);
    		json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (AppException e) {
		    log.error(e.getMessage(),e);
			json.setRespCode(ConstantClazz.ERROR_CODE);
		}
    	return JSONObject.toJSONString(json);
    }
}

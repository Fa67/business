package com.eayun.monitor.ecmccontroller;

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
import com.eayun.common.RedisNodeIdConstant;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.util.DateUtil;
import com.eayun.monitor.bean.EcmcVmIndicator;
import com.eayun.monitor.bean.MonitorMngData;
import com.eayun.monitor.ecmcservice.EcmcVmIndicatorService;


@Controller
@RequestMapping("/ecmc/monitor/resource")
public class EcmcVmIndicatorController {
	
	private static final Logger log = LoggerFactory.getLogger(EcmcVmIndicatorController.class);
	
	@Autowired
    private EcmcVmIndicatorService ecmcVmIndicatorService;
	
	@RequestMapping("/getinterval")
    @ResponseBody
	public String getInterval(HttpServletRequest request){
		log.info("获取主机监控详情时间范围");
		JSONObject json = new JSONObject();
		try {
			List<MonitorMngData> MonitorMngList = ecmcVmIndicatorService.getInterval(RedisNodeIdConstant.VM_MONITOR_TIME);
			json.put("data", MonitorMngList);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
		}
		return json.toJSONString();
	}
	
	@RequestMapping("/getvmlistforecmclive")
    @ResponseBody
	public String getvmListforEcmcLive(HttpServletRequest request , Page page , @RequestBody ParamsMap map){
		log.info("获取资源监控实时指标数据信息");
		String queryType = map.getParams().get("queryType").toString();
        String queryName = map.getParams().get("queryName").toString();
    	String orderBy = map.getParams().get("orderBy").toString();
        String sort = map.getParams().get("sort").toString();
        String dcName = map.getParams().get("dcName").toString();
        int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
        
        pageSize = 20;
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        
        page = ecmcVmIndicatorService.getVmListforLive(page, queryMap, queryType, queryName , dcName,orderBy,sort);
		return JSONObject.toJSONString(page);
	}
	
	@RequestMapping("/getvmlistforecmclast")
    @ResponseBody
	public String getvmListforEcmcLast(HttpServletRequest request , Page page , @RequestBody ParamsMap map){
		log.info("获取资源监控历史指标数据信息开始");
		String queryType = map.getParams().get("queryType").toString();
        String queryName = map.getParams().get("queryName").toString();
        String end = map.getParams().get("endDate").toString();
        String interval = map.getParams().get("interval").toString();
        String orderBy = map.getParams().get("orderBy").toString();
        String sort = map.getParams().get("sort").toString();
        String dcName = map.getParams().get("dcName").toString();
        
        int mins = Integer.parseInt(interval);
        Date endDate = DateUtil.timestampToDate(end);
        
        int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
        
        pageSize = 20;
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        page = ecmcVmIndicatorService.getVmListforLast(page, queryMap, queryType, queryName, endDate, mins, orderBy, sort , dcName);
		return JSONObject.toJSONString(page);
	}
	
	@RequestMapping("/getvmdetailbyid")
    @ResponseBody
	public String getVmdetailById(HttpServletRequest request , @RequestBody Map map){
		log.info("查询云主机详情开始");
		JSONObject json = new JSONObject();
		String vmId = map.get("vmId").toString();
        try {
			EcmcVmIndicator vm = ecmcVmIndicatorService.getvmById(vmId);
			json.put("data", vm);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
		}
		return json.toJSONString();
	}
	
	@RequestMapping("/getmonitordatabyid")
    @ResponseBody
	public String getMonitorDataById (HttpServletRequest request , @RequestBody Map map){
		log.info("查询资源监控详情开始");
		JSONObject json = new JSONObject();
        String end = map.get("endTime").toString();
        String count = map.get("count").toString();
        String vmId = map.get("vmId").toString();
        String type = map.get("type").toString();
        
        int cou = Integer.parseInt(count);
        Date endTime = DateUtil.timestampToDate(end);
        
        try {
			List<EcmcVmIndicator> vmList = ecmcVmIndicatorService.getDataById(endTime,cou,vmId,type);
			json.put("data", vmList);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
        } catch (Exception e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
		}
		return json.toJSONString();
	}
	
	@RequestMapping("/getcharttypes")
    @ResponseBody
	public String getChartTypes(HttpServletRequest request){
		log.info("获取主机监控详情图表类别");
		JSONObject json = new JSONObject();
		try {
			List<MonitorMngData> MonitorMngList = ecmcVmIndicatorService.getChartTypes(RedisNodeIdConstant.VM_MONITOR_DETAILS_TYPE);
			json.put("data", MonitorMngList);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
        } catch (Exception e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
		}
		return json.toJSONString();
	}
	
	@RequestMapping("/getcharttimes")
    @ResponseBody
	public String getChartTimes(HttpServletRequest request){
		log.info("获取主机监控详情时间范围");
		JSONObject json = new JSONObject();
		try {
			List<MonitorMngData> MonitorMngList = ecmcVmIndicatorService.getChartTimes(RedisNodeIdConstant.VM_MONITOR_DETAILS_TIME);
			json.put("data", MonitorMngList);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
        } catch (Exception e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
		}
		return json.toJSONString();
	}

}

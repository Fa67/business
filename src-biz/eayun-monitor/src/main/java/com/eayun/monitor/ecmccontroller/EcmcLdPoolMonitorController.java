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
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.util.DateUtil;
import com.eayun.monitor.bean.LdPoolIndicator;
import com.eayun.monitor.ecmcservice.EcmcLdPoolMonitorService;
import com.eayun.monitor.model.CloudLdpoolExp;

@Controller
@RequestMapping("/ecmc/monitor/ldpool")
public class EcmcLdPoolMonitorController {

private static final Logger log = LoggerFactory.getLogger(EcmcLdPoolMonitorController.class);
	
	@Autowired
    private EcmcLdPoolMonitorService ecmcLdPoolMonitorService;
	
	@RequestMapping("/getecmcldpoolmonitorlist")
    @ResponseBody
    public String getEcmcLdPoolMonitorList(HttpServletRequest request , Page page , @RequestBody ParamsMap map) {
        log.info("运维获取负载均衡资源监控开始");
        String queryType = null==map.getParams().get("queryType")?"":map.getParams().get("queryType").toString();
        String queryName = null==map.getParams().get("queryName")?"":map.getParams().get("queryName").toString();
    	String orderBy = null==map.getParams().get("orderBy")?"":map.getParams().get("orderBy").toString();
        String sort = null==map.getParams().get("sort")?"":map.getParams().get("sort").toString();
        String dcName = null==map.getParams().get("dcName")?"":map.getParams().get("dcName").toString();
        String mode = null==map.getParams().get("mode")?"":map.getParams().get("mode").toString();
        
        int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
        
        pageSize = 20;
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        
        page=ecmcLdPoolMonitorService.getEcmcLdPoolMonitorList(page,queryMap,
        		queryType, queryName,orderBy,sort,dcName,mode);
        return JSONObject.toJSONString(page);
    }
    /**
     * 运维查询负载均衡的成员异常记录列表
     * @Author: duanbinbin
     * @param request
     * @param page
     * @param map
     * @return
     *<li>Date: 2017年3月2日</li>
     */
    @RequestMapping("/getecmcldpoolexplist")
    @ResponseBody
    public String getEcmcLdPoolExpList(HttpServletRequest request , Page page , @RequestBody ParamsMap map) {
        log.info("运维查询负载均衡成员异常记录列表");
        String end = null==map.getParams().get("endTime")?"":map.getParams().get("endTime").toString();
        String count = null==map.getParams().get("count")?"":map.getParams().get("count").toString();
        String poolId = null==map.getParams().get("poolId")?"":map.getParams().get("poolId").toString();
        String mode = null==map.getParams().get("mode")?"":map.getParams().get("mode").toString();
        String role = null==map.getParams().get("role")?"":map.getParams().get("role").toString();
        String memberName = null==map.getParams().get("memberName")?"":map.getParams().get("memberName").toString();
        String healthName = null==map.getParams().get("healthName")?"":map.getParams().get("healthName").toString();
        String isRepair = null==map.getParams().get("isRepair")?"":map.getParams().get("isRepair").toString();
        
        int cou = Integer.parseInt(count);
        Date endTime = DateUtil.timestampToDate(end);
        int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
        
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        
        page=ecmcLdPoolMonitorService.getEcmcLdPoolExpList(page,queryMap,
        		endTime,cou,poolId,mode,role,memberName,healthName,isRepair);
        return JSONObject.toJSONString(page);
    }
    
    @RequestMapping("/getLdPoolDetailById")
    @ResponseBody
    public String getLdPoolDetailById(HttpServletRequest request , @RequestBody Map map) {
    	JSONObject json = new JSONObject();
        String ldPoolId = map.get("ldPoolId").toString();
        try {
        	LdPoolIndicator LdPool = ecmcLdPoolMonitorService.getLdPoolDetailById(ldPoolId);
			json.put("data", LdPool);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
		}
		return json.toJSONString();
		
    }
    
    @RequestMapping("/getmemandheanamebyid")
    @ResponseBody
    public String getMemAndHeaNameById(HttpServletRequest request , @RequestBody Map map) {
    	log.info("查询所有符合条件的成员和健康检查名称，并根据名称去重");
    	JSONObject json = new JSONObject();
    	String end = null==map.get("endTime")?"":map.get("endTime").toString();
        String count = null==map.get("count")?"":map.get("count").toString();
        String poolId = null==map.get("poolId")?"":map.get("poolId").toString();
        String mode = null==map.get("mode")?"":map.get("mode").toString();
        String role = null==map.get("role")?"":map.get("role").toString();
        String memberName = null==map.get("memberName")?"":map.get("memberName").toString();
        String healthName = null==map.get("healthName")?"":map.get("healthName").toString();
        String isRepair = null==map.get("isRepair")?"":map.get("isRepair").toString();
        
        int cou = Integer.parseInt(count);
        Date endTime = DateUtil.timestampToDate(end);
        try {
        	Map<String ,List<CloudLdpoolExp>> nameList = ecmcLdPoolMonitorService.getMemAndHeaNameById(endTime,
        			cou,poolId,mode,role,memberName,null,isRepair);
			json.put("data", nameList);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
		}
		return json.toJSONString();
		
    }
}

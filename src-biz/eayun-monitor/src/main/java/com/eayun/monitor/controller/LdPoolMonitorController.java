package com.eayun.monitor.controller;

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
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.util.DateUtil;
import com.eayun.monitor.bean.LdPoolIndicator;
import com.eayun.monitor.model.CloudLdpoolExp;
import com.eayun.monitor.service.LdPoolAlarmMonitorService;

@Controller
@RequestMapping("/monitor/ldpool")
public class LdPoolMonitorController {
	
	private static final Logger log = LoggerFactory.getLogger(LdPoolMonitorController.class);

	@Autowired
    private LdPoolAlarmMonitorService ldPoolAlarmMonitorService;
    /**
     * 查询负载均衡资源监控列表（主备或普通）
     * @Author: duanbinbin
     * @param request
     * @param page
     * @param map
     * @return
     *<li>Date: 2017年3月2日</li>
     */
    @RequestMapping("/getldpoolmonitorlist")
    @ResponseBody
    public String getLdPoolMonitorList(HttpServletRequest request , Page page , @RequestBody ParamsMap map) {
        log.info("获取负载均衡资源监控开始");
        String projectId = map.getParams().get("projectId").toString();
        String poolName = map.getParams().get("poolName").toString();
        String mode = map.getParams().get("mode").toString();
        int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
        
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        
        page=ldPoolAlarmMonitorService.getLdPoolMonitorList(page,queryMap , projectId, poolName,mode);
        return JSONObject.toJSONString(page);
    }
    /**
     * 查询负载均衡的成员异常记录列表
     * @Author: duanbinbin
     * @param request
     * @param page
     * @param map
     * @return
     *<li>Date: 2017年3月2日</li>
     */
    @RequestMapping("/getldpoolexplist")
    @ResponseBody
    public String getLdPoolExpList(HttpServletRequest request , Page page , @RequestBody ParamsMap map) {
        log.info("查询负载均衡成员异常记录列表");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().
                getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String cusId = sessionUser.getCusId();
        String end = map.getParams().get("endTime").toString();
        String count = map.getParams().get("count").toString();
        String poolId = map.getParams().get("poolId").toString();
        String mode = map.getParams().get("mode").toString();
        String role = map.getParams().get("role").toString();
        String memberName = map.getParams().get("memberName").toString();
        String healthName = map.getParams().get("healthName").toString();
        String isRepair = map.getParams().get("isRepair").toString();
        
        int cou = Integer.parseInt(count);
        Date endTime = DateUtil.timestampToDate(end);
        int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
        
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        
        page=ldPoolAlarmMonitorService.getLdPoolExpList(page,queryMap,
        		cusId,endTime,cou,poolId,mode,role,memberName,healthName,isRepair);
        return JSONObject.toJSONString(page);
    }
    /**
     * 负载均衡资源监控查询详情
     * @Author: duanbinbin
     * @param request
     * @param map
     * @return
     *<li>Date: 2017年3月10日</li>
     */
    @RequestMapping("/getLdPoolDetailById")
    @ResponseBody
    public String getLdPoolDetailById(HttpServletRequest request , @RequestBody Map map) {
        String ldPoolId = map.get("ldPoolId").toString();
        LdPoolIndicator LdPool = ldPoolAlarmMonitorService.getLdPoolDetailById(ldPoolId);
        return JSONObject.toJSONString(LdPool);
    }
    
    @RequestMapping("/getNameListById")
    @ResponseBody
    public String getNameListById(HttpServletRequest request , @RequestBody Map map) {
    	log.info("查询所有符合条件的成员和健康检查名称，并根据名称去重");
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
        Map<String ,List<CloudLdpoolExp>> nameList = ldPoolAlarmMonitorService.getNameListById(endTime,
    			cou,poolId,mode,role,memberName,null,isRepair);
		return JSONObject.toJSONString(nameList);
		
    }
}

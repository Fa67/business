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
import com.eayun.monitor.bean.VmIndicator;
import com.eayun.monitor.service.VmIndicatorService;

@Controller
@RequestMapping("/monitor/resourcemonitor")
public class VmIndicatorController {
    
private static final Logger log = LoggerFactory.getLogger(VmIndicatorController.class);
    
    @Autowired
    private VmIndicatorService vmIndicatorService;
    
    @RequestMapping("/getvmListforMonitor")
    @ResponseBody
    public String getvmListforMonitor(HttpServletRequest request , Page page , @RequestBody ParamsMap map) {
        log.info("获取资源监控开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().
                getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        
        String projectId = map.getParams().get("projectId").toString();
        String vmName = map.getParams().get("vmName").toString();
        int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
        
        pageSize = 20;
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        
        page=vmIndicatorService.getvmList(page,queryMap , projectId, vmName);
        return JSONObject.toJSONString(page);
    }
    
    @RequestMapping("/getVmdetailById")
    @ResponseBody
    public String getVmdetailById(HttpServletRequest request , @RequestBody Map map) {
        String vmId = map.get("vmId").toString();
        VmIndicator vm = vmIndicatorService.getvmById(vmId);
        return JSONObject.toJSONString(vm);
    }
    
    @RequestMapping("/getMonitorDataById")
    @ResponseBody
    public String getMonitorDataById(HttpServletRequest request , @RequestBody Map map) {
        log.info("查询主机监控开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().
                getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String cusId = sessionUser.getCusId();
        String end = null==map.get("endTime")?"":map.get("endTime").toString();
        String count = null==map.get("count")?"":map.get("count").toString();
        String vmId = null==map.get("vmId")?"":map.get("vmId").toString();
        String type = null==map.get("type")?"":map.get("type").toString();
        String instanceId = null==map.get("instanceId")?"":map.get("instanceId").toString();
        
        int cou = Integer.parseInt(count);
        Date endTime = DateUtil.timestampToDate(end);
        
        List<VmIndicator> vmList = vmIndicatorService.getDataById(endTime,cou,vmId,type,cusId,instanceId);
        return JSONObject.toJSONString(vmList);
    }
}

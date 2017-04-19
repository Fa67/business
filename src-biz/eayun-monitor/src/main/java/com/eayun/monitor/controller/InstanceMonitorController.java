package com.eayun.monitor.controller;

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
import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.database.configgroup.model.datastore.DatastoreVersion;
import com.eayun.monitor.bean.VmIndicator;
import com.eayun.monitor.service.InstanceAlarmMonitorService;
/**
 * ECSC数据库实例资源监控列表
 *                       
 * @Filename: InstanceMonitorController.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2017年3月7日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Controller
@RequestMapping("/monitor/instance")
public class InstanceMonitorController {
	
	private static final Logger log = LoggerFactory.getLogger(InstanceMonitorController.class);

	@Autowired
    private InstanceAlarmMonitorService instanceMonitorService;
    
	/**
	 * 查询实例资源监控
	 * @Author: duanbinbin
	 * @param request
	 * @param page
	 * @param map
	 * @return
	 *<li>Date: 2017年3月7日</li>
	 */
    @RequestMapping("/getinstancemonitorpage")
    @ResponseBody
    public String getInstanceMonitorPage(HttpServletRequest request , Page page , @RequestBody ParamsMap map) {
        log.info("获取数据实例资源监控列表");
        
        String prjId = map.getParams().get("prjId").toString();
        String instanceName = map.getParams().get("instanceName").toString();
        String versionId = map.getParams().get("versionId").toString();
        int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
        
        pageSize = 20;
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        
        page=instanceMonitorService.getInstanceMonitorPage(page,queryMap , prjId, instanceName, versionId);
        return JSONObject.toJSONString(page);
    }
    /**
     * 查询数据库实例版本列表
     * @Author: duanbinbin
     * @param request
     * @return
     * @throws Exception
     *<li>Date: 2017年3月7日</li>
     */
    @RequestMapping(value="/getdataversionlist", method = RequestMethod.POST)
    @ResponseBody
    public String getDataVersionList(HttpServletRequest request) throws Exception{
        log.info("获取数据版本列表");
        List<DatastoreVersion> list = instanceMonitorService.getDataVersionList();
        return JSONObject.toJSONString(list);
    }

    @RequestMapping("/getRdsDetailById")
    @ResponseBody
	public String getRdsDetailById(HttpServletRequest request , @RequestBody Map map){
		log.info("资源监控查询数据库实例详情开始");
		JSONObject json = new JSONObject();
		String instanceId = map.get("instanceId").toString();
        try {
			VmIndicator instance = instanceMonitorService.getRdsDetailById(instanceId);
			json.put("data", instance);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
		}
		return json.toJSONString();
	}
}

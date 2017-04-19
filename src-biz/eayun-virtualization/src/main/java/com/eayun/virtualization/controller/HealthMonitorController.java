package com.eayun.virtualization.controller;


import java.util.ArrayList;
import java.util.List;

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
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.log.service.LogService;
import com.eayun.virtualization.model.BaseCloudLdMonitor;
import com.eayun.virtualization.model.CloudLdMonitor;
import com.eayun.virtualization.model.CloudLdPool;
import com.eayun.virtualization.service.HealthMonitorService;

@Controller
@RequestMapping("/cloud/loadbalance/healthmonitor")
@Scope("prototype")
public class HealthMonitorController extends BaseController{
    private static final Logger log = LoggerFactory.getLogger(HealthMonitorController.class);
	@Autowired
	private LogService logService;
	@Autowired
	private HealthMonitorService monitorService;
	
	/**
	 * 查询健康检查
	 * 
	 * @author zhouhaitao
	 * @param request
	 * @param page
	 * @param map
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/getMonitorList" , method = RequestMethod.POST)
	@ResponseBody 
	public String getMonitorList(HttpServletRequest request,Page page, @RequestBody ParamsMap map) throws Exception{
		String ldmName="";
		try {
			 String dcId=map.getParams().get("dcId").toString();
			 String prjId = map.getParams().get("prjId").toString();
			 if(map.getParams().containsKey("name")){
				 ldmName=map.getParams().get("name").toString();
			 }
			int pageSize = map.getPageSize();
			int pageNumber = map.getPageNumber();
			QueryMap queryMap = new QueryMap();
			queryMap.setPageNum(pageNumber);
			queryMap.setCURRENT_ROWS_SIZE(pageSize);
			
			page  = monitorService.getMonitorList(page,dcId,prjId,ldmName,queryMap);
		}catch (Exception e) {
			throw e;
		}
		return JSONObject.toJSONString(page);
	}
	
	
	/**
	 * 查询项目的健康检查
	 * 
	 * @author zhouhaitao
	 * @param request
	 * @param cloudLdMonitor
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/getMonitorListByPool" , method = RequestMethod.POST)
	@ResponseBody                
	public String getMonitorListByPool(HttpServletRequest request, @RequestBody CloudLdPool pool) throws Exception{
		JSONObject json = new JSONObject();
		List<CloudLdMonitor> monitorList = new ArrayList<CloudLdMonitor>();
		try{
			monitorList = monitorService.getMonitorListByPool(pool);
			json.put("data",monitorList);
		}catch(Exception e){
		    log.error(e.toString(),e);
			throw e;
		}
		return json.toJSONString();
	} 
	
	/**
	 * 创建健康检查
	 * -----------------
	 * @author zhouhaitao
	 * @param request
	 * @param cloudLdMonitor
	 * @return
	 * @throws AppException
	 */
	@RequestMapping(value = "/addHealthMonitor", method = RequestMethod.POST)
	@ResponseBody
	public String addHealthMonitor(HttpServletRequest request, @RequestBody CloudLdMonitor cloudLdMonitor) throws Exception{
		JSONObject json = new JSONObject ();
		SessionUserInfo sessionUser = null;
		BaseCloudLdMonitor  result=null; 
		try {
			sessionUser = (SessionUserInfo)request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
			result = monitorService.addHealthMonitor(cloudLdMonitor,sessionUser); 
			logService.addLog("创建健康检查", ConstantClazz.LOG_TYPE_HEALTHMIR, cloudLdMonitor.getLdmName(), cloudLdMonitor.getPrjId(),
					ConstantClazz.LOG_STATU_SUCCESS, null);
			json.put("data", result);
			json.put("respCode",ConstantClazz.SUCCESS_CODE_ADD);
		}catch (Exception e) {
			logService.addLog("创建健康检查", ConstantClazz.LOG_TYPE_HEALTHMIR, cloudLdMonitor.getLdmName(), cloudLdMonitor.getPrjId(),
					ConstantClazz.LOG_STATU_ERROR, e);
			json.put("respCode",ConstantClazz.ERROR_CODE);
			throw e;
		}
		return json.toJSONString();
	}
	
	/**
	 * 修改健康检查
	 * --------------
	 * @author zhouhaitao
	 * @param request
	 * @param cloudLdMonitor
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/updateMonitor", method = RequestMethod.POST)
	@ResponseBody                
	public String updateMonitor(HttpServletRequest request,@RequestBody CloudLdMonitor cloudLdMonitor) throws Exception{
		JSONObject json = new JSONObject ();
		BaseCloudLdMonitor result=null;
		try {
			result = monitorService.updateMonitor(cloudLdMonitor);
			logService.addLog("编辑健康检查", ConstantClazz.LOG_TYPE_HEALTHMIR, cloudLdMonitor.getLdmName(), cloudLdMonitor.getPrjId(),
					ConstantClazz.LOG_STATU_SUCCESS, null);
			json.put("data", result);
			json.put("respCode",ConstantClazz.SUCCESS_CODE_UPDATE);
		}catch (Exception e) {
			logService.addLog("编辑健康检查", ConstantClazz.LOG_TYPE_HEALTHMIR, cloudLdMonitor.getLdmName(), cloudLdMonitor.getPrjId(),
					ConstantClazz.LOG_STATU_ERROR, e);
			json.put("respCode",ConstantClazz.ERROR_CODE);
			throw e;
		}

		return json.toJSONString();
	}
	
	/**
	 * 删除健康检查
	 * -------------
	 * @author zhouhaitao
	 * @param request
	 * @param cloudLdMonitor
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/deleteHealthMonitor", method = RequestMethod.POST)
	@ResponseBody                      
	public String deleteHealthMonitor(HttpServletRequest request,@RequestBody CloudLdMonitor cloudLdMonitor) throws Exception{
		JSONObject json = new JSONObject ();
		try {
			monitorService.deleteMonitor(cloudLdMonitor);
			logService.addLog("删除健康检查",ConstantClazz.LOG_TYPE_HEALTHMIR,  cloudLdMonitor.getLdmName(), cloudLdMonitor.getPrjId(),
					ConstantClazz.LOG_STATU_SUCCESS, null);
			json.put("respCode",ConstantClazz.SUCCESS_CODE_DELETE);
		}catch (Exception e) {
			logService.addLog("删除健康检查",ConstantClazz.LOG_TYPE_HEALTHMIR,  cloudLdMonitor.getLdmName(), cloudLdMonitor.getPrjId(),
					ConstantClazz.LOG_STATU_ERROR, e);
			json.put("respCode",ConstantClazz.ERROR_CODE);
			throw e;
		}
		return json.toJSONString();
	}
	
	/**
	 * 负载均衡器->健康检查(绑定健康检查)
	 * -------------
	 * @author zhouhaitao
	 * @param request
	 * @param cloudLdPool
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/bindHealthMonitor", method = RequestMethod.POST)
	@ResponseBody                      
	public String bindHealthMonitor(HttpServletRequest request,@RequestBody CloudLdPool cloudLdPool) throws Exception{
		JSONObject json = new JSONObject ();
		List<CloudLdMonitor> cloudLdMonitor = new ArrayList<CloudLdMonitor>();
		try {
			cloudLdMonitor = monitorService.bindHealthMonitor(cloudLdPool);
			json.put("data",cloudLdMonitor);
			json.put("respCode",ConstantClazz.SUCCESS_CODE_OP);
			logService.addLog("绑定健康检查",ConstantClazz.LOG_TYPE_HEALTHMIR,  cloudLdPool.getPoolName(), cloudLdPool.getPrjId(),
					ConstantClazz.LOG_STATU_SUCCESS, null);
		}catch (Exception e) {
			logService.addLog("绑定健康检查",ConstantClazz.LOG_TYPE_HEALTHMIR,  cloudLdPool.getPoolName(), cloudLdPool.getPrjId(),
					ConstantClazz.LOG_STATU_ERROR, e);
			json.put("respCode",ConstantClazz.ERROR_CODE);
			throw e;
		}
		return json.toJSONString();
	}
	
	/**
	 * 负载均衡器->健康检查(解除健康检查)
	 * -------------
	 * @author zhouhaitao
	 * @param request
	 * @param cloudLdPool
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/unBindHealthMonitor", method = RequestMethod.POST)
	@ResponseBody                      
	public String unBindHealthMonitor(HttpServletRequest request,@RequestBody CloudLdPool cloudLdPool) throws Exception{
		JSONObject json = new JSONObject ();
		List<CloudLdMonitor> cloudLdMonitor = new ArrayList<CloudLdMonitor>();
		try {
			cloudLdMonitor = monitorService.unBindHealthMonitorForPool(cloudLdPool);
			json.put("data",cloudLdMonitor);
			json.put("respCode",ConstantClazz.SUCCESS_CODE_OP);
			logService.addLog("解除健康检查",ConstantClazz.LOG_TYPE_HEALTHMIR,  cloudLdPool.getPoolName(), cloudLdPool.getPrjId(),
					ConstantClazz.LOG_STATU_SUCCESS, null);
		}catch (Exception e) {
			logService.addLog("解除健康检查",ConstantClazz.LOG_TYPE_HEALTHMIR,  cloudLdPool.getPoolName(), cloudLdPool.getPrjId(),
					ConstantClazz.LOG_STATU_ERROR, e);
			json.put("respCode",ConstantClazz.ERROR_CODE);
			throw e;
		}
		return json.toJSONString();
	}
	
	/**
	 * 健康检查重名校验
	 * -------------
	 * @author zhouhaitao
	 * @param request
	 * @param cloudLdPool
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/checkMonitorExsit", method = RequestMethod.POST)
	@ResponseBody                      
	public String checkMonitorExsit(HttpServletRequest request,@RequestBody CloudLdMonitor cloudLdMonitorl) throws Exception{
		JSONObject json = new JSONObject ();
		boolean flag = false;
		try {
			flag = monitorService.bindHealthMonitor(cloudLdMonitorl);
			json.put("data",flag);
		}catch (Exception e) {
			throw e;
		}
		return json.toJSONString();
	}
	
}

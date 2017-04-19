package com.eayun.project.ecmccontroller;

import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.controller.BaseController;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.tools.SeqTool;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.customer.model.BaseCustomer;
import com.eayun.customer.model.Customer;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.project.ecmcservice.EcmcProjectService;
import com.eayun.virtualization.bean.CloudTypes;
import com.eayun.virtualization.model.BaseCloudProject;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.service.StatisticsService;

@Controller
@RequestMapping("/ecmc/project")
@Scope("prototype")
public class EcmcProjectController extends BaseController {

	private static final Logger log = LoggerFactory.getLogger(EcmcProjectController.class);

	@Autowired
	private EcmcProjectService ecmcProjectService;
	@Autowired
	private StatisticsService statisticsService;
	@Autowired
    private EcmcLogService ecmcLogService;

	@RequestMapping(value = "/createproject")
	@ResponseBody
	public Object createProject(@RequestBody Map<String, Object> requestMap) throws Exception{
		EayunResponseJson reJson = new EayunResponseJson();
		Customer customer = new Customer();
		BeanUtils.mapToBean(customer, requestMap);
		CloudProject cloudProject = new CloudProject();
		BeanUtils.mapToBean(cloudProject, requestMap);
		try {
			Map<String,Object> returnMap = ecmcProjectService.createProject(cloudProject, customer, false);
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			customer = (Customer)MapUtils.getObject(returnMap, "customer");
			BaseCloudProject project = (BaseCloudProject)MapUtils.getObject(returnMap, "project");
			ecmcLogService.addLog("创建客户和项目（客户）", ConstantClazz.LOG_TYPE_CUSTOMER, customer.getCusOrg(), null, 1, customer.getCusId(), null);
			ecmcLogService.addLog("创建客户和项目（项目）", ConstantClazz.LOG_TYPE_PROJECT, project.getPrjName(), project.getProjectId(), 1, project.getProjectId(), null);
		} catch (Exception e) {
			reJson.setRespCode(ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("创建客户和项目（客户）", ConstantClazz.LOG_TYPE_CUSTOMER, customer.getCusOrg(), null, 0, customer.getCusId(), e);
			ecmcLogService.addLog("创建客户和项目（项目）", ConstantClazz.LOG_TYPE_PROJECT, cloudProject.getPrjName(), cloudProject.getProjectId(), 0, cloudProject.getProjectId(), e);
			throw e;
		}
		return reJson;
	}

	@RequestMapping(value = "/generateprojectname")
	@ResponseBody
	public Object generateProjectName(@RequestBody Map<String, Object> requestMap) {
		EayunResponseJson reJson = new EayunResponseJson();
		try {
			String cusOrg = MapUtils.getString(requestMap, "cusOrg");
			String prjName = SeqTool.getSeqId(cusOrg, "_", 2);
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			reJson.setData(prjName);
		} catch (Exception e) {
			reJson.setRespCode(ConstantClazz.ERROR_CODE);
			throw e;
		}
		return reJson;
	}

	@RequestMapping(value = "/getprojectbycustomer")
	@ResponseBody
	public Object getProjectByCustomer(@RequestBody Map<String, Object> requestMap) {
		EayunResponseJson reJson = new EayunResponseJson();
		reJson.setRespCode(ConstantClazz.ERROR_CODE);
		String customerId = MapUtils.getString(requestMap, "cusId");
		if (!StringUtil.isEmpty(customerId)) {
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			reJson.setData(ecmcProjectService.getProjectByCustomer(customerId));
		}
		return reJson;
	}

	@RequestMapping(value = "/getprojectbyid")
	@ResponseBody
	public Object getProjectById(@RequestBody Map<String, Object> requestMap) throws Exception {
		EayunResponseJson reJson = new EayunResponseJson();
		reJson.setRespCode(ConstantClazz.ERROR_CODE);
		String projectId = MapUtils.getString(requestMap, "projectId");
		if (!StringUtil.isEmpty(projectId)) {
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			reJson.setData(ecmcProjectService.getProjectById(projectId));
		}
		return reJson;
	}

	@RequestMapping(value = "/modifyproject")
	@ResponseBody
	public Object modifyProject(@RequestBody Map<String, Object> requestMap) throws Exception {
		EayunResponseJson reJson = new EayunResponseJson();
		BaseCloudProject baseCloudProject = new BaseCloudProject();
		//移除创建时间字段，避免因该字段值为null导致转化baseCloudProject失败
		requestMap.remove("createDate");
		BeanUtils.mapToBean(baseCloudProject, requestMap);
		baseCloudProject.getSnapshotSize();
		try {
			baseCloudProject = ecmcProjectService.updateProject(baseCloudProject);
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("修改项目", ConstantClazz.LOG_TYPE_PROJECT, baseCloudProject.getPrjName(), baseCloudProject.getProjectId(), 1, baseCloudProject.getProjectId(), null);
			return reJson;
		} catch (Exception e) {
			ecmcLogService.addLog("修改项目", ConstantClazz.LOG_TYPE_PROJECT, baseCloudProject.getPrjName(), baseCloudProject.getProjectId(), 0, baseCloudProject.getProjectId(), e);
			throw e;
		}
	}
	
	@RequestMapping(value = "/createprojectonly")
	@ResponseBody
	public Object createProjectOnly(@RequestBody Map<String, Object> requestMap) throws Exception {
		EayunResponseJson reJson = new EayunResponseJson();
		Customer customer = new Customer();
		BeanUtils.mapToBean(customer, requestMap);
		CloudProject cloudProject = new CloudProject();
		BeanUtils.mapToBean(cloudProject, requestMap);
		try {
			Map<String,Object> returnMap = ecmcProjectService.createProject(cloudProject, customer, true);
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			customer = (Customer)MapUtils.getObject(returnMap, "customer");
			BaseCloudProject project = (BaseCloudProject)MapUtils.getObject(returnMap, "project");
			ecmcLogService.addLog("创建项目", ConstantClazz.LOG_TYPE_PROJECT, project.getPrjName(), project.getProjectId(), 1, project.getProjectId(), null);
		} catch (Exception e) {
			reJson.setRespCode(ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("创建项目", ConstantClazz.LOG_TYPE_PROJECT, cloudProject.getPrjName(), cloudProject.getProjectId(), 0, cloudProject.getProjectId(), e);
			throw e;
		}
		return reJson;
	}

	@RequestMapping(value = "/deleteproject")
	@ResponseBody
	public Object deleteProject(@RequestBody Map<String, Object> requestMap) throws Exception {
		EayunResponseJson reJson = new EayunResponseJson();
		String projectId = MapUtils.getString(requestMap, "projectId");
		try {
			Map<String,String> resultMap = ecmcProjectService.hasResource(projectId);
			String error = MapUtils.getString(resultMap, "error");
			if(error != null){	//项目下存在资源
				reJson.setRespCode(ConstantClazz.ERROR_CODE);
				reJson.setData(error);
			}else{
				BaseCloudProject project = ecmcProjectService.deleteProject(projectId);
				reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
				ecmcLogService.addLog("删除项目", ConstantClazz.LOG_TYPE_PROJECT, project.getPrjName(), project.getProjectId(), 1, project.getProjectId(), null);
			}
			return reJson;
		} catch (Exception e) {
			ecmcLogService.addLog("删除项目", ConstantClazz.LOG_TYPE_PROJECT, null, projectId, 0, projectId, e);
			throw e;
		}
		
	}

	@RequestMapping(value = "/getprojectpool")
	@ResponseBody
	public Object getProjectQuotaPool(@RequestBody Map<String, Object> requestMap) throws Exception {
		EayunResponseJson reJson = new EayunResponseJson();
		reJson.setRespCode(ConstantClazz.ERROR_CODE);
		String projectId = MapUtils.getString(requestMap, "projectId");
		if (!StringUtil.isEmpty(projectId)) {
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			reJson.setData(ecmcProjectService.getProjectQuotaPool(projectId));
		}
		return reJson;
	}

	@RequestMapping(value = "/getvmresource")
	@ResponseBody
	public Object getProjectVmResource(HttpServletRequest request, Page page, @RequestBody ParamsMap map) {
		log.info("获取云主机资源列表");
		String cusId = map.getParams().get("cusId").toString();
		String dcId = map.getParams().get("dcId").toString();
        String start = map.getParams().get("startTime").toString();
        String end = map.getParams().get("endTime").toString();
        String sort = map.getParams().get("sort").toString();		//DESC、ASC
        String orderBy = map.getParams().get("orderBy").toString();	//CPU核数，内存大小，开始时间，累计时长
        
        
        
        Date startTime = DateUtil.timestampToDate(start);
        Date endTime = DateUtil.timestampToDate(end);
        int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
        
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        
        page = statisticsService.getCloudVmResources(page,dcId,cusId, startTime, endTime , sort , orderBy,queryMap);
        return JSONObject.toJSONString(page);
	}

	@RequestMapping(value = "/getvolumeresource")
	@ResponseBody
	public Object getProjectVolumeResource(HttpServletRequest request, Page page, @RequestBody ParamsMap map) {
		log.info("获取云硬盘资源列表");
		String cusId = map.getParams().get("cusId").toString();
        String dcId = map.getParams().get("dcId").toString();
        String start = map.getParams().get("startTime").toString();
        String end = map.getParams().get("endTime").toString();
        String sort = map.getParams().get("sort").toString();		//DESC、ASC
        String orderBy = map.getParams().get("orderBy").toString();	//硬盘容量，开始时间，累计时长
        
        Date startTime = DateUtil.timestampToDate(start);
        Date endTime = DateUtil.timestampToDate(end);
        int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
        
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        
        page = statisticsService.getCloudVolumeResources(page,dcId,cusId, startTime, endTime , sort , orderBy,queryMap);
        return JSONObject.toJSONString(page);
	}

	@RequestMapping(value = "/getnetresource")
	@ResponseBody
	public Object getProjectNetResource(@RequestBody Map<String, Object> requestMap) {
		log.info("获取网络资源列表");
		EayunResponseJson reJson = new EayunResponseJson();
		String projectId = MapUtils.getString(requestMap, "projectId");
		String start = MapUtils.getString(requestMap, "startTime");
		String end = MapUtils.getString(requestMap, "endTime");
		Date startTime = DateUtil.timestampToDate(start);
		Date endTime = DateUtil.timestampToDate(end);
		try {
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			reJson.setData(statisticsService.getNet(projectId, startTime, endTime));
		} catch (Exception e) {
			reJson.setRespCode(ConstantClazz.ERROR_CODE);
			throw e;
		}
		return reJson;
	}
	
	//查询导出数据
	@RequestMapping(value = "/getresourcesforexcel")
    @ResponseBody
    public Object getResourcesForExcel(@RequestBody Map<String, Object> requestMap) throws Exception{
		String projectId = MapUtils.getString(requestMap, "projectId");
		String dcId = MapUtils.getString(requestMap, "dcId");
		String start = MapUtils.getString(requestMap, "startTime");
		String end = MapUtils.getString(requestMap, "endTime");
		Date startTime = DateUtil.timestampToDate(start);
		Date endTime = DateUtil.timestampToDate(end);
        
        boolean isok = statisticsService.getResourcesForExcel(dcId,projectId, startTime, endTime);
        return isok;
    }
	
	//导出Excel
	@RequestMapping("/createexcel")
	public void createExcel(HttpServletResponse response , @RequestParam("projectId")String projectId ,@RequestParam("dcId")String dcId , @RequestParam("cusId")String cusId , 
			@RequestParam("startTime")String startTime , @RequestParam("endTime")String endTime ,@RequestParam("browser")String browser , 
			@RequestParam("orderBy")String orderBy ,@RequestParam("sort")String sort,
			@RequestParam("orderByVol")String orderByVol ,@RequestParam("sortVol")String sortVol) throws Exception {
		log.info("创建Excel文档");
		Date start = DateUtil.timestampToDate(startTime);
		Date end = DateUtil.timestampToDate(endTime);
		String projectName = statisticsService.getProNameById(projectId);
		String fileName = "";
		if ("Firefox".equals(browser)) {
			fileName = new String((projectName + "资源统计报表.xls").getBytes(), "iso-8859-1");
		} else {
			fileName = URLEncoder.encode(projectName + "资源统计报表.xls", "UTF-8");
		}
		response.setContentType("application/vnd.ms-excel");
		response.addHeader("Content-Disposition", "attachment;filename=" + fileName);
		try {
			statisticsService.exportSheets(response.getOutputStream() , dcId,cusId, projectId, start, end,sort,orderBy,sortVol,orderByVol);
		} catch (Exception e) {
			log.error("导出excel失败", e);
			throw e;
		}
	}
	
	@RequestMapping("/hasprjbycusanddc")
	@ResponseBody
	public Object hasProjectByCustomerAndDc(@RequestBody Map<String, Object> requestMap){
		log.info("查看用户在选择的数据中心下是否有项目");
		EayunResponseJson reJson = new EayunResponseJson();
		reJson.setRespCode(ConstantClazz.ERROR_CODE);
		String dcId = MapUtils.getString(requestMap, "dcId");
		String cusId = MapUtils.getString(requestMap, "cusId");
		if(!StringUtils.isEmpty(dcId)&&!StringUtils.isEmpty(cusId)){
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			reJson.setData(ecmcProjectService.hasProjectByCustomerAndDc(dcId, cusId));
		}
		return reJson;
	}

}

package com.eayun.dashboard.ecmccontroller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.eayun.common.ConstantClazz;
import com.eayun.common.model.EayunResponseJson;

import org.apache.commons.collections.MapUtils;
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
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.customer.model.BaseCustomer;
import com.eayun.dashboard.ecmcservice.EcmcOverviewService;
import com.eayun.datacenter.model.DcDataCenter;
import com.eayun.sys.model.SysDataTree;
import com.eayun.virtualization.model.CloudProject;

/**
 * 总览Controller
 * 
 * @author zhouhaitao
 * @date 2016-03-23
 *
 */
@Controller
@RequestMapping("/ecmc/overview")
@Scope("prototype")
public class EcmcOverviewController {
	
	private static final Logger log = LoggerFactory.getLogger(EcmcOverviewController.class); 
	@Autowired
	private EcmcOverviewService overviewService;

	
    @RequestMapping(value = "/getresourcetypelist" , method = RequestMethod.POST)
    @ResponseBody
    public String getResourceTypeList(HttpServletRequest request)throws Exception {
    	log.info("获取总览页面资源统计中的资源类型");
        List<SysDataTree> resourceList = new ArrayList<SysDataTree>();
        try {
        	resourceList = overviewService.getResourceTypeList();
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
		}
        
    	return JSONObject.toJSONString(resourceList);
    }
    
    
	@RequestMapping(value = "/getdcresourcelist" , method = RequestMethod.POST)
    @ResponseBody
    public String getDcResourceList(HttpServletRequest request,@RequestBody DcDataCenter dc)throws Exception {
    	log.info("查询总览页面中所有数据中心的资源配额和资源使用情况");
    	List<DcDataCenter> dcList = new ArrayList<DcDataCenter>();
    	try {
    		dcList = overviewService.getDcResourceList(dc.getResourceType(),dc.getSortType());
    	} catch (Exception e) {
    	    log.error(e.getMessage(),e);
    	}
    	
    	return JSONObject.toJSONString(dcList);
    }
    
    
    @RequestMapping(value = "/getprjresourcelist" , method = RequestMethod.POST)
    @ResponseBody
    public String getPrjResourceList(HttpServletRequest request,Page page,@RequestBody ParamsMap map)throws Exception {
    	log.info("查询项目下资源的配额及使用情况");
    	try {
    		int pageSize = map.getPageSize();
            int pageNumber = map.getPageNumber();
            
            QueryMap queryMap=new QueryMap();
            queryMap.setPageNum(pageNumber);
            queryMap.setCURRENT_ROWS_SIZE(pageSize);
    		page = overviewService.getListPrjResource(page,map,queryMap);
    	} catch (Exception e) {
    	    log.error(e.getMessage(),e);
    	}
    	
    	return JSONObject.toJSONString(page);
    }
    
    @RequestMapping(value = "/getallcustomerlist" , method = RequestMethod.POST)
    @ResponseBody
    public String getAllCustomerList(HttpServletRequest request)throws Exception {
    	log.info("查询系统中所有的客户列表");
        List<BaseCustomer> custList = new ArrayList<BaseCustomer>();
        try {
        	custList = overviewService.getAllCustomerList();
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
		}
        
    	return JSONObject.toJSONString(custList);
    }
    
    @RequestMapping(value = "/getallprojectlist" , method = RequestMethod.POST)
    @ResponseBody
    public String getAllProjectList(HttpServletRequest request)throws Exception {
    	log.info("查询系统中所有的项目列表");
        List<CloudProject> prjList = new ArrayList<CloudProject>();
        try {
        	prjList = overviewService.getAllProjectList();
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
		}
        
    	return JSONObject.toJSONString(prjList);
    }
    
    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/getprojectlistbydcid" , method = RequestMethod.POST)
    @ResponseBody
    public String getprojectListByDcId(HttpServletRequest request , @RequestBody Map map)throws Exception {
    	log.info("根据数据中心ID查数据中心下所有的项目列表");
    	String dcId = map.get("dcId").toString();
        List<CloudProject> prjList = new ArrayList<CloudProject>();
        try {
        	prjList = overviewService.getprojectListByDcId(dcId);
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
		}
    	return JSONObject.toJSONString(prjList);
    }
    
    @RequestMapping(value = "/getalldclist" , method = RequestMethod.POST)
    @ResponseBody
    public String getAlldcList(HttpServletRequest request )throws Exception {
    	log.info("查询所有数据中心");
        List<DcDataCenter> dcList = new ArrayList<DcDataCenter>();
        try {
        	dcList = overviewService.getAlldcList();
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
		}
    	return JSONObject.toJSONString(dcList);
    }

	@RequestMapping(value = "/getnowtime" , method = RequestMethod.POST)
	@ResponseBody
	public String getNowTime(HttpServletRequest request ){
		EayunResponseJson json = new EayunResponseJson();
		try {
			json.setData(overviewService.getNowTime());
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		return JSONObject.toJSONString(json);
	}
	/**
	 * 查询客户类型下的所有项目数量
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/getAllPrjsToCusType")
	@ResponseBody
	public String getAllPrjsToCusType(HttpServletRequest request){
		EayunResponseJson json = new EayunResponseJson();
		try {
			json.setData(overviewService.getAllProjectsType());
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			json.setData("客户类型下的所有项目数量查询失败");
			json.setRespCode(ConstantClazz.ERROR_CODE);
		}
		return JSONObject.toJSONString(json);
	}
	/**
	 * 查询一年每月注册的用户数量
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/getMonthsNewCus")
	@ResponseBody
	public String getMonthsNewCus(HttpServletRequest request,@RequestBody Map map){
		EayunResponseJson json = new EayunResponseJson();
		try {
			json.setData(overviewService.getNowCusToMonths(map.get("type").toString()));
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			json.setData("月开通量查询失败");
			json.setRespCode(ConstantClazz.ERROR_CODE);
		}
		return JSONObject.toJSONString(json);
	}
	@RequestMapping(value = "/getYears")
	@ResponseBody
	public  String getYears(HttpServletRequest request){
		EayunResponseJson json = new EayunResponseJson();
		try {
			json.setData(overviewService.getYears());
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			json.setData("所有年份查询失败");
			json.setRespCode(ConstantClazz.ERROR_CODE);
		}
		return JSONObject.toJSONString(json);
	}
	
	/**
	 * 获取总览收入统计数据（包括全部、昨日、近7日、近30日、近90日，年）
	 * @author bo.zeng@eayun.com
	 * @param params
	 * @return
	 */
	@RequestMapping("/getincomedata")
	@ResponseBody
	public Object getIncomeData(@RequestBody Map<String, Object> params){
		String periodType = MapUtils.getString(params, "periodType");
		String searchYear = MapUtils.getString(params, "searchYear");
		return overviewService.getIncomeData(periodType, searchYear);
	}
}

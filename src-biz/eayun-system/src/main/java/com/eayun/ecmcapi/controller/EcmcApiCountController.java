package com.eayun.ecmcapi.controller;

import java.util.ArrayList;
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
import com.eayun.common.controller.BaseController;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.ecmcapi.model.ApiDefaultCount;
import com.eayun.ecmcapi.service.EcmcApiRestrictService;
import com.eayun.log.ecmcsevice.EcmcLogService;

@Controller
@RequestMapping("/ecmc/system/apicount")
public class EcmcApiCountController  extends BaseController{
	private static final Logger log = LoggerFactory.getLogger(EcmcApiCountController.class);
	@Autowired
	private EcmcApiRestrictService ecmcApiRestrictService;
	@Autowired
    private EcmcLogService ecmcLogService;
	
	/**
	 * 同步api访问限制
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/syncapicount", method = RequestMethod.POST)
	public Object syncApiCount(HttpServletRequest request) {
		EayunResponseJson json=new EayunResponseJson();
		try {
			ecmcApiRestrictService.syncApiCount();
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			json.setRespCode(ConstantClazz.ERROR_CODE);
		}
		return JSONObject.toJSONString(json);
	}
	
	/**
	 * 查询默认访问限制次数
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/getdefaultapicount", method = RequestMethod.POST)
	public Object getDefaultApiCount(HttpServletRequest request, Page page, @RequestBody ParamsMap map) throws Exception{
		try {
			String version=(String) map.getParams().get("version");
			String apiType=(String) map.getParams().get("apiType");
			int pageNumber = map.getPageNumber();
			QueryMap queryMap = new QueryMap();
			queryMap.setPageNum(pageNumber);//第几页
			queryMap.setCURRENT_ROWS_SIZE(5);//每页包含5条
			page=ecmcApiRestrictService.getApiDefaultCount(version, apiType,queryMap,page);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			throw e;
		}
		return JSONObject.toJSONString(page);
	}
	
	/**
	 * 查询默认访问限制次数
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/getdefaultapicountlist", method = RequestMethod.POST)
	public Object getDefaultApiCountList(HttpServletRequest request, @RequestBody Map map) throws Exception{
		List<ApiDefaultCount> list=new ArrayList<ApiDefaultCount>();
		try {
			String version=(String) map.get("version");
			String apiType=(String) map.get("apiType");
			list=ecmcApiRestrictService.getApiDefaultCountList(version, apiType);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			throw e;
		}
		return JSONObject.toJSONString(list);
	}
	
	/**
	 * 修改默认访问限制次数
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/updatedefaultapicount", method = RequestMethod.POST)
	public Object updateDefaultApiCountList(HttpServletRequest request, @RequestBody Map map) throws Exception{
		List<Map> list=(List<Map>) map.get("actionsList");
		String version=(String)map.get("version");
		String apiTypeName=(String)map.get("apiTypeName");
		String apiType=(String)map.get("apiType");
		EayunResponseJson json=new EayunResponseJson();
		try {
			ecmcApiRestrictService.updateApiDefaultCount(list,version,apiType);
			ecmcLogService.addLog("编辑默认API访问次数", ConstantClazz.LOG_TYPE_APIRESTRICT_DEFAULT, version+"/"+apiTypeName, null, 1, null, null);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			ecmcLogService.addLog("编辑默认API访问次数", ConstantClazz.LOG_TYPE_APIRESTRICT_DEFAULT, version+"/"+apiTypeName, null, 0, null, e);
			log.error(e.getMessage(),e);
			json.setRespCode(ConstantClazz.ERROR_CODE);
		}
		return JSONObject.toJSONString(json);
	}
}

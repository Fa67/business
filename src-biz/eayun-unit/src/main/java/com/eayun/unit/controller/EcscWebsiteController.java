package com.eayun.unit.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.controller.BaseController;
import com.eayun.common.util.BeanUtils;
import com.eayun.log.service.LogService;
import com.eayun.unit.model.BaseWebSiteInfo;
import com.eayun.unit.service.EcscWebsiteService;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2016年12月22日
 */
@Controller
@RequestMapping("/ecsc/website")
public class EcscWebsiteController extends BaseController{

	@Autowired
	private LogService logService;
	@Autowired
	private EcscWebsiteService webservice;
	
	@RequestMapping(value = "/getUnitWebsite")
	@ResponseBody
	public String getUnitWebsite(@RequestBody String unitId){
		return JSONObject.toJSONString(webservice.getUnitWebsite(unitId));
	}
	
	@RequestMapping(value = "/addWebsite")
	@ResponseBody
	public String addWebsite(@RequestBody Map<String, Object> parms) {
		BaseWebSiteInfo web = new BaseWebSiteInfo();
		List<Map<String, Object>> listmap = (List<Map<String, Object>>)parms.get("webList");
		BaseWebSiteInfo website = null;
		String IP = "";
		for(int i=0;i<listmap.size();i++){
			if(i==0){
				Map<String, Object> m = listmap.get(i);
				website = new BaseWebSiteInfo();
				BeanUtils.mapToBean(website, m);
				IP = website.getServiceIp();
			}
		}
		try {
			web = webservice.addWebsite(parms);
			logService.addLog("提交资料(新增网站)", "备案", IP, null, ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (Exception e) {
			logService.addLog("提交资料(新增网站)", "备案", IP, null, ConstantClazz.LOG_STATU_ERROR, e);
			e.printStackTrace();
		}
		return JSONObject.toJSONString(web);
	}
	
	@RequestMapping(value = "/deleteWebsite")
	@ResponseBody
	public String deleteWebsite(@RequestBody Map<String, String> map ){
		boolean istrue = false;
		try {
			webservice.deleteWebsite(map.get("webId"));
			istrue = true;
			logService.addLog("删除网站", "备案", map.get("webName"), null, ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (Exception e) {
			logService.addLog("删除网站", "备案", map.get("webName"), null, ConstantClazz.LOG_STATU_ERROR, e);
			e.printStackTrace();
		}
		return JSONObject.toJSONString(istrue);
	}
	
	@RequestMapping(value = "/copyWebsite")
	@ResponseBody
	public String copyWebsite(@RequestBody Map<String, String> map){
		BaseWebSiteInfo newweb = null;
		try {
			newweb = webservice.copyWebsite(map.get("webId"));
			logService.addLog("变更备案", "备案", map.get("webName"), null, ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (Exception e) {
			logService.addLog("变更备案", "备案", map.get("webName"), null, ConstantClazz.LOG_STATU_ERROR, e);
			e.printStackTrace();
		}
		return JSONObject.toJSONString(newweb);
	}
	
	@RequestMapping(value = "/changeWebsite")
	@ResponseBody
	public String changeWebsite(@RequestBody Map<String, Object> map){
		List<BaseWebSiteInfo> weblist = null;
		List<Map<String, Object>> listmap = (List<Map<String, Object>>)map.get("webList");
		BaseWebSiteInfo website = null;
		String IP = "";
		for(int i=0;i<listmap.size();i++){
			if(i==0){
				Map<String, Object> m = listmap.get(i);
				website = new BaseWebSiteInfo();
				BeanUtils.mapToBean(website, m);
				IP = website.getServiceIp();
			}
		}
		try {
			weblist = webservice.changeWebsite(map);			
			logService.addLog("提交资料(变更备案)", "备案", IP, null, ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (Exception e) {
			logService.addLog("提交资料(变更备案)", "备案", IP, null, ConstantClazz.LOG_STATU_ERROR, e);
			e.printStackTrace();
		}
		return JSONObject.toJSONString(weblist);
	}
	
	@RequestMapping(value = "/updateWebsite")
	@ResponseBody
	public String updateWebsite(@RequestBody Map<String, Object> parms){
		BaseWebSiteInfo web = new BaseWebSiteInfo();
		BeanUtils.copyPropertiesByModel(web, parms);
		try {
			web = webservice.updateWebsite(web);
			logService.addLog("变更备案", "备案", web.getWebName(), null, ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (Exception e) {
			logService.addLog("变更备案", "备案", web.getWebName(), null, ConstantClazz.LOG_STATU_ERROR, e);
			e.printStackTrace();
		}
		return JSONObject.toJSONString(web);
	}
}

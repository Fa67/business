package com.eayun.obs.controller;

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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eayun.accesskey.model.AccessKey;
import com.eayun.accesskey.service.AccessKeyService;
import com.eayun.common.ConstantClazz;
import com.eayun.common.controller.BaseController;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.util.ObsUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.obs.service.ObsStorageService;

@Controller
@RequestMapping("/obs/storage")
public class ObsStorageController extends BaseController {
	@Autowired
	private ObsStorageService obsStorageService;
	@Autowired
	private AccessKeyService accessKeyService;
	
	private static final Logger log = LoggerFactory.getLogger(ObsStorageController.class);
	
	@SuppressWarnings("rawtypes")
    @RequestMapping(value = "/checkStorageName" , method = RequestMethod.POST)
	@ResponseBody
	public String checkStorageName(HttpServletRequest request ,@RequestBody Map map) throws Exception {
		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
		AccessKey accessKeyObj = accessKeyService.getDefaultAK(sessionUser.getCusId());
		String bucketName = map.get("bucketName").toString();
		String folderName = map.get("folderName").toString();
		String address = map.get("address").toString();
		JSONObject object = new JSONObject();
		return JSON.toJSONString(obsStorageService.checkStorageName(bucketName ,folderName ,address ,accessKeyObj ,object));
	}
	
	@SuppressWarnings("unused")
    @RequestMapping(value = "/getStorageList" , method = RequestMethod.POST)
    @ResponseBody
    public String getStorageList(HttpServletRequest request,Page page,@RequestBody ParamsMap map) throws Exception{
	    log.info("得到对象列表");
		String uploadBucketName = "";
		String uploadFolderName = "";
		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    	AccessKey accessKeyObj = accessKeyService.getDefaultAK(sessionUser.getCusId());
		String bucketName = "";
    	String obsName = "";
    	String folderName = "";
    	if(map.getParams().containsKey("bucketName") || map.getParams().containsKey("obsName")){
    		if(!StringUtil.isEmpty(map.getParams().get("bucketName").toString())){
        		bucketName = map.getParams().get("bucketName").toString();
        		uploadBucketName = bucketName;
        	}
    		if(!StringUtil.isEmpty(map.getParams().get("obsName").toString())){
    			obsName = map.getParams().get("obsName").toString();
        	}
    		if(!StringUtil.isEmpty(map.getParams().get("folderName").toString())){
    			folderName = map.getParams().get("folderName").toString();
    			uploadFolderName = folderName;
    		}
    	}
    	
        int pageSize = map.getPageSize();
		int pageNumber = map.getPageNumber();
		QueryMap queryMap = new QueryMap();
		queryMap.setPageNum(pageNumber);
		queryMap.setCURRENT_ROWS_SIZE(pageSize);
		page = obsStorageService.getStorageList(page, queryMap, bucketName, obsName ,folderName ,accessKeyObj);
    	return JSONObject.toJSONString(page);
    }
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
    @RequestMapping(value = "/delete" , method = RequestMethod.POST)
	@ResponseBody
	public boolean delete(HttpServletRequest request,@RequestBody Map map) throws Exception {
		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    	AccessKey accessKeyObj = accessKeyService.getDefaultAK(sessionUser.getCusId());
		String bucketName = map.get("bucketName").toString();
		List<String> obsNames = (List<String>)map.get("obsNames");
		return obsStorageService.delete(bucketName, obsNames ,accessKeyObj);
	}
	
	@SuppressWarnings("rawtypes")
    @RequestMapping(value = "/add" , method = RequestMethod.POST)
	@ResponseBody
	public String add(HttpServletRequest request,@RequestBody Map map) throws Exception {
		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
		boolean result=obsStorageService.obsIsStopService(sessionUser.getCusId());
		if(!result){
	    	AccessKey accessKeyObj = accessKeyService.getDefaultAK(sessionUser.getCusId());
			String bucketName = map.get("bucketName").toString();
			String obsName = map.get("obsName").toString();
			String folderName = map.get("folderName").toString();
			JSONObject resJson = obsStorageService.add(bucketName ,folderName ,obsName ,accessKeyObj);
			return JSONObject.toJSONString(resJson);
		}else{
			JSONObject resJson =new JSONObject();
			resJson.put("resCode", "406");
    		resJson.put("resMsg", "该账户因欠费导致对象存储服务不可用");
			return JSON.toJSONString(resJson);
		}
	}
	
	@RequestMapping(value = "/initialProgressPercent" , method = RequestMethod.POST)
	@ResponseBody
	public String initialProgressPercent() throws Exception {
		JSONObject object = new JSONObject();
		obsStorageService.initialProgressPercent(object);
		return JSON.toJSONString(object);
	}
	
	@SuppressWarnings("rawtypes")
    @RequestMapping(value = "/getProgressPercent" , method = RequestMethod.POST)
	@ResponseBody
	public String getProgressPercent(HttpServletRequest request ,@RequestBody Map map) throws Exception {
		String obsName = map.get("obsName").toString();
		JSONObject object = new JSONObject();
		obsStorageService.getProgressPercent(obsName ,object);
		return JSON.toJSONString(object);
	}
	
	@SuppressWarnings("rawtypes")
    @RequestMapping(value = "/abordUpload" , method = RequestMethod.POST)
	@ResponseBody
	public boolean abordUpload(HttpServletRequest request,@RequestBody Map map) throws Exception {
		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    	AccessKey accessKeyObj = accessKeyService.getDefaultAK(sessionUser.getCusId());
    	String bucketName = map.get("bucketName").toString();
    	String obsName = map.get("obsName").toString();
		return obsStorageService.abordUpload(bucketName, obsName, accessKeyObj);
	}
	
	@SuppressWarnings("rawtypes")
    @RequestMapping(value = "/getUrl" , method = RequestMethod.POST)
	@ResponseBody
	public String getUrl(HttpServletRequest request,@RequestBody Map map) throws Exception {
		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
		boolean result=obsStorageService.obsIsStopService(sessionUser.getCusId());
		if(!result){
			AccessKey accessKeyObj = accessKeyService.getDefaultAK(sessionUser.getCusId());
			String bucketName = map.get("bucketName").toString();
			String obsName = map.get("obsName").toString();
			JSONObject object = new JSONObject();
			obsStorageService.getUrl(bucketName ,obsName ,accessKeyObj ,object);
			return JSON.toJSONString(object);
		}else{
			return JSON.toJSONString(null);
		}
	}
	
	@RequestMapping(value= "/getFolderSeparator" , method = RequestMethod.POST)
	@ResponseBody
	public String getFolderSeparator(HttpServletRequest request){
		String fileSeparator = System.getProperty("file.separator");
		return fileSeparator;
	}
	
	@SuppressWarnings("rawtypes")
    @RequestMapping(value= "/getAuthorization" , method = RequestMethod.POST)
	@ResponseBody
	public String getAuthorization(HttpServletRequest request ,@RequestBody Map map) throws Exception {
		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
		boolean result=obsStorageService.obsIsStopService(sessionUser.getCusId());
		if(!result){
			AccessKey accessKeyObj = accessKeyService.getDefaultAK(sessionUser.getCusId());
			String contentType = map.get("contentType").toString();
			String uri = map.get("uri").toString();
			String httpMethod = map.get("httpMethod").toString();
			JSONObject object = new JSONObject();
			object = obsStorageService.getAuthorization(contentType ,uri ,httpMethod,object ,accessKeyObj);
			return JSON.toJSONString(object);
		}else{
			return JSON.toJSONString(null);
		}
	}
	
	@SuppressWarnings("rawtypes")
    @RequestMapping(value = "/junkUploadIdRecycling" , method = RequestMethod.POST)
	@ResponseBody
	public void junkUploadIdRecycling(HttpServletRequest request ,@RequestBody Map map) throws Exception {
		String bucketName = map.get("bucketName").toString();
		String obsName = map.get("obsName").toString();
		String uploadId = map.get("uploadId").toString();
		obsStorageService.junkUploadIdRecycling(bucketName ,obsName ,uploadId);
	}
	
	@RequestMapping(value = "/getEayunObsHost" , method = RequestMethod.POST)
	@ResponseBody
	public String getEayunObsHost(HttpServletRequest request) {
		JSONObject object = new JSONObject();
		object.put("eayunObsHost", ObsUtil.getEayunObsHost());
		return JSON.toJSONString(object);
	}
	@RequestMapping(value = "/getobsservicestate" , method = RequestMethod.POST)
	@ResponseBody
	public String getObsServiceState(HttpServletRequest request) throws Exception{
		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
		String cusId=sessionUser.getCusId();
		boolean result=obsStorageService.obsIsStopService(cusId);
		return JSON.toJSONString(result);
	}
	@RequestMapping(value = "/getEayunOBSRequest" , method = RequestMethod.POST)
	@ResponseBody
	public String getEayunOBSRequest(HttpServletRequest request) throws Exception{
		JSONObject object = new JSONObject();
		object.put("eayunOBSRequest", ObsUtil.getRequestHeader());
		return JSON.toJSONString(object);
	}
}

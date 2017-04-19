package com.eayun.obs.controller;



import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.accesskey.model.AccessKey;
import com.eayun.accesskey.service.AccessKeyService;
import com.eayun.common.ConstantClazz;
import com.eayun.common.controller.BaseController;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.util.StringUtil;
import com.eayun.obs.model.BucketStorageBean;
import com.eayun.obs.model.BucketUesdAndRequestBean;
import com.eayun.obs.model.ObsBucket;
import com.eayun.obs.service.ObsBucketService;

@Controller
@RequestMapping("/obs/bucket")
public class ObsBucketController extends BaseController{
	@Autowired
	private ObsBucketService obsBucketService;
	@Autowired
	private AccessKeyService accessKeyService;
	
	
	/**
	 * 校验Bucket名称
	 * @param request
	 * @param 
	 * @return
	 */
	@RequestMapping(value= "/checkBucketByName", method = RequestMethod.POST)
	@ResponseBody
	public String checkBucketByName(HttpServletRequest request,@RequestBody ObsBucket bucket) throws Exception{	
		boolean flag = false;
		// 从session中获取当前用户名
    	SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    	AccessKey accessKeyObj = accessKeyService.getDefaultAK(sessionUser.getCusId());
		
		String bucketName = bucket.getBucketName();
		flag =obsBucketService.checkBucketName(bucketName, accessKeyObj);
		
		return JSONObject.toJSONString(!flag);
	}
	
	 /**
     * 查询桶列表分页
     * @author liyanchao
     * @param request
     * @param map
     * @return Page
	 * @throws Exception 
     */
    @RequestMapping(value= "/getBucketPageList" , method = RequestMethod.POST)
    @ResponseBody
    public String getBucketPageList(HttpServletRequest request,Page page,@RequestBody ParamsMap map) throws Exception{
    	// 从session中获取当前用户名
    	SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    	AccessKey accessKeyObj = accessKeyService.getDefaultAK(sessionUser.getCusId());
    	String bucketName = "";
    	if(map.getParams().containsKey("name")){
    		if(!StringUtil.isEmpty(map.getParams().get("name").toString())){
        		bucketName = map.getParams().get("name").toString();
        	}
    	}
    	
        int pageSize = map.getPageSize();
		int pageNumber = map.getPageNumber();
		QueryMap queryMap = new QueryMap();
		queryMap.setPageNum(pageNumber);
		queryMap.setCURRENT_ROWS_SIZE(pageSize);
		page = obsBucketService.getBucketPageList(page, null, queryMap, accessKeyObj, bucketName);
    	return JSONObject.toJSONString(page);
    }
    /**
     * 查询桶列表
     * @author liyanchao
     * @param request
     * @param map
     * @return bucketList
	 * @throws Exception 
     */
    @RequestMapping(value= "/getBucketList" , method = RequestMethod.POST)
    @ResponseBody
    public String getBucketList(HttpServletRequest request) throws Exception{
    	// 从session中获取当前用户名
    	SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    	AccessKey accessKeyObj = accessKeyService.getDefaultAK(sessionUser.getCusId());
    	List<ObsBucket> bucketList = obsBucketService.getBucketList(accessKeyObj);
    	return JSONObject.toJSONString(bucketList);
    }
    /**
     * 获取当前用户bucket列表&权限
     * @author liyanchao
     * @param request
     * @param map
     * @return bucketList
	 * @throws Exception 
     */
    @RequestMapping(value= "/getBucketAclList" , method = RequestMethod.POST)
    @ResponseBody
    public String getBucketAclList(HttpServletRequest request) throws Exception{
    	// 从session中获取当前用户名
    	SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    	AccessKey accessKeyObj = accessKeyService.getDefaultAK(sessionUser.getCusId());
    	List<ObsBucket> bucketAclList = obsBucketService.bucketAclList(accessKeyObj);
    	return JSONObject.toJSONString(bucketAclList);
    }
    /**创建Bucket
	 * @param request
	 * @param page
	 * @param Map
	 * @return 
	 */
	@RequestMapping(value = "/addBucket", method = RequestMethod.POST)
	@ResponseBody
	public String addBucket(HttpServletRequest request, @RequestBody Map<String,String> map)throws Exception {
		
		// 从session中获取当前用户名
    	SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    	AccessKey accessKeyObj = accessKeyService.getDefaultAK(sessionUser.getCusId());
    	JSONObject resJson = obsBucketService.addBucket(accessKeyObj, map);
		return JSONObject.toJSONString(resJson);
	}
	/**修改Bucket
	 * @param request
	 * @param page
	 * @param Map
	 * @return 
	 */
	@RequestMapping(value = "/editBucket", method = RequestMethod.POST)
	@ResponseBody
	public String editBucket(HttpServletRequest request, @RequestBody Map<String,String> map)throws Exception {
		
		// 从session中获取当前用户名
    	SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    	AccessKey accessKeyObj = accessKeyService.getDefaultAK(sessionUser.getCusId());
    	boolean flag = obsBucketService.editBucket(accessKeyObj, map);
		return JSONObject.toJSONString(flag);
	}
	
	
	
	/**
	 * 删除deleteBucket
	 * @param request
	 * @param idStr
	 * @return
	 */
	@RequestMapping(value= "/deleteBucket", method = RequestMethod.POST)
	@ResponseBody
	public String deleteBucket(HttpServletRequest request,@RequestBody Map<String,String> map) throws Exception{
//		boolean flag = false;
		// 从session中获取当前用户名
    	SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    	AccessKey accessKeyObj = accessKeyService.getDefaultAK(sessionUser.getCusId());
		String bucketName = map.get("bucketName").toString();
		JSONObject result = obsBucketService.deleteBucket(bucketName, accessKeyObj);
		return JSONObject.toJSONString(result);
	}
	/**
	 * Bucket总存储量
	 * @param request
	 * @param idStr
	 * @return
	 */
	@RequestMapping(value= "/getBucketStorage", method = RequestMethod.POST)
	@ResponseBody
	public String getBucketStorage(HttpServletRequest request,@RequestBody Map<String,String> map) throws Exception{
		List<BucketStorageBean> storageList = new ArrayList<BucketStorageBean>();
		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
		String cusId = sessionUser.getCusId();
		String bucketName = map.get("bucketName");
		storageList = obsBucketService.getBucketStorage(cusId,bucketName);
		return JSONObject.toJSONString(storageList);
	}
	/**
	 * Bucket请求次数与使用量
	 * @param request
	 * @param idStr
	 * @return
	 */
	@RequestMapping(value= "/getBucketUsedAndRequest", method = RequestMethod.POST)
	@ResponseBody
	public String getBucketUsedAndRequest(HttpServletRequest request,@RequestBody Map<String,String> map) throws Exception{
		String type = map.get("type");
		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
		String cusId = sessionUser.getCusId();
		
		List<BucketUesdAndRequestBean> usedAndRequestList = new ArrayList<BucketUesdAndRequestBean>();
		String bucketName = map.get("bucketName");
		usedAndRequestList = obsBucketService.getBucketUsedAndRequest(cusId,bucketName,type);
		return JSONObject.toJSONString(usedAndRequestList);
	}
	
}

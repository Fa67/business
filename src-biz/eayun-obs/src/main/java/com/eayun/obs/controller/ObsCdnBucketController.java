package com.eayun.obs.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import com.eayun.cdn.util.CDNConstant;
import com.eayun.common.ConstantClazz;
import com.eayun.common.controller.BaseController;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.obs.model.BucketStorageBean;
import com.eayun.obs.service.ObsCdnBucketService;

@Controller
@RequestMapping("/obs/cdn")
@Scope("prototype")
public class ObsCdnBucketController extends BaseController {
	
	@Autowired
	private ObsCdnBucketService obsCdnBucketService;
	
	private static final Logger log = LoggerFactory.getLogger(ObsCdnBucketController.class);
	
	/**
	 * 开启CDN服务
	 * @Author: duanbinbin
	 * @param request
	 * @param map
	 * @return
	 * @throws Exception
	 *<li>Date: 2016年6月15日</li>
	 */
	@SuppressWarnings("rawtypes")
    @RequestMapping(value= "/opencdn", method = RequestMethod.POST)
	@ResponseBody
	public String openCDN(HttpServletRequest request,@RequestBody Map map) throws Exception{
		log.info("开启CDN服务");
		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
		EayunResponseJson json = new EayunResponseJson();
		String bucketName = null == map.get("bucketName")?"":map.get("bucketName").toString();
		String cusId = sessionUser.getCusId();
		String cdnProvider = null == map.get("cdnProvider")?CDNConstant.cdnProvider.UpYun.toString():map.get("cdnProvider").toString();
		try {
			JSONObject jsonIsOk = obsCdnBucketService.enableDomain(bucketName, cusId,cdnProvider,sessionUser.getUserName());
			if("true".equals(jsonIsOk.getString("result"))){
				json.setRespCode(ConstantClazz.SUCCESS_CODE);
			}else if("false".equals(jsonIsOk.getString("result"))){
				json.setMessage(jsonIsOk.getString("message"));
				json.setRespCode(ConstantClazz.ERROR_CODE);
			}
		} catch (Exception e) {
			json.setMessage(e.toString());
			json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
		}
		return JSONObject.toJSONString(json);
	}
	/**
	 * 关闭CDN服务
	 * @Author: duanbinbin
	 * @param request
	 * @param map
	 * @return
	 * @throws Exception
	 *<li>Date: 2016年6月15日</li>
	 */
	@SuppressWarnings("rawtypes")
    @RequestMapping(value= "/closecdn", method = RequestMethod.POST)
	@ResponseBody
	public String closeCDN(HttpServletRequest request,@RequestBody Map map) throws Exception{
		log.info("关闭CDN服务");
		EayunResponseJson json = new EayunResponseJson();
		String bucketName = null == map.get("bucketName")?"":map.get("bucketName").toString();
		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
		String cusId = sessionUser.getCusId();
		String cdnProvider = null == map.get("cdnProvider")?CDNConstant.cdnProvider.UpYun.toString():map.get("cdnProvider").toString();
		try {
			JSONObject jsonIsOk = obsCdnBucketService.disableDomain(bucketName, cusId,cdnProvider,sessionUser.getUserName());
			if("true".equals(jsonIsOk.getString("result"))){
				json.setRespCode(ConstantClazz.SUCCESS_CODE);
			}else if("false".equals(jsonIsOk.getString("result"))){
				json.setMessage(jsonIsOk.getString("message"));
				json.setRespCode(ConstantClazz.ERROR_CODE);
			}
		} catch (Exception e) {
			json.setMessage(e.toString());
			json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
		}
		return JSONObject.toJSONString(json);
	}
	/**
	 * 获取加速后的文件URL
	 * @Author: duanbinbin
	 * @param request
	 * @param map
	 * @return
	 * @throws Exception
	 *<li>Date: 2016年6月15日</li>
	 */
	@SuppressWarnings({ "rawtypes", "unused" })
    @RequestMapping(value= "/getfilednsurl", method = RequestMethod.POST)
	@ResponseBody
	public String getFileDnsUrl(HttpServletRequest request,@RequestBody Map map) throws Exception{
		log.info("获取加速后的文件url");
		EayunResponseJson json = new EayunResponseJson();
		String bucketName = null == map.get("bucketName")?"":map.get("bucketName").toString();
		String obsName = null == map.get("obsName")?"":map.get("obsName").toString();
		try {
			String url = null;
			json.setData(url);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
		}
		return JSONObject.toJSONString(json);
	}
	
	@SuppressWarnings("rawtypes")
    @RequestMapping(value= "/getcdnflowbybucket", method = RequestMethod.POST)
	@ResponseBody
	public String getCdnFlowByBucket(HttpServletRequest request,@RequestBody Map map) throws Exception{
		log.info("获取CDN下载流量");
		EayunResponseJson json = new EayunResponseJson();
		String bucketName = null == map.get("bucketName")?"":map.get("bucketName").toString();
		String cdnProvider = null == map.get("cdnProvider")?CDNConstant.cdnProvider.UpYun.toString():map.get("cdnProvider").toString();
		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
		String cusId = sessionUser.getCusId();
		List<BucketStorageBean> cdnFlowList = new ArrayList<BucketStorageBean>();
		try {
			cdnFlowList = obsCdnBucketService.getCDNFlowData(bucketName, cusId,cdnProvider);
			json.setData(cdnFlowList);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
		}
		return JSONObject.toJSONString(json);
	}

}

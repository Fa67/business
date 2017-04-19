package com.eayun.ecmcapi.controller;

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
import com.eayun.common.model.EayunResponseJson;
import com.eayun.ecmcapi.model.ApiBlackList;
import com.eayun.ecmcapi.model.BaseApiBlackList;
import com.eayun.ecmcapi.service.EcmcBlackService;
import com.eayun.log.ecmcsevice.EcmcLogService;

@Controller
@RequestMapping("/ecmc/system/api")
public class EcmcBlackController {
	
	private static final Logger log = LoggerFactory.getLogger(EcmcBlackController.class);
	
	@Autowired
	private EcmcBlackService ecmcBlackService;
	@Autowired
    private EcmcLogService ecmcLogService;
	
	
	
	/**
	 * 查询黑名单客户
	 * @param request request请求对象
	 * @param page 分页对象
	 * @param map 分页参数
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/getBlackCustomer", method = RequestMethod.POST)
	public Object getBlackCustomer(HttpServletRequest request, Page page, @RequestBody ParamsMap map) throws Exception {
		
//		int pageSize = map.getPageSize();
		int pageNumber = map.getPageNumber();
		
		QueryMap queryMap = new QueryMap();
		queryMap.setPageNum(pageNumber);//第几页
		queryMap.setCURRENT_ROWS_SIZE(5);//每页包含5条
		
		page = ecmcBlackService.getBlackCustomer(page, queryMap);
		return JSONObject.toJSONString(page);
	}
	/**
	 * 查询黑名单IP
	 * @param request request请求对象
	 * @param page 分页对象
	 * @param map 分页参数
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/getBlackIp", method = RequestMethod.POST)
	public Object getBlackIp(HttpServletRequest request, Page page, @RequestBody ParamsMap map) throws Exception {
		
//		int pageSize = map.getPageSize();
		int pageNumber = map.getPageNumber();
		
		QueryMap queryMap = new QueryMap();
		queryMap.setPageNum(pageNumber);//第几页
		queryMap.setCURRENT_ROWS_SIZE(5);//每页包含5条
		
		page = ecmcBlackService.getBlackIp(page, queryMap);
		return JSONObject.toJSONString(page);
	}
	
	
	/**
	 * 添加黑名单
	 * @param request
	 * @param page
	 * @param Map
	 * @return 
	 */
	@RequestMapping(value = "/addBlack", method = RequestMethod.POST)
	@ResponseBody
	public Object addBlack(HttpServletRequest request, @RequestBody ApiBlackList blackList) throws Exception {
		log.info("添加黑名单"+blackList.getApiValue()+"开始");
		EayunResponseJson reJson = new EayunResponseJson();
		BaseApiBlackList baseBlack = new BaseApiBlackList();
		baseBlack = ecmcBlackService.addBlack(blackList);
		
		if(null != baseBlack){
			if("blackCus".equals(baseBlack.getApiType())){
				ecmcLogService.addLog("添加黑名单", "API黑名单", blackList.getCusOrg(), null, 1, baseBlack.getApiValue(), null);
			}else{
				ecmcLogService.addLog("添加黑名单", "API黑名单", baseBlack.getApiValue(), null, 1, null, null);
			}
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE );
		}else{
			if("blackCus".equals(baseBlack.getApiType())){
				ecmcLogService.addLog("添加黑名单", "API黑名单", blackList.getCusOrg(), null, 0, baseBlack.getApiValue(), null);
			}else{
				ecmcLogService.addLog("添加黑名单", "API黑名单", baseBlack.getApiValue(), null, 0, null, null);
			}
			reJson.setRespCode(ConstantClazz.ERROR_CODE);
		}
		log.info("添加黑名单"+blackList.getApiValue()+"结束");
		return reJson;

	}
	
	
	/**
	 * 删除黑名单
	 * @param request
	 * @param page
	 * @param Map
	 * @return 
	 */
	@RequestMapping(value = "/deleteBlack", method = RequestMethod.POST)
	@ResponseBody
	public Object deleteBlack(HttpServletRequest request, @RequestBody ApiBlackList blackList) throws Exception {
		
		EayunResponseJson reJson = new EayunResponseJson();
		
		ApiBlackList black = ecmcBlackService.getApiBlack(blackList.getApiId());
		log.info("删除黑名单"+black.getApiValue()+"开始");
		
		if(ecmcBlackService.deleteBlack(blackList.getApiId())){
			if("blackCus".equals(black.getApiType())){
				ecmcLogService.addLog("移除黑名单", "API黑名单", black.getCusOrg(), null, 1, black.getApiValue(), null);
			}else{
				ecmcLogService.addLog("移除黑名单", "API黑名单", black.getApiValue(), null, 1, null, null);
			}
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE );
		}else{
			if("blackCus".equals(black.getApiType())){
				ecmcLogService.addLog("移除黑名单", "API黑名单", black.getCusOrg(), null, 0, black.getApiValue(), null);
			}else{
				ecmcLogService.addLog("移除黑名单", "API黑名单", black.getApiValue(), null, 0, null, null);
			}
			reJson.setRespCode(ConstantClazz.ERROR_CODE);
		}
		log.info("删除黑名单"+black.getApiValue()+"结束");
		return reJson;

	}
	
	/**
     * 同步mysql黑名单到redis
     * @param request
     * @param map
     * @return
     * @throws Exception
     */
	@ResponseBody
	@RequestMapping(value = "/synchronizeBlack", method = RequestMethod.POST)
	public Object synchronizeBlack() throws Exception{
		log.info("*************同步黑名单开始*************");
		EayunResponseJson reJson = new EayunResponseJson();
		if(ecmcBlackService.synchronizeBlack()){
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		}else{
			reJson.setRespCode(ConstantClazz.ERROR_CODE);
		}
		log.info("*************同步黑名单结束*************");
		return reJson;
	}
	
	/**
     * 校验黑名单IP中是否有当前输入的IP地址
     * @param request
     * @param map
     * @return
     * @throws Exception
     */
	@ResponseBody
	@RequestMapping(value = "/checkBlackIpExist", method = RequestMethod.POST)
	public Object checkBlackIpExist(@RequestBody ApiBlackList blackList) throws Exception{
		log.info("*************校验黑名单IP是否重复开始*************");
		EayunResponseJson reJson = new EayunResponseJson();
		
		if(ecmcBlackService.checkBlackIpExist(blackList)){
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		}else{
			reJson.setRespCode(ConstantClazz.ERROR_CODE);
		}
		
		log.info("*************校验黑名单IP是否重复结束*************");
		return reJson;
	}

}

package com.eayun.log.ecmccontroller;

import java.util.Date;
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
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.model.RespJSON;
import com.eayun.common.util.DateUtil;
import com.eayun.log.controller.LogController;
import com.eayun.log.ecmcsevice.EcmcLogService;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2016年3月29日
 */
@Controller
@RequestMapping("/ecmc/system/log")
public class EcmcLogController {

	private static final Logger log = LoggerFactory.getLogger(LogController.class);
	@Autowired
	private EcmcLogService ecmclogservice;
	
    /**
     * 日志详情
     * @param prames
     * @return
     * @throws AppException
     */
    @RequestMapping(value = "/getOperLogFromMongo")
    @ResponseBody
    public Object getOperLogFromMongo(@RequestBody Map<String,String> prames) throws AppException{
    	EayunResponseJson respJson = new EayunResponseJson();
    	respJson.setRespCode(ConstantClazz.SUCCESS_CODE);
    	respJson.setData(ecmclogservice.getOneEcmcLogFromMongo(prames.get("id")));
    	return respJson;
    }
    /**
     * 将ecsc日志从数据库中转移到mongo中
     * 
     * @param request
     * @param dto
     * @return
     */
    @RequestMapping(value = "/syncLog" , method = RequestMethod.POST)
    @ResponseBody
    public String syncLog(HttpServletRequest request) throws Exception {
    	log.info("数据库日志同步到mongo开始");
    	RespJSON resp=new RespJSON();
    	try {
    		ecmclogservice.syncLog();
    		resp.setCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
    		resp.setCode(ConstantClazz.ERROR_CODE);
			log.error("数据库日志同步到mongo失败",e);
			throw e;
		}
    	return JSONObject.toJSONString(resp);
    }
    
    /**
     * 将ecmc日志从数据库中转移到mongo中
     * 
     * @param request
     * @param dto
     * @return
     */
    @RequestMapping(value = "/syncEcmcLog" , method = RequestMethod.POST)
    @ResponseBody
    public String syncEcmcLog(HttpServletRequest request) throws Exception {
    	log.info("数据库ECMC日志同步到mongo开始");
    	RespJSON resp=new RespJSON();
    	try {
    		ecmclogservice.syncEcmcLog();
    		resp.setCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
    		resp.setCode(ConstantClazz.ERROR_CODE);
			log.error("数据库日志同步到mongo失败",e);
			throw e;
		}
    	return JSONObject.toJSONString(resp);
    }
    /**
     * 操作日志查询的列表页面
     * 查询mongo中ECSC日志
     * @param request
     * @param dto
     * @return
     */
    @RequestMapping(value = "/getecscloglistbymongon" , method = RequestMethod.POST)
    @ResponseBody
    public Object getecscloglistbymongon(HttpServletRequest request,Page page,@RequestBody ParamsMap mapparams)throws Exception {
        log.info("获取ECSC日志列表开始");
        
        Date beginTime = DateUtil.timestampToDate(mapparams.getParams().get("begin") == null ? null : mapparams.getParams().get("begin").toString());
        Date endTime = DateUtil.timestampToDate(mapparams.getParams().get("end") == null ? null : mapparams.getParams().get("end").toString());
        String actItem = mapparams.getParams().get("actItem") == null ? null : mapparams.getParams().get("actItem").toString();
        String status = mapparams.getParams().get("status") == null ? null : mapparams.getParams().get("status").toString();
        String prjId = mapparams.getParams().get("prjId") == null ? null : mapparams.getParams().get("prjId").toString();
        String resourceType = mapparams.getParams().get("resourceType") == null ? null : mapparams.getParams().get("resourceType").toString();
        String ip = mapparams.getParams().get("ip") ==null ? null : mapparams.getParams().get("ip").toString();
        String resourceName = mapparams.getParams().get("resourceName") == null ? null : mapparams.getParams().get("resourceName").toString();
        String operator = mapparams.getParams().get("operator") ==null ? null : mapparams.getParams().get("operator").toString();

        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(mapparams.getPageNumber());
        queryMap.setCURRENT_ROWS_SIZE(mapparams.getPageSize());
        page = ecmclogservice.getLogListMongo(page, beginTime, endTime, actItem, status, prjId,resourceType ,ip,resourceName,operator,queryMap);
        return page;
    }
    /**
     * 操作日志查询的列表页面
     * 查询mongo中ECMC日志
     * @param request
     * @param dto
     * @return
     */
    @RequestMapping(value = "/getecmcloglistbymongon" , method = RequestMethod.POST)
    @ResponseBody
    public Object getecmcloglistbymongon(HttpServletRequest request,Page page,@RequestBody ParamsMap mapparams)throws Exception {
    	log.info("获取ECMC日志列表开始");
        
        Date beginTime = DateUtil.timestampToDate(mapparams.getParams().get("begin") == null ? null : mapparams.getParams().get("begin").toString());
        Date endTime = DateUtil.timestampToDate(mapparams.getParams().get("end") == null ? null : mapparams.getParams().get("end").toString());
        String actItem = mapparams.getParams().get("actItem") == null ? null : mapparams.getParams().get("actItem").toString();
        String status = mapparams.getParams().get("status") == null ? null : mapparams.getParams().get("status").toString();
        String prjId = mapparams.getParams().get("prjId") == null ? null : mapparams.getParams().get("prjId").toString();
        String resourceType = mapparams.getParams().get("resourceType") == null ? null : mapparams.getParams().get("resourceType").toString();
        String ip = mapparams.getParams().get("ip") ==null ? null : mapparams.getParams().get("ip").toString();
        String resourceName = mapparams.getParams().get("resourceName") == null ? null : mapparams.getParams().get("resourceName").toString();
        
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(mapparams.getPageNumber());
        queryMap.setCURRENT_ROWS_SIZE(mapparams.getPageSize());
        page = ecmclogservice.getEcmcLogListMongo(page, beginTime, endTime, actItem, status, prjId,resourceType ,ip,resourceName,queryMap);
        return page;
    }
}

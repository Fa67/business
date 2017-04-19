package com.eayun.obs.ecmccontroller;

import java.util.ArrayList;
import java.util.Date;
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
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.util.DateUtil;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.obs.ecmcmodel.EcmcObsEchartsBean;
import com.eayun.obs.ecmcmodel.EcmcObsTopModel;
import com.eayun.obs.ecmcservice.EcmcObsOverviewService;
import com.eayun.obs.model.ObsUsedType;
/*
 * ECMC1.1对象存储总览专用
 * 
 */
@Controller
@RequestMapping("/ecmc/obs/obsview")
public class EcmcObsOverviewController extends BaseController{
    private static final Logger log = LoggerFactory.getLogger(EcmcObsOverviewController.class);
	@Autowired
	private EcmcObsOverviewService ecmcObsOverviewService;
	@Autowired
	private EcmcLogService ecmcLogService;
	
	
	 /**
     * 获取24小时内新增；
     */
    @ResponseBody
    @RequestMapping(value="getObs24View" , method = RequestMethod.POST)
    public String getObs24View(HttpServletRequest request)throws AppException{
    	//此方法只涉及求和；核对无误
    	ObsUsedType out=new ObsUsedType();
    	try {
			out=ecmcObsOverviewService.getObs24Used();
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
        return JSONObject.toJSONString(out);
    }
    
    /**
     * 获取对象--存储概览
     */
    @ResponseBody
    @RequestMapping(value="getObsView" , method = RequestMethod.POST)
    public Object getObsView(HttpServletRequest request)throws AppException{
    	//此只涉及求和：请求次数、流量上传下载均核对无误。
    	ObsUsedType out=new ObsUsedType();
    	try {
			out=ecmcObsOverviewService.getObsView();
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
		}
        return out;
    }
    
    /**
     * 获取统计图;
     * type：storage/request/loadFlow为”存储量/请求次数/下载流量”
     */
    @ResponseBody
    @RequestMapping(value="getChart" , method = RequestMethod.POST)
    public Object getChart(HttpServletRequest request , @RequestBody Map<String,String> map)throws Exception{
    	//此方法核对无误
    	EcmcObsEchartsBean echartsBean = new EcmcObsEchartsBean();
    	String type = map.get("type");
    	String startTime = map.get("startTime");
    	String endTime = map.get("endTime");
    	
    	Date start = DateUtil.timestampToDate(startTime);
    	Date end = DateUtil.timestampToDate(endTime);
    	
    	String startString = DateUtil.dateToStr(start);
    	Date useStart = DateUtil.strToDate(startString);
    	
    	String endString = DateUtil.dateToStr(end);
    	Date useEnd = DateUtil.strToDate(endString);
    	echartsBean = ecmcObsOverviewService.getChart(type, useStart, useEnd);
        return echartsBean;
    } 
    
    
    /**
     * 获取统计图
     */
    @ResponseBody
    @RequestMapping(value="getThreshold" , method = RequestMethod.POST)
    public Object getThreshold(HttpServletRequest request )throws Exception{
    	ObsUsedType result=ecmcObsOverviewService.getThreshold();
        return result;
    } 
    /**
     * 设置阈值
     */
    @ResponseBody
    @RequestMapping(value= "setThreshold" , method = RequestMethod.POST)
    public String setThreshold(HttpServletRequest request, @RequestBody Map<String,String> map)throws Exception{
    	String storage=map.get("usedStorage");
    	String flow=map.get("loadDown");
    	String requestCount=map.get("requestCount");
    	JSONObject resultJson = new JSONObject();
    	
    	String result=ecmcObsOverviewService.setThreshold(storage, flow, requestCount);
    	resultJson.put("code", result);
        return resultJson.toJSONString();
    }
    
    /**
     * 获取资源排行Top10
     */
    @ResponseBody
    @RequestMapping(value= "getTop10" , method = RequestMethod.POST)
    public String getTop10(HttpServletRequest request, @RequestBody Map<String,String> map)throws Exception{
    	//上期排名，数据值核对无误
    	String type=map.get("type");
    	List<EcmcObsTopModel> list= new ArrayList<EcmcObsTopModel>();
    	list = ecmcObsOverviewService.getTop10(type);
        return JSONObject.toJSONString(list);
    }
    
    /**
     * 对象存储用户同步
     */
    @ResponseBody
    @RequestMapping(value="syncobsuser",method=RequestMethod.POST)
    public String syncObsUser(HttpServletRequest request)throws Exception{
    	EayunResponseJson eayunResponseJson=new EayunResponseJson();
    	try {
    		eayunResponseJson=ecmcObsOverviewService.syncObsUser();
    		if(ConstantClazz.SUCCESS_CODE.equals(eayunResponseJson.getRespCode())){
    			ecmcLogService.addLog("同步对象存储", "对象存储", "同步对象存储", null, 1, null, null);
    		}else{
    			ecmcLogService.addLog("同步对象存储", "对象存储", "同步对象存储", null, 0, null, null);
    		}
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			ecmcLogService.addLog("同步对象存储", "对象存储", "同步对象存储", null, 0, null, null);
		}
    	return JSONObject.toJSONString(eayunResponseJson);
    }
    
}

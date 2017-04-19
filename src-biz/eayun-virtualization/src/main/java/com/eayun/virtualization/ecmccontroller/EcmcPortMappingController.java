package com.eayun.virtualization.ecmccontroller;

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
import com.eayun.common.ConstantClazz;
import com.eayun.common.controller.BaseController;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.virtualization.ecmcservice.EcmcPortMappingService;
import com.eayun.virtualization.model.CloudPortMapping;

@Controller
@RequestMapping("/ecmc/cloud/netWork/portmapping")
@Scope("prototype")
public class EcmcPortMappingController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(EcmcPortMappingController.class);

	@Autowired
	private EcmcPortMappingService portMappingService;
	@Autowired
    private EcmcLogService ecmcLogService;
	/**
	 * 
	 * @author gaoxiang
	 * @param request
	 * @param page
	 * @param paramsMap
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	@ResponseBody
	public String getPortMappingList(HttpServletRequest request, Page page, @RequestBody ParamsMap paramsMap) throws Exception {
		String dcId = paramsMap.getParams().get("dcId").toString();
		String prjId = paramsMap.getParams().get("prjId").toString();
		String routeId = paramsMap.getParams().get("routeId").toString();
		QueryMap queryMap = new QueryMap();
		queryMap.setPageNum(paramsMap.getPageNumber());
		queryMap.setCURRENT_ROWS_SIZE(paramsMap.getPageSize());
		try {
			page = portMappingService.getPortMappingList(page, dcId, prjId, routeId, queryMap);
		} catch (Exception e) {
			throw e;
		}
		return JSONObject.toJSONString(page);
	}
	/**
	 * 添加端口映射关系
	 * @author gaoxiang
	 * @param request
	 * @param cloudPortMapping
	 * @return
	 */
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public String addPortMapping(HttpServletRequest request, @RequestBody CloudPortMapping cloudPortMapping) {
	    try {
	        cloudPortMapping = portMappingService.addPortMapping(cloudPortMapping);
	        ecmcLogService.addLog("创建端口映射", 
	                ConstantClazz.LOG_TYPE_NET, 
                    cloudPortMapping.getResourceIp()+":"+cloudPortMapping.getResourcePort()+"→"+cloudPortMapping.getDestinyIp()+":"+cloudPortMapping.getDestinyPort(), 
                    cloudPortMapping.getPrjId(), 1, cloudPortMapping.getPmId(), null);
	    } catch (Exception e) {
	        ecmcLogService.addLog("创建端口映射", 
                    ConstantClazz.LOG_TYPE_NET, 
                    cloudPortMapping.getResourceIp()+":"+cloudPortMapping.getResourcePort()+"→"+cloudPortMapping.getDestinyIp()+":"+cloudPortMapping.getDestinyPort(), 
                    cloudPortMapping.getPrjId(), 0, cloudPortMapping.getPmId(), e);
	        log.error(e.toString(),e);
	        throw e;
	    }
	    return JSONObject.toJSONString(cloudPortMapping);
	}
	
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public String updatePortMapping(HttpServletRequest request, @RequestBody CloudPortMapping cloudPortMapping) {
	    try {
	        cloudPortMapping = portMappingService.updatePortMapping(cloudPortMapping);
	        ecmcLogService.addLog("编辑端口映射", 
                    ConstantClazz.LOG_TYPE_NET, 
                    cloudPortMapping.getResourceIp()+":"+cloudPortMapping.getResourcePort()+"→"+cloudPortMapping.getDestinyIp()+":"+cloudPortMapping.getDestinyPort(), 
                    cloudPortMapping.getPrjId(), 1, cloudPortMapping.getPmId(), null);
	    } catch (Exception e) {
	        ecmcLogService.addLog("编辑端口映射", 
                    ConstantClazz.LOG_TYPE_NET, 
                    cloudPortMapping.getResourceIp()+":"+cloudPortMapping.getResourcePort()+"→"+cloudPortMapping.getDestinyIp()+":"+cloudPortMapping.getDestinyPort(), 
                    cloudPortMapping.getPrjId(), 0, cloudPortMapping.getPmId(), e);
	        log.error(e.toString(),e);
	        throw e;
	    }
	    return JSONObject.toJSONString(cloudPortMapping);
	}
	
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	@ResponseBody
	public String deletePortMapping(HttpServletRequest request, @RequestBody Map<String, String> map) {
	    String dcId = map.get("dcId").toString();
	    String prjId = map.get("prjId").toString();
	    String resourceIp = map.get("resourceIp").toString();
	    String resourcePort = map.get("resourcePort").toString();
	    String destinyIp = map.get("destinyIp").toString();
	    String destinyPort = map.get("destinyPort").toString();
	    String portMappingId = map.get("portMappingId").toString();
	    boolean flag = false;
	    try {
	        flag = portMappingService.deletePortMapping(dcId, prjId, portMappingId);
	        ecmcLogService.addLog("删除端口映射", 
                    ConstantClazz.LOG_TYPE_NET, 
                    resourceIp+":"+resourcePort+"→"+destinyIp+":"+destinyPort, 
                    prjId, 1, portMappingId, null);
	    } catch (Exception e) {
	        ecmcLogService.addLog("删除端口映射", 
                    ConstantClazz.LOG_TYPE_NET, 
                    resourceIp+":"+resourcePort+"→"+destinyIp+":"+destinyPort, 
                    prjId, 0, portMappingId, e);
	        log.error(e.toString(),e);
	        throw e;
	    }
	    return JSONObject.toJSONString(flag);
	}
	/**
     * 检查源端口是否重复
     * @param params
     * @return
     * @throws AppException
     */
    @RequestMapping("/checkresourceport")
    @ResponseBody
    public String checkResourcePort(HttpServletRequest reqeust ,@RequestBody Map<String, String> params) throws AppException {
    	EayunResponseJson responseJson = new EayunResponseJson();
    	boolean bool = portMappingService.checkResourcePort(params);
		responseJson.setRespCode(ConstantClazz.SUCCESS_CODE);
    	responseJson.setData(bool);
    	return JSONObject.toJSONString(responseJson);
    }
}

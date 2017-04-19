package com.eayun.virtualization.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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
import com.eayun.eayunstack.model.PortMapping;
import com.eayun.log.service.LogService;
import com.eayun.virtualization.model.CloudPortMapping;
import com.eayun.virtualization.service.PortMappingService;

@Controller
@RequestMapping("/cloud/netWork/portmapping")
@Scope("prototype")
public class PortMappingController extends BaseController {
    @Autowired
    private LogService logService;
	@Autowired
	private PortMappingService portMappingService;
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
	    CloudPortMapping flag = new CloudPortMapping();
	    try {
	        flag = portMappingService.addPortMapping(cloudPortMapping);
	        if (flag != null) {
	            logService.addLog( "创建端口映射", 
	                    ConstantClazz.LOG_TYPE_NET, 
	                    cloudPortMapping.getResourceIp()+":"+cloudPortMapping.getResourcePort()+"→"+cloudPortMapping.getDestinyIp()+":"+cloudPortMapping.getDestinyPort(), 
	                    cloudPortMapping.getPrjId(),
	                    ConstantClazz.LOG_STATU_SUCCESS, null);
	        }
	    } catch (Exception e) {
	        logService.addLog( "创建端口映射", 
                    ConstantClazz.LOG_TYPE_NET, 
                    cloudPortMapping.getResourceIp()+":"+cloudPortMapping.getResourcePort()+"→"+cloudPortMapping.getDestinyIp()+":"+cloudPortMapping.getDestinyPort(), 
                    cloudPortMapping.getPrjId(),
                    ConstantClazz.LOG_STATU_ERROR, e);
	        throw e;
	    }
	    return JSONObject.toJSONString(flag);
	}
	
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public String updatePortMapping(HttpServletRequest request, @RequestBody CloudPortMapping cloudPortMapping) {
	    CloudPortMapping flag = new CloudPortMapping();
	    try {
	        flag = portMappingService.updatePortMapping(cloudPortMapping);
	        if (flag != null) {
	            logService.addLog( "编辑端口映射", 
	                    ConstantClazz.LOG_TYPE_NET, 
	                    cloudPortMapping.getResourceIp()+":"+cloudPortMapping.getResourcePort()+"→"+cloudPortMapping.getDestinyIp()+":"+cloudPortMapping.getDestinyPort(), 
	                    cloudPortMapping.getPrjId(),
	                    ConstantClazz.LOG_STATU_SUCCESS, null);
	        }
	    } catch (Exception e) {
	        logService.addLog( "编辑端口映射", 
                    ConstantClazz.LOG_TYPE_NET, 
                    cloudPortMapping.getResourceIp()+":"+cloudPortMapping.getResourcePort()+"→"+cloudPortMapping.getDestinyIp()+":"+cloudPortMapping.getDestinyPort(), 
                    cloudPortMapping.getPrjId(),
                    ConstantClazz.LOG_STATU_ERROR, e);
	    }
	    return JSONObject.toJSONString(flag);
	}
	
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	@ResponseBody
	public String deletePortMapping(HttpServletRequest request, @RequestBody CloudPortMapping cloudPortMapping) {
	    boolean flag = false;
	    String dcId = cloudPortMapping.getDcId();
	    String prjId = cloudPortMapping.getPrjId();
	    String portMappingId = cloudPortMapping.getPmId();
	    try {
	        flag = portMappingService.deletePortMapping(dcId, prjId, portMappingId);
	        if (flag) {
	            logService.addLog( "删除端口映射", 
                        ConstantClazz.LOG_TYPE_NET, 
                        cloudPortMapping.getResourceIp()+":"+cloudPortMapping.getResourcePort()+"→"+cloudPortMapping.getDestinyIp()+":"+cloudPortMapping.getDestinyPort(), 
                        cloudPortMapping.getPrjId(),
                        ConstantClazz.LOG_STATU_SUCCESS, null);
	        }
	    } catch (Exception e) {
	        logService.addLog( "删除端口映射", 
                    ConstantClazz.LOG_TYPE_NET, 
                    cloudPortMapping.getResourceIp()+":"+cloudPortMapping.getResourcePort()+"→"+cloudPortMapping.getDestinyIp()+":"+cloudPortMapping.getDestinyPort(), 
                    cloudPortMapping.getPrjId(),
                    ConstantClazz.LOG_STATU_ERROR, e);
	        throw e;
	    }
	    return JSONObject.toJSONString(flag);
	}
	
	@RequestMapping(value = "/listall", method = RequestMethod.POST)
	@ResponseBody
	public String listAll(HttpServletRequest request, @RequestBody Map<String, String> map) {
	    String dcId = map.get("dcId").toString();
	    List<PortMapping> list = portMappingService.listAllPortMapping(dcId);
	    return JSONObject.toJSONString(list);
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

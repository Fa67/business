/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.virtualization.ecmccontroller;

import java.util.HashMap;
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
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.ecmcuser.util.EcmcSessionUtil;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.virtualization.ecmcservice.EcmcNetworkService;
import com.eayun.virtualization.model.CloudNetWork;

/**
 *                       
 * @Filename: EcmcNetworkController.java
 * @Description: 私有网络Controller类
 * @Version: 1.0
 * @Author: fangjun.yang
 * @Email: fangjun.yang@eayun.com
 * @History:<br>
 *<li>Date: 2016年3月31日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Controller
@RequestMapping("/ecmc/virtual/network")
@Scope("prototype")
public class EcmcNetworkController {
    
    public final static Logger log = LoggerFactory.getLogger(EcmcNetworkController.class);
    
    @Autowired
    private EcmcNetworkService ecmcNetworkService;
    @Autowired
    private EcmcLogService ecmcLogService;
    
    /**
     * 查询网络
     * @param paramsMap 
     * @param httpRequest
     * @param datacenterId
     * @param projectId
     * @param name
     * @param pageNo
     * @return
     * @throws AppException
     */
    @RequestMapping("/querynetwork")
    @ResponseBody
    public Object queryNetwork(@RequestBody ParamsMap paramsMap) throws AppException{
        try {
        	QueryMap queryMap = new QueryMap();
        	queryMap.setPageNum(paramsMap.getPageNumber());
        	queryMap.setCURRENT_ROWS_SIZE(paramsMap.getPageSize());
        	Map<String, Object> params = paramsMap.getParams() != null ? paramsMap.getParams(): new HashMap<String, Object>();
            return ecmcNetworkService.getNetworkList((String)params.get("netName"), (String)params.get("dcId"), (String)params.get("prjName"),  (String)params.get("cusOrg"), queryMap);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException("error.globe.system", e);
        }
    }
    
    /**
     * 添加网络
     * @param httpRequest
     * @param datacenter
     * @param projectId
     * @param name
     * @param cloudNetwork 
     * @return
     * @throws AppException
     */
    @RequestMapping("/addnetwork")
    @ResponseBody
    public String addNetwork(HttpServletRequest httpRequest, @RequestBody CloudNetWork cloudNetwork) throws AppException{
    	EayunResponseJson json = new EayunResponseJson();
        try {
            cloudNetwork.setCreateName(EcmcSessionUtil.getUser().getAccount());
            CloudNetWork network = ecmcNetworkService.addNetWork(cloudNetwork);
            ecmcLogService.addLog("新增私有网络", ConstantClazz.LOG_TYPE_NET, network.getNetName(), network.getPrjId(), 1, network.getNetId(), null);
            ecmcLogService.addLog("新增路由", ConstantClazz.LOG_TYPE_ROUTE, network.getRouteName(), network.getPrjId(), 1, network.getRouteId(), null);
            json.setRespCode(ConstantClazz.SUCCESS_CODE);
            json.setData(network);
        } catch (Exception e) {
        	ecmcLogService.addLog("新增私有网络", ConstantClazz.LOG_TYPE_NET, cloudNetwork.getNetName(), cloudNetwork.getPrjId(), 0, cloudNetwork.getNetId(), e);
            throw e;
        }
        
        return JSONObject.toJSONString(json);
    }
    
    /**
     * 验证网络名称是否存在
     * @param params
     * @return
     * @throws AppException
     */
    @RequestMapping("/checknetworkname")
    @ResponseBody
    public String checkNetworkName(@RequestBody Map<String, String> params) throws AppException{
    	EayunResponseJson resJson = new EayunResponseJson();
    	try {
			resJson.setData(ecmcNetworkService.checkNetworkName( null,params.get("prjId"), params.get("netName"), params.get("netId")));
			resJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			throw e;
		}
    	return JSONObject.toJSONString(resJson);
    }
    
    /**
     * 修改网络
     * @param dcId
     * @param prjId
     * @param netId
     * @param netNamex
     * @return
     * @throws AppException
     */
    @RequestMapping("/modnetwork")
    @ResponseBody
    public String modNetwork(HttpServletRequest request,@RequestBody CloudNetWork cloudNetWork) throws AppException{
    	EayunResponseJson json = new EayunResponseJson();
        try {
            cloudNetWork.setCreateName(EcmcSessionUtil.getUser().getAccount());
            CloudNetWork net = ecmcNetworkService.updateNetwork(cloudNetWork);
            ecmcLogService.addLog("编辑私有网络", ConstantClazz.LOG_TYPE_NET, cloudNetWork.getNetName(), cloudNetWork.getPrjId(), 1, cloudNetWork.getNetId(), null);
            json.setRespCode(ConstantClazz.SUCCESS_CODE);
            json.setData(net);
        }catch (AppException e) {
        	ecmcLogService.addLog("编辑私有网络", ConstantClazz.LOG_TYPE_NET, cloudNetWork.getNetName(), cloudNetWork.getPrjId(), 0, cloudNetWork.getNetId(), e);
            throw e;
        } catch (Exception e) {
        	ecmcLogService.addLog("编辑私有网络", ConstantClazz.LOG_TYPE_NET, cloudNetWork.getNetName(), cloudNetWork.getPrjId(), 0, cloudNetWork.getNetId(), e);
            log.error(e.getMessage(), e);
            throw new AppException("error.globe.system", e);
        }
        return JSONObject.toJSONString(json);
    }
    
    /**
     * 删除网络前验证
     * @param networkid
     * @return
     * @throws AppException
     */
    @RequestMapping("/checkfordel")
    @ResponseBody
    public String checkForDel(@RequestBody Map<String, String> params) throws AppException{
    	EayunResponseJson json = ecmcNetworkService.checkForDel(params.get("networkId"));
        return JSONObject.toJSONString(json);
        
    }
    
    /**
     * 删除网络
     * @param cloudNetWork 
     * @return
     * @throws AppException
     */
    @RequestMapping("/deletenetwork")
    @ResponseBody
    public String deleteNetwork(@RequestBody CloudNetWork cloudNetWork) throws AppException{
    	EayunResponseJson json = new EayunResponseJson();
        try{
        	if (ecmcNetworkService.deleteNetwork(cloudNetWork)) {
        		json.setRespCode(ConstantClazz.SUCCESS_CODE);
        		ecmcLogService.addLog("删除私有网络", ConstantClazz.LOG_TYPE_NET, cloudNetWork.getNetName(), cloudNetWork.getPrjId(), 1, cloudNetWork.getNetId(), null);
        		ecmcLogService.addLog("删除路由", ConstantClazz.LOG_TYPE_ROUTE, cloudNetWork.getRouteName(), cloudNetWork.getPrjId(), 1, cloudNetWork.getRouteId(), null);
        	}else {	
        		json.setRespCode(ConstantClazz.ERROR_CODE);
        		json.setMessage("网络已被使用，不允许删除!");
        	}
        }catch(Exception e){
        	ecmcLogService.addLog("删除私有网络", ConstantClazz.LOG_TYPE_NET, cloudNetWork.getNetName(), cloudNetWork.getPrjId(), 0, cloudNetWork.getNetId(), e);
        	throw e;
        }
        return JSONObject.toJSONString(json);
    }
    
    /**
     * 获取网络详情
     * @param id
     * @return
     * @throws AppException
     */
    @RequestMapping("/getnetworkbyid")
    @ResponseBody
    public String getNetworkById(@RequestBody Map<String, String> params) throws AppException{
    	EayunResponseJson responseJson = new EayunResponseJson();
        try{
        	responseJson.setData(ecmcNetworkService.getNetworkById(params.get("netId")));
        	responseJson.setRespCode(ConstantClazz.SUCCESS_CODE);
        }catch (Exception e) {
            log.error(e.toString(),e);
			throw e;
		}
        return JSONObject.toJSONString(responseJson);
    }
    
    /**
     * 查询项目下未绑定路由的子网
     * @param params
     * @return
     * @throws AppException
     */
    @RequestMapping("/getnotbindroutenetwork")
    @ResponseBody
    public String getNotBindRouteNetwork(@RequestBody Map<String, String> params) throws AppException{
    	EayunResponseJson responseJson = new EayunResponseJson();
        try{
        	responseJson.setData(ecmcNetworkService.getNotBindRouteNetworkByPrjId(params.get("prjId")));
        	responseJson.setRespCode(ConstantClazz.SUCCESS_CODE);
        }catch (Exception e) {
            log.error(e.toString(),e);
			throw e;
		}
        return JSONObject.toJSONString(responseJson);
    }
    
    /**
     * 获取项目下的私有网络列表
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getnetworklistbyprjid", method = RequestMethod.POST)
    @ResponseBody
    public String getNetWorkListByPrjId(@RequestBody Map<String, String> params) throws Exception {
        EayunResponseJson responseJson = new EayunResponseJson();
        try{
        	responseJson.setData(ecmcNetworkService.getNetworkListByPrjId(params.get("prjId")));
        	responseJson.setRespCode(ConstantClazz.SUCCESS_CODE);
        }catch (Exception e) {
            log.error(e.toString(),e);
			throw e;
		}
        return JSONObject.toJSONString(responseJson);
    }
    
}

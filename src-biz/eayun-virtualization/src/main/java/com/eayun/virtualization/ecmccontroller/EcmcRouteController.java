/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
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
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.ecmcuser.util.EcmcSessionUtil;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.virtualization.ecmcservice.EcmcRouteService;
import com.eayun.virtualization.model.BaseCloudSubNetWork;
import com.eayun.virtualization.model.CloudRoute;

/**
 *                       
 * @Filename: EcmcRouteController.java
 * @Description: 
 * @Version: 1.0
 * @Author: fangjun.yang
 * @Email: fangjun.yang@eayun.com
 * @History:<br>
 *<li>Date: 2016年4月6日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Controller
@RequestMapping("/ecmc/virtual/route")
@Scope("prototype")
public class EcmcRouteController {

    private final static Logger log = LoggerFactory.getLogger(EcmcRouteController.class);

    @Autowired
    private EcmcRouteService    ecmcRouteService;
    @Autowired
    private EcmcLogService ecmcLogService;
    
    @RequestMapping("/queryroute")
    @ResponseBody
    public Object queryRoute(@RequestBody ParamsMap paramsMap) throws AppException {
        log.info("查询路由开始");
        Map<String, Object> params = paramsMap.getParams();
        QueryMap queryMap = new QueryMap();
        queryMap.setPageNum(paramsMap.getPageNumber());
        queryMap.setCURRENT_ROWS_SIZE(paramsMap.getPageSize());
        return ecmcRouteService.queryRoute((String) params.get("datacenterId"), (String) params.get("prjName"), (String) params.get("cusOrg"), (String) params.get("name"), queryMap);
    }

    /**
     * 用于校验路由名称是否已经存在
     * @param request
     * @param page
     * @return
     * @throws AppException
     */
    @RequestMapping("/checkroutename")
    @ResponseBody
    public String checkRouteName(HttpServletRequest request, @RequestBody CloudRoute cloudRoute) throws AppException {
        log.info("校验路由名次开始");
        EayunResponseJson resultJson = new EayunResponseJson();
        try {
            if (cloudRoute.getRouteName() == null || "".equals(cloudRoute.getRouteName())) {
            	resultJson.setData(false);
            } else {
                resultJson.setData(ecmcRouteService.checkRouteName(cloudRoute.getDcId(), cloudRoute.getPrjId(), cloudRoute.getRouteName(), cloudRoute.getRouteId()));
            }
            resultJson.setRespCode(ConstantClazz.SUCCESS_CODE);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException("error.openstack.system", e);
        }
        return JSONObject.toJSONString(resultJson);
    }

    /**
     * 创建路由
     * @param cloudRoute
     * @return
     * @throws AppException
     */
    @RequestMapping("/addroute")
    @ResponseBody
    public String addRoute(@RequestBody CloudRoute cloudRoute) throws AppException {
    	EayunResponseJson resultJson = new EayunResponseJson();
        log.info("创建路由开始");
        try{
        	cloudRoute.setCreateName(EcmcSessionUtil.getUser().getAccount());
        	ecmcRouteService.addRoute(cloudRoute);
        	ecmcLogService.addLog("创建路由", ConstantClazz.LOG_TYPE_ROUTE, cloudRoute.getRouteName(), cloudRoute.getPrjId(), 1, cloudRoute.getRouteId(), null);
        	resultJson.setData(cloudRoute);
        	resultJson.setRespCode(ConstantClazz.SUCCESS_CODE);
        }catch(Exception e){
        	ecmcLogService.addLog("创建路由", ConstantClazz.LOG_TYPE_ROUTE, cloudRoute.getRouteName(), cloudRoute.getPrjId(), 0, cloudRoute.getRouteId(), e);
        	throw e;
        }
        return JSONObject.toJSONString(resultJson);
    }

    /**
     * 检测路由带宽
     * @param prjId
     * @return
     * @throws AppException
     */
    @RequestMapping("/checkrouterate")
    @ResponseBody
    public String checkRouteRate(@RequestBody Map<String, String> param) throws AppException {
    	EayunResponseJson resultJson = new EayunResponseJson();
        log.info("检测路由带宽开始");
        try{
        	Map<String, Object> map = ecmcRouteService.getRouteRateInfo(param.get("prjId"));
        	resultJson.setData(map);
        	resultJson.setRespCode(ConstantClazz.SUCCESS_CODE);
        }catch(Exception e){
        	throw e;
        }
        return JSONObject.toJSONString(resultJson);
    }

    /**
     * 修改路由
     * @param cloudRoute
     * @return
     * @throws AppException
     */
    @RequestMapping("/updateroute")
    @ResponseBody
    public String updateRoute(@RequestBody CloudRoute cloudRoute) throws AppException {
    	EayunResponseJson resultJson = new EayunResponseJson();
        log.info("修改路由开始");
        try {
            ecmcRouteService.updateRoute(cloudRoute);
            ecmcLogService.addLog("编辑路由", ConstantClazz.LOG_TYPE_ROUTE, cloudRoute.getRouteName(), cloudRoute.getPrjId(), 1, cloudRoute.getRouteId(), null);
            resultJson.setData(cloudRoute);
        	resultJson.setRespCode(ConstantClazz.SUCCESS_CODE);
        } catch (Exception e) {
        	ecmcLogService.addLog("编辑路由", ConstantClazz.LOG_TYPE_ROUTE, cloudRoute.getRouteName(), cloudRoute.getPrjId(), 0, cloudRoute.getRouteId(), e);
            log.error(e.getMessage(), e);
            throw e;
        }
        return JSONObject.toJSONString(resultJson);
    }

    /**
     * 设置网关
     * @param cloudRoute
     * @return
     * @throws AppException
     */
    @RequestMapping("/setgateway")
    @ResponseBody
    public String setGateWay(@RequestBody CloudRoute cloudRoute) throws AppException {
    	EayunResponseJson resultJson = new EayunResponseJson();
        log.info("设置网关开始");
        try {
            ecmcRouteService.setGateWay(cloudRoute.getRouteId(), cloudRoute.getNetId(), cloudRoute.getDcId());
            ecmcLogService.addLog("设置网关", ConstantClazz.LOG_TYPE_ROUTE, cloudRoute.getRouteName(), cloudRoute.getPrjId(), 1, cloudRoute.getRouteId(), null);
            resultJson.setRespCode(ConstantClazz.SUCCESS_CODE);
        } catch (Exception e) {
        	ecmcLogService.addLog("设置网关", ConstantClazz.LOG_TYPE_ROUTE, cloudRoute.getRouteName(), cloudRoute.getPrjId(), 0, cloudRoute.getRouteId(), e);
            log.error(e.getMessage(), e);
            throw e;
        }
        return JSONObject.toJSONString(resultJson);
    }

    /**
     * 判断网关是否允许移除
     * @param routeId
     * @return
     * @throws AppException
     */
    @RequestMapping("/checkforclear")
    @ResponseBody
    public String checkForCle(@RequestBody String routeId) throws AppException {
    	EayunResponseJson json = ecmcRouteService.checkForCle(routeId);
        return JSONObject.toJSONString(json);
    }
    /**
     * 移除网关
     * @param cloudRoute
     * @return
     * @throws AppException
     */
    @RequestMapping("/removegateway")
    @ResponseBody
    public String removeGateway(@RequestBody CloudRoute cloudRoute) throws AppException {
    	EayunResponseJson resultJson = new EayunResponseJson();
        log.info("清除网关开始");
        try {
            ecmcRouteService.removeGateway(cloudRoute.getRouteId(), cloudRoute.getDcId());
            ecmcLogService.addLog("清除网关", ConstantClazz.LOG_TYPE_ROUTE, cloudRoute.getRouteName(), cloudRoute.getPrjId(), 1, cloudRoute.getRouteId(), null);
            resultJson.setRespCode(ConstantClazz.SUCCESS_CODE);
        } catch (Exception e) {
        	ecmcLogService.addLog("清除网关", ConstantClazz.LOG_TYPE_ROUTE, cloudRoute.getRouteName(), cloudRoute.getPrjId(), 0, cloudRoute.getRouteId(), e);
            log.error(e.getMessage(), e);
            throw e;
        }
        return JSONObject.toJSONString(resultJson);
    }

    /**
     * 绑定子网
     * @param routeId
     * @param subNetworkId
     * @param datacenterId
     * @param request
     * @return
     * @throws AppException
     */
    @RequestMapping("/attachsubnet")
    @ResponseBody
    public String attachSubnet(@RequestBody Map<String, String> params) throws AppException {
        log.info("绑定子网开始");
        EayunResponseJson reJson = new EayunResponseJson();
        try {
            BaseCloudSubNetWork subnet = ecmcRouteService.attachSubnet(params.get("routeId"), params.get("dcId"), params.get("subNetworkId"));
            if (subnet != null && subnet.getRouteId() != null) {
                reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
                reJson.setData(subnet);
                ecmcLogService.addLog("连接路由", ConstantClazz.LOG_TYPE_ROUTE,params.get("routeName"), params.get("prjId"), 1, params.get("routeId"), null);
            }
        } catch (AppException e) {
        	ecmcLogService.addLog("连接路由", ConstantClazz.LOG_TYPE_ROUTE,params.get("routeName"), params.get("prjId"), 0, params.get("routeId"), e);
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ecmcLogService.addLog("连接路由", ConstantClazz.LOG_TYPE_ROUTE,params.get("routeName"), params.get("prjId"), 0, params.get("routeId"), e);
            throw new AppException("error.globe.system", e);
        }
        return JSONObject.toJSONString(reJson);
    }

    /**
     * 解绑子网
     * @param params
     * @return
     * @throws AppException
     */
    @RequestMapping("/detachsubnet")
    @ResponseBody
    public String detachSubnet(@RequestBody Map<String, String> params) throws AppException {
    	EayunResponseJson reJson = new EayunResponseJson();
        log.info("解绑子网开始");
        try {
            ecmcRouteService.detachSubnet(params.get("routeId"), params.get("subNetworkId"), params.get("dcId"));
            ecmcLogService.addLog("断开路由", ConstantClazz.LOG_TYPE_ROUTE,params.get("routeName"), params.get("prjId"), 1, params.get("routeId"), null);
            reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ecmcLogService.addLog("断开路由", ConstantClazz.LOG_TYPE_ROUTE,params.get("routeName"), params.get("prjId"), 0, params.get("routeId"), e);
            throw e;
        }
        return JSONObject.toJSONString(reJson);
    }

    /**
     * 删除路由
     * @param cloudRoute
     * @return
     * @throws AppException
     */
    @RequestMapping("/deleteroute")
    @ResponseBody
    public String deleteRoute(@RequestBody CloudRoute cloudRoute) throws AppException {
    	EayunResponseJson reJson = new EayunResponseJson();
        log.info("删除路由开始");
        try {
            if (ecmcRouteService.deleteRoute(cloudRoute.getDcId(), cloudRoute.getRouteId())) {
            	ecmcLogService.addLog("删除路由", ConstantClazz.LOG_TYPE_ROUTE, cloudRoute.getRouteName(), cloudRoute.getPrjId(), 1, cloudRoute.getRouteId(), null);
            	reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
            }
        } catch (Exception e) {
        	ecmcLogService.addLog("删除路由", ConstantClazz.LOG_TYPE_ROUTE, cloudRoute.getRouteName(), cloudRoute.getPrjId(), 0, cloudRoute.getRouteId(), e);
            log.error(e.getMessage(), e);
            throw e;
        }
        return JSONObject.toJSONString(reJson);
    }

    /**
     * 查询路由详情
     * @param params
     * @return
     * @throws AppException
     */
    @RequestMapping("/getroutedetailbyid")
    @ResponseBody
    public String getRouteDetailById(@RequestBody Map<String, String> params) throws AppException {
    	EayunResponseJson reJson = new EayunResponseJson();
        log.info("获取路由详情开始");
        try{
        	CloudRoute route = ecmcRouteService.findRouteDetailById(params.get("routeId"));
        	reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
        	reJson.setData(route);
        }catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        return JSONObject.toJSONString(reJson);
    }

    /**
     * 路由子网列表 ---修改为：私有网络下的受管子网,并且分页
     * @param paramsMap
     * @return
     * @throws AppException
     */
    @RequestMapping("/queryroutesubnetwork")
    @ResponseBody
    public String queryRouteSubNetwork(HttpServletRequest request, Page page, @RequestBody ParamsMap paramsMap) throws AppException {
    	String dcId = paramsMap.getParams().get("dcId").toString();
		String routeId = paramsMap.getParams().get("routeId").toString();
		QueryMap queryMap = new QueryMap();
		queryMap.setPageNum(paramsMap.getPageNumber());
		queryMap.setCURRENT_ROWS_SIZE(paramsMap.getPageSize());
        try {
            page = ecmcRouteService.getSubnetList(page,dcId, routeId,queryMap);
        } catch (AppException e) {
            throw e;
        }
        return JSONObject.toJSONString(page);
    }
    @RequestMapping("/checkDetachSubnet")
    @ResponseBody
    public String checkDetachSubnet(@RequestBody String subnetId) {
    	EayunResponseJson json = ecmcRouteService.checkDetachSubnet(subnetId);
        return JSONObject.toJSONString(json);
    }
    protected EayunResponseJson newResultJson(String code) {
        EayunResponseJson resultJson = new EayunResponseJson();
        resultJson.setRespCode(code);
        return resultJson;
    }
    
}

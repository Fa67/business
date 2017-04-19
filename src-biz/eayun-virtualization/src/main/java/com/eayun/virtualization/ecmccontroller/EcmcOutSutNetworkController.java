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
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.virtualization.ecmcservice.EcmcOutSubNetworService;
import com.eayun.virtualization.model.CloudSubNetWork;
import com.eayun.virtualization.model.EcmcCloudSubNetwork;

/**
 *                       
 * @Filename: EcmcOutSutNetworkController.java
 * @Description: 
 * @Version: 1.0
 * @Author: fangjun.yang
 * @Email: fangjun.yang@eayun.com
 * @History:<br>
 *<li>Date: 2016年4月5日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Controller
@RequestMapping("/ecmc/virtual/outsubnetwork")
@Scope("prototype")
public class EcmcOutSutNetworkController {

    private final static Logger log = LoggerFactory.getLogger(EcmcOutSutNetworkController.class);
    
    @Autowired
    private EcmcOutSubNetworService ecmcOutSubNetworService;
    @Autowired
    private EcmcLogService ecmcLogService;
    
    /**
     * 查询子网
     * @return
     * @throws AppException
     */
    @RequestMapping("/querysubnetwork")
    @ResponseBody
    public String querySubNetwork(HttpServletRequest reqeust ,@RequestBody Map<String, String> params) throws AppException{
        log.info("查询外网子网开始");
        EayunResponseJson responseJson = new EayunResponseJson();
        responseJson.setData(ecmcOutSubNetworService.getOutSubNetworkList(params));
        responseJson.setRespCode(ConstantClazz.SUCCESS_CODE);
        return JSONObject.toJSONString(responseJson);
    }
    
    /**
     * 添加子网
     * @return
     * @throws AppException
     */
    @RequestMapping("/addsubnetwork")
    @ResponseBody
    public String addSubNetwork(HttpServletRequest reqeust ,@RequestBody EcmcCloudSubNetwork ecmcCloudSubNetwork) throws AppException{
        log.info("添加外网子网开始");
        EayunResponseJson responseJson = new EayunResponseJson();
        try {
        	ecmcCloudSubNetwork=ecmcOutSubNetworService.addSubNetwork(ecmcCloudSubNetwork);
        	if( ecmcCloudSubNetwork!= null){
            	responseJson.setRespCode(ConstantClazz.SUCCESS_CODE);
            	responseJson.setData(ecmcCloudSubNetwork);
            	ecmcLogService.addLog("添加外网子网", ConstantClazz.LOG_TYPE_SUBNET, ecmcCloudSubNetwork.getSubnetName(), ecmcCloudSubNetwork.getPrjId(), 1, ecmcCloudSubNetwork.getSubnetId(), null);
            }
		} catch (AppException e) {
			responseJson.setRespCode(ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("添加外网子网", ConstantClazz.LOG_TYPE_SUBNET, ecmcCloudSubNetwork.getSubnetName(), ecmcCloudSubNetwork.getPrjId(), 0, ecmcCloudSubNetwork.getSubnetId(), e);
			throw e;
		}
        return JSONObject.toJSONString(responseJson);
    }
    
    /**
     * 修改外网子网
     * @return
     * @throws AppException
     */
    @RequestMapping("/updatesubnetwork")
    @ResponseBody
    public String updateSubNetwork(HttpServletRequest reqeust ,@RequestBody Map<String,String> map) throws AppException{
    	EayunResponseJson responseJson = new EayunResponseJson();
        EcmcCloudSubNetwork subNetwork = null;
        try {
        	subNetwork = ecmcOutSubNetworService.updateSubNetwork(map);
        	if( subNetwork!= null){
            	responseJson.setRespCode(ConstantClazz.SUCCESS_CODE);
            	responseJson.setData(subNetwork);
            	ecmcLogService.addLog("编辑外网子网", ConstantClazz.LOG_TYPE_SUBNET, subNetwork.getSubnetName(), subNetwork.getPrjId(), 1, subNetwork.getSubnetId(), null);
            }
		} catch (AppException e) {
			responseJson.setRespCode(ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("编辑外网子网", ConstantClazz.LOG_TYPE_SUBNET, map.get("subnetName"),null, 0, map.get("subnetId"), e);
			throw e;
		}
        return JSONObject.toJSONString(responseJson);
    }
    
    /**
     * 删除子网
     * @return
     * @throws AppException
     */
    @RequestMapping("/deletesubnetwork")
    @ResponseBody
    public String deleteSubNetwork(HttpServletRequest reqeust ,@RequestBody Map<String, String> params) throws AppException{
    	EayunResponseJson responseJson = new EayunResponseJson();
    	try {
    		boolean bool = ecmcOutSubNetworService.deleteSubNetwork(params);
        	if( bool){
            	responseJson.setRespCode(ConstantClazz.SUCCESS_CODE);
            	ecmcLogService.addLog("删除外网子网", ConstantClazz.LOG_TYPE_SUBNET, params.get("subNetName"), params.get("prjId"), 1, params.get("subNetId"), null);
            }
		} catch (AppException e) {
			responseJson.setRespCode(ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("删除外网子网", ConstantClazz.LOG_TYPE_SUBNET, params.get("subNetName"), params.get("prjId"), 0, params.get("subNetId"), e);
			throw e;
		}
        return JSONObject.toJSONString(responseJson);
    }
    
    /**
     * 检查外网子网是否重名
     * @param params
     * @return
     * @throws AppException
     */
    @RequestMapping("/checkoutsubnetname")
    @ResponseBody
    public String checkOutSubnetName(HttpServletRequest reqeust ,@RequestBody Map<String, String> params) throws AppException {
    	EayunResponseJson responseJson = new EayunResponseJson();
    	boolean bool =ecmcOutSubNetworService.checkOutSubnetName(params);
		responseJson.setRespCode(ConstantClazz.SUCCESS_CODE);
    	responseJson.setData(bool);
    	return JSONObject.toJSONString(responseJson);
    }
    
}

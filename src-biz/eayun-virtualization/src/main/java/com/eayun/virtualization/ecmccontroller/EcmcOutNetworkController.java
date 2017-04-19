/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.virtualization.ecmccontroller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;

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
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.virtualization.ecmcservice.EcmcOutNetworkService;
import com.eayun.virtualization.model.BaseCloudNetwork;
import com.eayun.virtualization.model.EcmcCloudNetwork;

/**
 * 外部网络Controller类                      
 * @Filename: EcmcOutNetworkController.java
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
@RequestMapping("/ecmc/virtual/networkout")
@Scope("prototype")
public class EcmcOutNetworkController {

    private final static Logger   log = LoggerFactory.getLogger(EcmcOutNetworkController.class);

    @Autowired
    private EcmcOutNetworkService ecmcOutNetworkService;
    @Autowired
    private EcmcLogService ecmcLogService;
    /**
     * 查询外部网络列表
     * @param datacenterId
     * @param pageNo
     * @param pageSize
     * @return
     * @throws AppException
     */
    @RequestMapping("/queryoutnetwork")
    @ResponseBody
    public String queryOutNetwork(HttpServletRequest request,Page page,@RequestBody ParamsMap paramsMap) throws AppException {
        log.info("查询外部网络列表开始");
        page=ecmcOutNetworkService.getOutNetworkList(page,paramsMap);
        return JSONObject.toJSONString(page);
    }
    /**
     * 外部网络重名验证
     * @param request
     * @param map
     * @return
     */
    @RequestMapping("/checknetname")
    @ResponseBody
    public String checkNetName(HttpServletRequest request,@RequestBody Map<String,String> map){
    	boolean bool = ecmcOutNetworkService.checkNetName(map);
    	EayunResponseJson reJson = new EayunResponseJson();
		reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		reJson.setData(bool);
    	return JSONObject.toJSONString(reJson);
    }
    /**
     * 修改外部网络
     * @param networkId
     * @param datacenterId
     * @param netName
     * @param admStateup
     * @return
     * @throws AppException
     */
    @RequestMapping("/modoutnetwork")
    @ResponseBody
    public Object modOutNetwork(HttpServletRequest request,@RequestBody Map<String, String> params) throws AppException {
        log.info("修改外部网络开始");
        EayunResponseJson responseJson = new EayunResponseJson();
        EcmcCloudNetwork network =null;
        try {
        	network = ecmcOutNetworkService.modifyOutNetwork(params);
			if(network != null){
	        	responseJson.setData(network);
	        	responseJson.setRespCode(ConstantClazz.SUCCESS_CODE);
	        	ecmcLogService.addLog("修改外部网络", ConstantClazz.LOG_TYPE_NET, network.getNetName(), network.getPrjId(), 1, network.getNetId(), null);
	        }
		} catch (AppException e) {
			responseJson.setRespCode(ConstantClazz.ERROR_CODE);
			if(network!=null){
			    ecmcLogService.addLog("修改外部网络", ConstantClazz.LOG_TYPE_NET, network.getNetName(), network.getPrjId(), 0, network.getNetId(), e);
			}
			throw e;
		}
        return responseJson;
    }

    /**
     * 下拉选择列表
     * @param params
     * @return
     * @throws AppException
     */
    @RequestMapping("/selectoutnet")
    @ResponseBody
    public Object selectOutNet(HttpServletRequest request,@RequestBody Map<String, String> params) throws AppException {
        //此处需要从本地ＤＢ获得网络list,然后执行下面代码在页面展示网络下拉框
        List<EcmcCloudNetwork> listNetWork = ecmcOutNetworkService.getAllOutNetworkList(params);
        EayunResponseJson reJson = new EayunResponseJson();
		reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		reJson.setData(listNetWork);
        return JSONObject.toJSON(reJson);
    }
    
    @RequestMapping("/getoutnetworkdetail")
    @ResponseBody
    public Object getOutNetworkDetail(HttpServletRequest request,@RequestBody Map<String, String> params) throws AppException{
    	EcmcCloudNetwork cloudNetWork=ecmcOutNetworkService.getCloudNetworkById(params);
    	EayunResponseJson reJson = new EayunResponseJson();
		reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		reJson.setData(cloudNetWork);
        return JSONObject.toJSON(reJson);
    }
}

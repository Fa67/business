/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.virtualization.ecmccontroller;

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
import com.eayun.common.ConstantClazz;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.datacenter.service.DataCenterService;
import com.eayun.ecmcuser.util.EcmcSessionUtil;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.virtualization.ecmcservice.EcmcSubNetworkService;
import com.eayun.virtualization.model.BaseCloudSubNetWork;
import com.eayun.virtualization.model.CloudSubNetWork;
import com.eayun.virtualization.model.EcmcCloudSubNetwork;
import com.eayun.virtualization.service.SubNetWorkService;

@Controller
@RequestMapping("/ecmc/virtual/subnetwork")
@Scope("prototype")
public class EcmcSubNetworkController {

    private final static Logger   log = LoggerFactory.getLogger(EcmcSubNetworkController.class);

    @Autowired
    private EcmcSubNetworkService ecmcSubNetworkService;

    @Autowired
    private SubNetWorkService     subNetService;
    @Autowired
    private EcmcLogService ecmcLogService;
    @Autowired
	private DataCenterService dataCenterService;

    /**
     * 获取子网详情
     * @param params
     * @return
     * @throws AppException
     */
    @RequestMapping("/getsubnetworkbyid")
    @ResponseBody
    public Object getSubNetworkById(@RequestBody Map<String, String> params) throws AppException {
        log.info("获取子网详情开始");
        return ecmcSubNetworkService.getSubNetworkById(params.get("netid"));
    }

    /**
     * 添加子网
     * @param request
     * @param reqVo
     * @return
     * @throws AppException
     */
    @RequestMapping("/addsubnetwork")
    @ResponseBody
    public String addSubNetwork(HttpServletRequest request, @RequestBody CloudSubNetWork reqVo) throws AppException {
        log.info("添加子网开始");
        EayunResponseJson resJson = new EayunResponseJson();
        CloudSubNetWork network =new CloudSubNetWork();
        try{
        	String createName = EcmcSessionUtil.getUser().getAccount();
        	reqVo.setCreateName(createName);
        	network = ecmcSubNetworkService.addSubNetwork(reqVo);
        	if(network.getSubnetType().equals("1")){
        		ecmcLogService.addLog("增加受管子网", ConstantClazz.LOG_TYPE_NET, reqVo.getSubnetName(), reqVo.getPrjId(), 1, network.getSubnetId(), null);
        	}else if(network.getSubnetType().equals("0")){
        		ecmcLogService.addLog("增加自管子网", ConstantClazz.LOG_TYPE_NET, reqVo.getSubnetName(), reqVo.getPrjId(), 1, network.getSubnetId(), null);
        	}
        	resJson.setRespCode(ConstantClazz.SUCCESS_CODE);
        	resJson.setData(network);
        }
        catch(Exception e){
        	if(reqVo.getSubnetType().equals("1")){
        		ecmcLogService.addLog("增加受管子网", ConstantClazz.LOG_TYPE_NET, reqVo.getSubnetName(), reqVo.getPrjId(), 0, network.getSubnetId(), e);
        	}else if(reqVo.getSubnetType().equals("0")){
        		ecmcLogService.addLog("增加自管子网", ConstantClazz.LOG_TYPE_NET, reqVo.getSubnetName(), reqVo.getPrjId(), 0, network.getSubnetId(), e);
        	}
        	throw e;
        }
        return JSONObject.toJSONString(resJson);
    }

    /**
     * 验证子网IP是否存在 存在返回：true,失败返回：false
     * @param subnetIP
     * @param netId
     * @return
     * @throws AppException
     */
    @RequestMapping("/checksubnetipbynet")
    @ResponseBody
    public String checkSubNetIPByNet(@RequestBody Map<String, String> params) throws AppException {
    	EayunResponseJson resJson = new EayunResponseJson();
        log.info("验证子网IP开始");
        if(ecmcSubNetworkService.checkSameSubNetIP(params.get("subnetIP"), params.get("netId"))){
        	resJson.setRespCode(ConstantClazz.ERROR_CODE);
        	resJson.setMessage("网络地址重复");
        }
        else{
        	resJson.setRespCode(ConstantClazz.SUCCESS_CODE);
        }
        return JSONObject.toJSONString(resJson);
    }

    /**
     * 修改子网
     * @param request
     * @param cloudSubNetWork
     * @return
     */
    @RequestMapping(value = "updatesubnetwork", method = RequestMethod.POST)
    @ResponseBody
    public String updateSubNetWork(HttpServletRequest request, @RequestBody CloudSubNetWork cloudSubNetWork) {
    	EayunResponseJson responseJson = new EayunResponseJson();
        try {
            responseJson.setData(ecmcSubNetworkService.updateSubNetwork(cloudSubNetWork));
            responseJson.setRespCode(ConstantClazz.SUCCESS_CODE);
            if(cloudSubNetWork.getSubnetType().equals("1")){
        		ecmcLogService.addLog("编辑受管子网", ConstantClazz.LOG_TYPE_NET, cloudSubNetWork.getSubnetName(), cloudSubNetWork.getPrjId(), 1, cloudSubNetWork.getSubnetId(), null);
        	}else if(cloudSubNetWork.getSubnetType().equals("0")){
        		ecmcLogService.addLog("编辑自管子网", ConstantClazz.LOG_TYPE_NET, cloudSubNetWork.getSubnetName(), cloudSubNetWork.getPrjId(), 1, cloudSubNetWork.getSubnetId(), null);
        	}
        } catch (Exception e) {
        	if(cloudSubNetWork.getSubnetType().equals("1")){
        		ecmcLogService.addLog("编辑受管子网", ConstantClazz.LOG_TYPE_NET, cloudSubNetWork.getSubnetName(), cloudSubNetWork.getPrjId(), 0, cloudSubNetWork.getSubnetId(), e);
        	}else if(cloudSubNetWork.getSubnetType().equals("0")){
        		ecmcLogService.addLog("编辑自管子网", ConstantClazz.LOG_TYPE_NET, cloudSubNetWork.getSubnetName(), cloudSubNetWork.getPrjId(), 0, cloudSubNetWork.getSubnetId(), e);
        	}
            throw e;
        }
        return JSONObject.toJSONString(responseJson);
    }

    /**
     * 删除子网
     * @param cloudSubNetWork 
     * @param id
     * @param datacenterId
     * @return
     * @throws AppException
     */
    @RequestMapping("/deletesubnetwork")
    @ResponseBody
    public String deleteSubNetwork(@RequestBody CloudSubNetWork cloudSubNetWork) throws AppException {
        log.info("删除子网开始");
        EayunResponseJson responseJson = new EayunResponseJson();
        try{
        	boolean flag = subNetService.daleteCloudSubNet(cloudSubNetWork);
        	if(flag){
        		if(cloudSubNetWork.getSubnetType().equals("1")){
            		ecmcLogService.addLog("删除受管子网", ConstantClazz.LOG_TYPE_NET, cloudSubNetWork.getSubnetName(), cloudSubNetWork.getPrjId(), 1, cloudSubNetWork.getSubnetId(), null);
            	}else if(cloudSubNetWork.getSubnetType().equals("0")){
            		ecmcLogService.addLog("删除自管子网", ConstantClazz.LOG_TYPE_NET, cloudSubNetWork.getSubnetName(), cloudSubNetWork.getPrjId(), 1, cloudSubNetWork.getSubnetId(), null);
            	}
        		responseJson.setRespCode(ConstantClazz.SUCCESS_CODE);
        	}
        	else{
        		responseJson.setRespCode(ConstantClazz.ERROR_CODE);
        		responseJson.setMessage("子网IP已被占用，不能删除");
        	}
        }catch(Exception e){
        	if(cloudSubNetWork.getSubnetType().equals("1")){
        		ecmcLogService.addLog("删除受管子网", ConstantClazz.LOG_TYPE_NET, cloudSubNetWork.getSubnetName(), cloudSubNetWork.getPrjId(), 0, cloudSubNetWork.getSubnetId(), e);
        	}else if(cloudSubNetWork.getSubnetType().equals("0")){
        		ecmcLogService.addLog("删除自管子网", ConstantClazz.LOG_TYPE_NET, cloudSubNetWork.getSubnetName(), cloudSubNetWork.getPrjId(), 0, cloudSubNetWork.getSubnetId(), e);
        	}
        	throw e;
        }
        return JSONObject.toJSONString(responseJson);
    }

    /**
     * 检查子网是否重名
     * @param dcId
     * @param prjId
     * @param subnetId
     * @param subnetName
     * @return
     * @throws AppException
     */
    @RequestMapping("/checksubnetworkname")
    @ResponseBody
    public String checkSubNetWorkName(@RequestBody CloudSubNetWork cloudSubNetWork) throws AppException {
        log.info("检查子网名称是否重名开始");
        EayunResponseJson responseJson = new EayunResponseJson();
        try{
        	responseJson.setData(ecmcSubNetworkService.checkSubNetWorkName(cloudSubNetWork.getDcId(), cloudSubNetWork.getPrjId(), cloudSubNetWork.getSubnetName(), cloudSubNetWork.getSubnetId()));
        	responseJson.setRespCode(ConstantClazz.SUCCESS_CODE);
        }catch (Exception e) {
			log.error(e.toString(),e);
			throw e;
		}
        return JSONObject.toJSONString(responseJson);
    }

    /**
     * 获取指定网络下的子网
     * @param request
     * @param netId
     * @return
     */
    @RequestMapping(value = "getsubnetlistbynetid", method = RequestMethod.POST)
    @ResponseBody
    public String getSubNetListByNetId(HttpServletRequest request, @RequestBody Map<String, String> params) {
    	log.info("获取网络下的子网开始");
    	EayunResponseJson responseJson = new EayunResponseJson();
    	String netId = null == params.get("netId")?"":params.get("netId");
    	List<EcmcCloudSubNetwork> sunnetList = new ArrayList<EcmcCloudSubNetwork>();
    	try {
			sunnetList = ecmcSubNetworkService.getSubNetListByNetId(netId);
			responseJson.setData(sunnetList);
	    	responseJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			log.error(e.toString(),e);
			throw e;
		}
    	
    	return JSONObject.toJSONString(responseJson);
    }
    
    /**
     * 获取项目下的子网下拉列表
     * @param request
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getsubnetlistbyprjid", method = RequestMethod.POST)
	@ResponseBody
	public String getSubnetListByPrjId(HttpServletRequest request,@RequestBody Map<String, String> params) throws Exception{
    	EayunResponseJson responseJson = new EayunResponseJson();
		try {			
			List<BaseCloudSubNetWork> list = subNetService.querySubnetList(params.get("dcId"),params.get("prjId")); 
			responseJson.setData(list);
			responseJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		}catch (Exception e) {
			throw e;
		}
		return JSONObject.toJSONString(responseJson);
	}
    
    /**
     * 查询路由可连接子网
     * @param request
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getnotbinroutesubnetlist", method = RequestMethod.POST)
	@ResponseBody
	public String getNotBinRouteSubnetList(HttpServletRequest request,@RequestBody Map<String, String> params) throws Exception{
    	EayunResponseJson responseJson = new EayunResponseJson();
		try {	
			List<CloudSubNetWork> data = ecmcSubNetworkService.getNotBindRouteSubnetList(params.get("dcId"),params.get("prjId"), params.get("netWorkId")); 
			responseJson.setData(data);
			responseJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		}catch (Exception e) {
			throw e;
		}
		return JSONObject.toJSONString(responseJson);
	}
    /**
     * 获取所在数据中心下的dns
     * @author gaoxiang
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value = "/getdatacenter", method = RequestMethod.POST)
    @ResponseBody
    public String getDnsByDcId(HttpServletRequest request, @RequestBody Map<String, String> map) {
        JSONObject json = new JSONObject();
        String dcId = map.get("dcId").toString();
        BaseDcDataCenter datacenter = dataCenterService.getById(dcId);
        json.put("datacenter", datacenter);
        return json.toJSONString();
    }
    /**
     * 判断子网是否允许删除
     * @param routeId
     * @return
     * @throws AppException
     */
    @RequestMapping("/checkfordel")
    @ResponseBody
    public String checkForDel(@RequestBody Map<String, String> params) throws AppException {
    	EayunResponseJson json = ecmcSubNetworkService.checkForDel(params);
        return JSONObject.toJSONString(json);
    }
}

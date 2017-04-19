package com.eayun.virtualization.controller;

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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.controller.BaseController;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.util.StringUtil;
import com.eayun.log.service.LogService;
import com.eayun.virtualization.model.BaseCloudNetwork;
import com.eayun.virtualization.model.BaseCloudRoute;
import com.eayun.virtualization.model.BaseCloudSubNetWork;
import com.eayun.virtualization.model.CloudRoute;
import com.eayun.virtualization.service.RouteService;

@Controller
@RequestMapping("/cloud/route")
@Scope("prototype")
public class RouteController extends BaseController{
	private static final Logger log = LoggerFactory
			.getLogger(RouteController.class);
	@Autowired
	private LogService logService;
	@Autowired
	private RouteService routeService;
	
	/**
     * 验证重名 创建、编辑时
     * @author liyanchao
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value= "/getRouteById" , method = RequestMethod.POST)
    @ResponseBody
    public String getGroupById(HttpServletRequest request,@RequestBody Map map){
		boolean isTrue=false;
    	try{
    		Map project = (Map)map.get("project");
    		String dcId="";
    		String routeName="";
    		String routeId="";
    		//用于创建时判断重名
    		if(null!=project){
    			dcId=project.get("dcId").toString();
        		routeName=map.get("name").toString();
    		}else{
    			//用于编辑时时判断重名
    			dcId=map.get("dcId").toString();
    			routeName=map.get("name").toString();
    			routeId=map.get("routeId").toString();
    		}
    		
    		isTrue=routeService.getRouteById(dcId,routeId,routeName);
    	}catch(AppException e){
    		throw e;
    	}
		return JSONObject.toJSONString(isTrue);
    	
    }
    /**
     * 创建页面中查询当前项目下已有路由所有的带宽
     * @author liyanchao
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value= "/getHaveBandCount" , method = RequestMethod.POST)
    @ResponseBody
    public String getHaveBandCount(HttpServletRequest request,@RequestBody Map<String,String> map){
    	String prjId = map.get("prjId");
    	String routeId ="";
    	if(!StringUtil.isEmpty(map.get("routeId"))){
    		routeId =map.get("routeId");
    	}
    	return JSONObject.toJSONString(routeService.getHaveBandCount(prjId,routeId));
    }
    /**
     * 创建页面中查询当前项目设置的路由带宽的配额
     * @author liyanchao
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value= "/getPrjBandCount" , method = RequestMethod.POST)
    @ResponseBody
    public String getPrjBandCount(HttpServletRequest request,@RequestBody Map<String,String> map){
    	String prjId = map.get("prjId");
    	return JSONObject.toJSONString(routeService.getPrjBandCount(prjId));
    }
    /**
     * 获取已使用带宽和订单中待创建和待支付的私有网络的带宽之和
     * @author gaoxiang
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value = "/getqosnumbyprjid", method = RequestMethod.POST)
    @ResponseBody
    public String getQosNumByPrjId(HttpServletRequest request, @RequestBody Map<String, String> map) {
        JSONObject json = new JSONObject();
        String prjId = map.get("prjId");
        int bandCount = routeService.getQosNumByPrjId(prjId);
        json.put("respData", bandCount);
        return json.toJSONString();
    }
	
	/**
	 * 获取外部网络列表
	 * @param id 路由id
	 * @param request
	 * @return
	 * @throws AppException 
	 */
	@RequestMapping(value = "/getOutNetList", method = RequestMethod.POST)
	@ResponseBody
	public String getOutNetList(HttpServletRequest request,@RequestBody Map map) throws Exception{	//之前此方法参数只有datacenterId	
		JSONArray jArray = new JSONArray();
		String dcId = map.get("dcId").toString();
		try {
			//此处需要从本地ＤＢ获得网络list,然后执行下面代码在页面展示网络下拉框
			List<BaseCloudNetwork> listNetWork=routeService.getOutNetList(dcId);
			 
			 if (listNetWork!= null && listNetWork.size()>0){
				 for (int i=0;i<listNetWork.size();i++){
					 BaseCloudNetwork network = listNetWork.get(i);
					 String netid = network.getNetId();
					 String name = network.getNetName();
					 JSONObject jsonObject = new JSONObject();
					 jsonObject.put("value", netid);
					 jsonObject.put("text", name);
					 jArray.add(jsonObject);
				 }
			 }
			 
			 
		}catch (Exception e) {
			
			throw e;
		}	
		return JSONObject.toJSONString(jArray);
	}
	
	/**
	 * 查询本地路由信息
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/getRouteList", method = RequestMethod.POST)
	@ResponseBody
	public String getRouteList(HttpServletRequest request,Page page, @RequestBody ParamsMap map) throws Exception{
		String prjId = map.getParams().get("prjId").toString();
		 String dcId=map.getParams().get("dcId").toString();
		 String routeName="";
		try {
			if(map.getParams().containsKey("name")){
				routeName=map.getParams().get("name").toString();
			}
			int pageSize = map.getPageSize();
			int pageNumber = map.getPageNumber();
			QueryMap queryMap = new QueryMap();
			queryMap.setPageNum(pageNumber);
			queryMap.setCURRENT_ROWS_SIZE(pageSize);
			page  =routeService.getRouteList(page,dcId,prjId,routeName,queryMap);
			
			
		}catch (Exception e) {
			
			throw e;
		}
		return JSONObject.toJSONString(page);
	}
	
	/**
	 * 添加路由
	 * @param networkVoe
	 * @param request
	 * @return String
	 * @throws Exception 
	 */
	@RequestMapping(value= "/addRoute", method = RequestMethod.POST)
	@ResponseBody  
	public String addRoute(HttpServletRequest request, @RequestBody Map map) throws Exception{
		BaseCloudRoute resultData =new BaseCloudRoute();
		String routeName = map.get("name").toString();
		String prjId =  ((Map)map.get("project")).get("projectId").toString();
		try {
			//执行创建操作
			resultData = routeService.addRoute(request,map);	
			logService.addLog("创建路由",ConstantClazz.LOG_TYPE_ROUTE,  routeName, prjId,
					ConstantClazz.LOG_STATU_SUCCESS, null);
		}catch (Exception e) {
			logService.addLog("创建路由",ConstantClazz.LOG_TYPE_ROUTE,  routeName, prjId,
					ConstantClazz.LOG_STATU_ERROR, e);
			throw e;
		}
		return JSONObject.toJSONString(resultData);
	}
	/**
     * 验证重名 创建、编辑时
     * @author liyanchao
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value= "/getRouteByIdOrName" , method = RequestMethod.POST)
    @ResponseBody
    public String getRouteByIdOrName(HttpServletRequest request,@RequestBody Map map) throws Exception{
		boolean isTrue=false;
    	try{
    		Map project = (Map)map.get("project");
    		String dcId="";
    		String routeName="";
    		String routeId="";
    		//用于创建时判断重名
    		if(null!=project){
    			dcId=project.get("dcId").toString();
    			routeName=map.get("name").toString();
    		}else{
    			//用于编辑时时判断重名
    			dcId=map.get("dcId").toString();
    			routeName=map.get("name").toString();
    			routeId=map.get("sgId").toString();
    		}
    		
    		isTrue=routeService.getRouteByIdOrName(dcId,routeId,routeName);
    		
    	}catch(Exception e){
    		
    		throw e;
    	}
		return JSONObject.toJSONString(isTrue);
    	
    }
    /**
	 * 编辑路由
	 * @param networkVoe
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value= "/editRoute", method = RequestMethod.POST)
	@ResponseBody
	public String editRoute(String id,String datacenterId, HttpServletRequest request,@RequestBody Map<String,String> map) throws Exception{
		BaseCloudRoute baseRoute = null;
		String dcId = map.get("dcId").toString();
		String routeId = map.get("routeId").toString();
		String routeName = map.get("routeName").toString();
		String prjId = map.get("prjId").toString();
		try {	
			baseRoute = routeService.editRoute(dcId,routeId,routeName, map);			
			logService.addLog( "编辑路由", ConstantClazz.LOG_TYPE_ROUTE, routeName, prjId,
					ConstantClazz.LOG_STATU_SUCCESS, null);
		}catch (Exception e) {
			logService.addLog("编辑路由", ConstantClazz.LOG_TYPE_ROUTE, routeName, prjId,
					ConstantClazz.LOG_STATU_ERROR, e);
    		throw e;
		}
		return JSONObject.toJSONString(baseRoute);	
	}
	
    /**
	 * 设置网关
	 * @param RouteVoe
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value= "/addGateWay", method = RequestMethod.POST)
	@ResponseBody
	public String addGateWay(HttpServletRequest request,@RequestBody Map map) throws AppException{
		CloudRoute routeVoe = null;
		String dcId = map.get("dcId").toString();
		String routeId = map.get("routeId").toString();
		String outNetWorkId = map.get("outNetId").toString();
		String prjId = map.get("prjId").toString();
		String netName = map.get("netName").toString();
		EayunResponseJson json = new EayunResponseJson();
		try {
			//执行创建操作
			routeVoe = routeService.setGateWay(routeId,dcId, outNetWorkId);
			logService.addLog("设置网关",ConstantClazz.LOG_TYPE_NET,  netName, prjId,
					ConstantClazz.LOG_STATU_SUCCESS, null);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			json.setData(routeVoe);
		}catch (Exception e) {
		    json.setRespCode(ConstantClazz.ERROR_CODE);
			logService.addLog("设置网关",ConstantClazz.LOG_TYPE_NET,  netName, prjId,
					ConstantClazz.LOG_STATU_ERROR, e);
    		throw e;
		}
		return JSONObject.toJSONString(json);		
	}
	/**
	 * 清除网关
	 * @param RouteVoe
	 * @param request
	 * @return
	 * @throws AppException 
	 */
	@RequestMapping(value= "/deleteGateway", method = RequestMethod.POST)
	@ResponseBody  
	public String deleteGateway(HttpServletRequest request,@RequestBody Map map) throws AppException{		
		BaseCloudRoute resultData = null;
		String dcId = map.get("dcId").toString();
		String routeId = map.get("routeId").toString();
		String netName = map.get("netName").toString();
		String prjId =  map.get("prjId").toString();
		EayunResponseJson json = new EayunResponseJson();
		try {
			resultData = routeService.deleteGateway(routeId, dcId);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			json.setData(resultData);
			logService.addLog("清除网关",ConstantClazz.LOG_TYPE_NET,  netName, prjId,
					ConstantClazz.LOG_STATU_SUCCESS, null);		
		}catch (Exception e) {
		    json.setRespCode(ConstantClazz.ERROR_CODE);
			logService.addLog("清除网关",ConstantClazz.LOG_TYPE_NET,  netName, prjId,
					ConstantClazz.LOG_STATU_ERROR, e);
    		throw e;
		}
		return JSONObject.toJSONString(json);
	}
	/**
	 * 路由连接内子网
	 * @param RouteVoe
	 * @param request
	 * @return
	 * @throws AppException 
	 */
	@RequestMapping(value= "/connectSubnet", method = RequestMethod.POST)
	@ResponseBody
	public String connectSubnet(HttpServletRequest request,@RequestBody Map map) throws Exception{
		BaseCloudSubNetWork subnet = new BaseCloudSubNetWork();
		String dcId = map.get("dcId").toString();
		String routeId = map.get("routeId").toString();
		String subnetworkId = map.get("subNetId").toString();
		String subnetName = map.get("subnetName").toString();
		String subnetCidr = map.get("subnetCidr").toString();
		String routeName = map.get("routeName").toString();
		String prjId =  map.get("prjId").toString();
		try {
			//执行创建操作
			subnet = routeService.connectSubnet(dcId, routeId, subnetworkId);
			logService.addLog("连接路由",ConstantClazz.LOG_TYPE_SUBNET,  subnetName+"("+subnetCidr+")", prjId,
					ConstantClazz.LOG_STATU_SUCCESS, null);
		}catch (Exception e) {
			logService.addLog("连接路由",ConstantClazz.LOG_TYPE_SUBNET,  subnetName+"("+subnetCidr+")", prjId,
					ConstantClazz.LOG_STATU_ERROR, e);
    		throw e;
		}
		return JSONObject.toJSONString(subnet);		
	}
	/**
     * 根据prjId、dcId、routeId获取Entity
     * @author liyanchao
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value= "/getRouteDetail" , method = RequestMethod.POST)
    @ResponseBody
    public String getRouteDetail(HttpServletRequest request,@RequestBody Map map){
    	CloudRoute routeVoe=new CloudRoute();
    	try{
    		String dcId="";
    		String prjId="";
    		String routeId="";
    		dcId=map.get("dcId").toString();
    		prjId=map.get("prjId").toString();
    		routeId=map.get("routeId").toString();
    		routeVoe=routeService.getRouteDetail(dcId, prjId, routeId);
    	}catch(AppException e){
    		throw e;
    	}
		return JSONObject.toJSONString(routeVoe);
    	
    }
    /**
	 *解除路由子网操作
	 * @param RouteVoe
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value= "/detachSubnet" , method = RequestMethod.POST)
	@ResponseBody
	public String detachSubnet(HttpServletRequest request,@RequestBody Map map) throws Exception{
		String dcId = map.get("dcId").toString();
		String subNetId = map.get("subnetId").toString();
		String subnetName = map.get("subnetName").toString();
        String subnetCidr = map.get("subnetCidr").toString();
		String routeId = map.get("routeId").toString();
		String prjId = map.get("prjId").toString();
		BaseCloudSubNetWork resultData = new BaseCloudSubNetWork();
		try {
			
		    resultData = routeService.detachSubnet(dcId,routeId, subNetId);
		    logService.addLog("断开路由",ConstantClazz.LOG_TYPE_SUBNET,  subnetName+"("+subnetCidr+")", prjId,
					ConstantClazz.LOG_STATU_SUCCESS, null);
		}catch (Exception e) {
			logService.addLog("断开路由",ConstantClazz.LOG_TYPE_SUBNET,  subnetName+"("+subnetCidr+")", prjId,
					ConstantClazz.LOG_STATU_ERROR, e);
    		throw e;
		}
		return JSONObject.toJSONString(resultData);	
	}
	
	/**
	 * 删除路由
	 * @param request
	 * @param idStr
	 * @return
	 */
	@RequestMapping(value= "/deleteRoute", method = RequestMethod.POST)
	@ResponseBody
	public String deleteRoute(HttpServletRequest request,String id,String datacenterId,@RequestBody Map<String,String> map) throws AppException{	
		String dcId = map.get("dcId").toString();
		String routeId = map.get("routeId").toString();
		String prjId = map.get("prjId").toString();
		String routeName = map.get("routeName").toString();
		String qosId ="";
		if(!StringUtil.isEmpty(map.get("qosId"))){
			qosId = map.get("qosId").toString();
		}
		try{
			boolean flag = routeService.delete(dcId, routeId,qosId);
			logService.addLog("删除路由", ConstantClazz.LOG_TYPE_ROUTE,  routeName, prjId,
					ConstantClazz.LOG_STATU_SUCCESS, null);	
			return JSONObject.toJSONString(flag);
		}catch (Exception e) {
			logService.addLog("删除路由", ConstantClazz.LOG_TYPE_ROUTE,  routeName, prjId,
					ConstantClazz.LOG_STATU_ERROR, e);
    		throw e;
		}
	}
	
	/**
     * 判断网关是否允许移除
     * @param netId
     * @return
     * @throws AppException
     */
    @RequestMapping("/checkforclear")
    @ResponseBody
    public String checkForCle(@RequestBody String netId) throws AppException {
    	EayunResponseJson json = routeService.checkForCle(netId);
        return JSONObject.toJSONString(json);
    }
    /**
     * 判断子网是否能够解绑路由
     * @param subnetId
     * @return
     */
    @RequestMapping("/checkDetachSubnet")
    @ResponseBody
    public String checkDetachSubnet(@RequestBody Map<String,String> map) {
    	String subnetId = map.get("subnetId") == null ? "" : map.get("subnetId").toString();
		EayunResponseJson json = routeService.checkDetachSubnet(subnetId);
        return JSONObject.toJSONString(json);
    }
    
}

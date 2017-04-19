package com.eayun.virtualization.controller;

import java.io.IOException;
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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.controller.BaseController;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.datacenter.service.DataCenterService;
import com.eayun.log.service.LogService;
import com.eayun.virtualization.model.BaseCloudSubNetWork;
import com.eayun.virtualization.model.CloudSubNetWork;
import com.eayun.virtualization.service.SubNetWorkService;

@Controller
@RequestMapping("/cloud/subnetwork")
@Scope("prototype")
public class SubNetWorkController extends BaseController{
	private static final Logger log = LoggerFactory.getLogger(SubNetWorkController.class);
	@Autowired
	private LogService logService;
	@Autowired
	private SubNetWorkService subNetService;
	@Autowired
	private DataCenterService dataCenterService;
	/**
	 * 获取内部网络，用于下拉选项
	 * @param id 路由id
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value = "/getSelectInnerNet", method = RequestMethod.POST)
	@ResponseBody
	public String getSelectInnerNet(HttpServletRequest request,String id,String datacenterId,String projectId,@RequestBody Map map) throws Exception{		
		JSONArray jArray = new JSONArray();
		String dcId = map.get("dcId").toString();
		String prjId = map.get("projectId").toString();
		try {
			 List<CloudSubNetWork> listVoe = subNetService.getInnerNetList(dcId, prjId);
			 
			 if (listVoe!= null && listVoe.size()>0){
				 for (int i=0;i<listVoe.size();i++){
					 CloudSubNetWork voe = listVoe.get(i);
					 String subnetid = voe.getSubnetId();					 
					 String name = voe.getSubnetName() + ":" + voe.getCidr() + "(" + voe.getNetName() + ")";
					 JSONObject jsonObject = new JSONObject();
					 jsonObject.put("value", subnetid);
					 jsonObject.put("text", name);
					 jArray.add(jsonObject);
				 }
			 }
			 return jArray.toString();
		}catch (Exception e) {
			throw e;
		}
	}
	/**
	 * 查询路由子网列表信息
	 * @param request
	 * @return
	 * @throws Exception+
	 */
	@RequestMapping(value = "/getSubnetWorksByRouteId", method = RequestMethod.POST)
	@ResponseBody
	public String getSubnetWorksByRouteId(HttpServletRequest request,@RequestBody Map map) throws Exception{
		String dcId = map.get("dcId").toString();
		String prjId = map.get("prjId").toString();
		String routeId = map.get("routeId").toString();
		List<CloudSubNetWork> listport = new ArrayList<CloudSubNetWork>();
		try {
			//查询路由下面的所有子网
			listport = subNetService.getSubnetList(dcId,prjId,routeId);
		}catch (Exception e) {
			throw e;
		}
		
		return JSONObject.toJSONString(listport);
	}
	/**
	 * 根据datacenterId，projectId，查找所有内网的子网，用于资源池创建选择的下拉选项
	 * @param request
	 * @param page
	 * @return 子网名称，子网id，网络id
	 * @throws AppException
	 */
	@RequestMapping(value = "/getSubnetList", method = RequestMethod.POST)
	@ResponseBody
	public String getSubnetList(HttpServletRequest request,@RequestBody Map map) throws Exception{
		//表示切换项目查询子网
				String dcId="";
				String prjId = "";
				if(map.containsKey("project")){
					dcId=((Map)map.get("project")).get("dcId").toString();
					prjId = ((Map)map.get("project")).get("projectId").toString();
				}else{//表示默认在创建页面展示第一条项目的子网
					dcId = map.get("dcId").toString();
					prjId = map.get("projectId").toString();
				}
		
		List<BaseCloudSubNetWork> listT = new ArrayList<BaseCloudSubNetWork>();
		try {			
			listT = subNetService.querySubnetList(dcId,prjId); 
			
		}catch (Exception e) {
			
			throw e;
		}
		return JSONObject.toJSONString(listT);
	}
	//-------------------陈鹏飞
	/**
	 * 
	 * @param request
	 * @param prjId
	 * @return
	 */
	@RequestMapping(value = "/findSubNetCountByPrjId", method = RequestMethod.POST)
    @ResponseBody
    public String findSubNetCountByPrjId(HttpServletRequest request,@RequestBody String prjId){
    	int countSubNetWork=subNetService.findSubNetCountByPrjId(prjId);
    	return JSONObject.toJSONString(countSubNetWork);
    }
	/**
	 * 验证子网地址是否重复
	 * @param request
	 * @param cidr
	 * @return
	 */
	@RequestMapping(value = "/checkCidr", method = RequestMethod.POST)
	@ResponseBody
	public boolean checkCidr(HttpServletRequest request,@RequestBody Map<String,String> map){
		String netId = map.get("netId");
		String cidr = map.get("cidr");
		boolean bool = subNetService.checkCidr(netId, cidr);
		return bool;
	}
	
	/**
     * 检查子网网络是否重名
     * @param request
     * @param customer
     * @return
     * @throws AppException
     * @throws IOException 
     */
    @RequestMapping(value = "/checkSubNetName", method = RequestMethod.POST)
    @ResponseBody
    public String checkSubNetName(HttpServletRequest request, Page page, @RequestBody Map<String,String> map) {
        boolean bool = subNetService.checkSubNetName(map.get("subnetId"),map.get("subNetName"),map.get("prjId"));
        return JSONObject.toJSONString(bool);
    }
    /**
     * 新增子网
     * @param request
     * @param page
     * @param cloudSubNetWork
     * @return
     */
    @RequestMapping(value = "/addSubNetWork", method = RequestMethod.POST)
    @ResponseBody
    public String addSubNetWork(HttpServletRequest request, Page page, @RequestBody CloudSubNetWork cloudSubNetWork) {
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String userName = sessionUser.getUserName();
        cloudSubNetWork.setCreateName(userName);
        try {
        	cloudSubNetWork = subNetService.addSubNetWork(cloudSubNetWork);
        	if ("1".equals(cloudSubNetWork.getSubnetType())) {
        	    logService.addLog( "添加受管子网", 
        	            ConstantClazz.LOG_TYPE_SUBNET, 
        	            cloudSubNetWork.getSubnetName()+"("+cloudSubNetWork.getCidr()+")",
        	            cloudSubNetWork.getPrjId(),
        	            ConstantClazz.LOG_STATU_SUCCESS, null);
        	} else {
        	    logService.addLog( "添加自管子网", ConstantClazz.LOG_TYPE_SUBNET, cloudSubNetWork.getSubnetName()+"("+cloudSubNetWork.getCidr()+")", cloudSubNetWork.getPrjId(),
                        ConstantClazz.LOG_STATU_SUCCESS, null);
        	}
		}catch (Exception e) {
		    if ("1".equals(cloudSubNetWork.getSubnetType())) {
		        logService.addLog( "添加受管子网", 
		                ConstantClazz.LOG_TYPE_SUBNET, 
		                cloudSubNetWork.getSubnetName()+"("+cloudSubNetWork.getCidr()+")", 
		                cloudSubNetWork.getPrjId(),
		                ConstantClazz.LOG_STATU_ERROR, e);
		    } else {
		        logService.addLog( "添加自管子网", ConstantClazz.LOG_TYPE_SUBNET, cloudSubNetWork.getSubnetName()+"("+cloudSubNetWork.getCidr()+")", cloudSubNetWork.getPrjId(),
                        ConstantClazz.LOG_STATU_ERROR, e);
		    }
			throw e;
		}
        return JSONObject.toJSONString(cloudSubNetWork);
    }
    /**
     * 编辑子网
     * @param request
     * @param page
     * @param cloudSubNetWork
     * @return
     */
    @RequestMapping(value = "/updateSubNetWork", method = RequestMethod.POST)
    @ResponseBody
    public String updateSubNetWork(HttpServletRequest request, Page page, @RequestBody CloudSubNetWork cloudSubNetWork) {
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String userName = sessionUser.getUserName();
        cloudSubNetWork.setCreateName(userName);
        try {
        	cloudSubNetWork = subNetService.updateSubNetWork(cloudSubNetWork);
        	if ("1".equals(cloudSubNetWork.getSubnetType())) {
        	    logService.addLog( "修改受管子网", 
        	            ConstantClazz.LOG_TYPE_SUBNET, 
        	            cloudSubNetWork.getSubnetName()+"("+cloudSubNetWork.getCidr()+")", 
        	            cloudSubNetWork.getPrjId(),
        	            ConstantClazz.LOG_STATU_SUCCESS, null);
        	} else {
        	    logService.addLog( "修改自管子网", 
        	            ConstantClazz.LOG_TYPE_SUBNET, 
        	            cloudSubNetWork.getSubnetName()+"("+cloudSubNetWork.getCidr()+")", 
        	            cloudSubNetWork.getPrjId(),
                        ConstantClazz.LOG_STATU_SUCCESS, null);
        	}
		}catch (Exception e) {
		    if ("1".equals(cloudSubNetWork.getSubnetType())) {
		        logService.addLog( "修改受管子网", 
		                ConstantClazz.LOG_TYPE_SUBNET, 
		                cloudSubNetWork.getSubnetName()+"("+cloudSubNetWork.getCidr()+")", 
		                cloudSubNetWork.getPrjId(),
		                ConstantClazz.LOG_STATU_ERROR, e);
		    } else {
		        logService.addLog( "修改自管子网", 
		                ConstantClazz.LOG_TYPE_SUBNET, 
		                cloudSubNetWork.getSubnetName()+"("+cloudSubNetWork.getCidr()+")", 
		                cloudSubNetWork.getPrjId(),
                        ConstantClazz.LOG_STATU_ERROR, e);
		    }
			throw e;
		}
        
        return JSONObject.toJSONString(cloudSubNetWork);
    }
    /**
     * 删除子网
     * @param request
     * @param page
     * @param cloudSubNetWork
     * @return
     */
    @RequestMapping(value = "/deleteCloudSubNet", method = RequestMethod.POST)
    @ResponseBody
    public String deleteCloudSubNet(HttpServletRequest request, Page page, @RequestBody CloudSubNetWork cloudSubNetWork) {
        boolean bool = true;
        try {
        	bool = subNetService.daleteCloudSubNet(cloudSubNetWork);
        	if(bool){
        	    if ("1".equals(cloudSubNetWork.getSubnetType())) {
        	        logService.addLog( "删除受管子网", 
        	                ConstantClazz.LOG_TYPE_SUBNET, 
        	                cloudSubNetWork.getSubnetName()+"("+cloudSubNetWork.getCidr()+")", 
        	                cloudSubNetWork.getPrjId(),
        	                ConstantClazz.LOG_STATU_SUCCESS, null);
        	    } else {
        	        logService.addLog( "删除自管子网", 
        	                ConstantClazz.LOG_TYPE_SUBNET, 
        	                cloudSubNetWork.getSubnetName()+"("+cloudSubNetWork.getCidr()+")", 
        	                cloudSubNetWork.getPrjId(),
                            ConstantClazz.LOG_STATU_SUCCESS, null);
        	    }
        	}
		}catch (Exception e) {
		    if ("1".equals(cloudSubNetWork.getSubnetType())) {
		        logService.addLog( "删除受管子网", ConstantClazz.LOG_TYPE_SUBNET, cloudSubNetWork.getSubnetName(), cloudSubNetWork.getPrjId(),
		                ConstantClazz.LOG_STATU_ERROR, e);
		    } else {
		        logService.addLog( "删除自管子网", ConstantClazz.LOG_TYPE_SUBNET, cloudSubNetWork.getSubnetName(), cloudSubNetWork.getPrjId(),
                        ConstantClazz.LOG_STATU_SUCCESS, null);
		    }
			throw e;
		}
        return JSONObject.toJSONString(bool);
    }
    /**
     * 获取指定网络下的子网
     * @param request
     * @param netId
     * @return
     */
    @RequestMapping(value = "getSubNetListByNetId", method = RequestMethod.POST)
    @ResponseBody
    public String getSubNetListByNetId(HttpServletRequest request, @RequestBody Map<String,String> Map) {
        List<CloudSubNetWork> subList= subNetService.getSubNetListById(Map.get("netId"),null,null);
        return JSONObject.toJSONString(subList);
    }
    
    /**
     * 获取子网的DNS
     * @param request
     * @param netId
     * @return
     */
    @RequestMapping(value = "getSubnetDNS", method = RequestMethod.POST)
    @ResponseBody
    public String getSubnetDNS(HttpServletRequest request, @RequestBody Map<String,String> Map) {
    	String dns= subNetService.getSubnetDNS(Map.get("subnetId"));
    	return JSONObject.toJSONString(dns);
    }
    /**
     * 获取连接路由的受管子网或自管子网列表
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value = "/getsubnetlist", method = RequestMethod.POST)
    @ResponseBody
    public String getSubnetListByType(HttpServletRequest request, @RequestBody Map<String, String> map) {
    	String netId = map.get("netId").toString();
    	String subnetType = map.get("subnetType").toString();
    	List<BaseCloudSubNetWork> list = subNetService.getSubNetListByType(netId, subnetType);
    	JSONObject respJson = new JSONObject();
    	respJson.put("resultData", list);
    	return JSONObject.toJSONString(respJson);
    }
    
    @RequestMapping(value = "/getmanagedsubnetlist", method = RequestMethod.POST)
    @ResponseBody
    public String getManagedSubnetList(HttpServletRequest request, @RequestBody Map<String, String> map) {
        String netId = map.get("netId") == null?"":map.get("netId").toString();
        EayunResponseJson json = new EayunResponseJson();
        List<BaseCloudSubNetWork> list = subNetService.getManagedSubnetList(netId);
        json.setRespCode(ConstantClazz.SUCCESS_CODE);
        json.setData(list);
        return JSONObject.toJSONString(json);
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
     * 通过子网id获取相应的私有网络id
     * @author gaoxiang
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value = "/getnetidbysubnetid", method = RequestMethod.POST)
    @ResponseBody
    public String getNetIdBySubnetId(HttpServletRequest request, @RequestBody Map<String, String> map) {
        String subnetId = map.get("subnetId");
        EayunResponseJson json = new EayunResponseJson();
        BaseCloudSubNetWork subnet = subNetService.getNetIdBySubnetId(subnetId);
        json.setRespCode(ConstantClazz.SUCCESS_CODE);
        json.setData(subnet);
        return JSONObject.toJSONString(json);
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
    	EayunResponseJson json = subNetService.checkForDel(params);
        return JSONObject.toJSONString(json);
    }
}

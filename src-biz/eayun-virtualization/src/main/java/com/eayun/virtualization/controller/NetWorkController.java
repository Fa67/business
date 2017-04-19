package com.eayun.virtualization.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
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
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.controller.BaseController;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.util.StringUtil;
import com.eayun.log.service.LogService;
import com.eayun.virtualization.model.BaseCloudNetwork;
import com.eayun.virtualization.model.CloudNetWork;
import com.eayun.virtualization.model.CloudOrderNetWork;
import com.eayun.virtualization.service.CloudOrderNetWorkService;
import com.eayun.virtualization.service.NetWorkService;

/**
 * 
 *                       
 * @Filename: NetWorkController.java
 * @Description: 
 * @Version: 1.0
 * @Author: 陈鹏飞
 * @Email: pengfei.chen@eayun.com
 * @History:<br>
 *<li>Date: 2015年11月13日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Controller
@RequestMapping("/cloud/netWork")
@Scope("prototype")
public class NetWorkController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(NetWorkController.class);
    @Autowired
    private NetWorkService netWorkService;
    @Autowired
    private CloudOrderNetWorkService orderNetworkService;
    @Autowired
    private LogService logService;

    @RequestMapping(value = "/findNetWorkCountByPrjId", method = RequestMethod.POST)
    @ResponseBody
    public String findNetWorkCountByPrjId(HttpServletRequest request,@RequestBody String prjId){
    	int countNetWork=netWorkService.findNetWorkCountByPrjId(prjId);
    	return JSONObject.toJSONString(countNetWork);
    }
    /**
     * 查询项目网络
     * @param request
     * @param page
     * @param paramsMap
     * @return
     * @throws Exception 
     * @throws AppException
     * @throws IOException 
     */
    @RequestMapping(value = "/getNetWorkListByPrjId", method = RequestMethod.POST)
    @ResponseBody
    public String getNetWorkListByPrjId(HttpServletRequest request, Page page, @RequestBody ParamsMap paramsMap) throws Exception {
        page = netWorkService.getNetWorkListByPrjId(page, paramsMap);
        return JSONObject.toJSONString(page);
    }
    /**
     * 检查网络是否重名
     * @param request
     * @param page
     * @param map
     * @return
     * @throws AppException
     * @throws IOException 
     */
    @RequestMapping(value = "/checkNetWorkName", method = RequestMethod.POST)
    @ResponseBody
    public String checkNetWorkName(HttpServletRequest request, Page page, @RequestBody Map<String,String> map) {
        boolean bool = netWorkService.checkNetWorkName(map.get("netId"),map.get("netName"),map.get("prjId"));
        return JSONObject.toJSONString(bool);
    }
    /**
     * 新购网络
     * @author gaoxiang
     * @param request
     * @param cloudOrderNetWork
     * @return
     */
    @RequestMapping(value = "/buynetwork", method = RequestMethod.POST)
    @ResponseBody
    public String buyNetWork(HttpServletRequest request, @RequestBody CloudOrderNetWork cloudOrderNetWork) throws Exception {
    	SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    	JSONObject json = new JSONObject();
    	String errMsg = new String();
        try {
        	errMsg = netWorkService.buyNetWork(cloudOrderNetWork, sessionUser);
        	if (StringUtils.isEmpty(errMsg)) {
        	    json.put("respCode", ConstantClazz.SUCCESS_CODE);
        	    json.put("orderNo", cloudOrderNetWork.getOrderNo());
        	} else {
        	    json.put("respCode", ConstantClazz.WARNING_CODE);
        	    json.put("respMsg", errMsg);
        	}
        } catch (Exception e) {
            if ("余额不足".equals(e.getMessage())) {
                json.put("respCode", ConstantClazz.WARNING_CODE);
                json.put("respMsg", "CHANGE_OF_BALANCE");
            } else {
                json.put("respCode", ConstantClazz.ERROR_CODE);
                json.put("respMsg", e.getMessage());
            }
        	log.error(e.getMessage(), e);
        }
    	return json.toJSONString();
    }
    /**
     * 新增网络
     * @param request
     * @param customer
     * @return
     * @throws AppException
     * @throws IOException 
     */
    /*@RequestMapping(value = "/addNetWork", method = RequestMethod.POST)
    @ResponseBody
    public String addNetWork(HttpServletRequest request, @RequestBody CloudNetWork cloudNetWork) {
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String userName = sessionUser.getUserName(); 
        cloudNetWork.setCreateName(userName);
        try {
        	cloudNetWork = netWorkService.addNetWork(cloudNetWork);
			logService.addLog( "创建私有网络", ConstantClazz.LOG_TYPE_NET, cloudNetWork.getNetName(), cloudNetWork.getPrjId(),
					ConstantClazz.LOG_STATU_SUCCESS, null);
		}catch (Exception e) {
			logService.addLog( "创建私有网络", ConstantClazz.LOG_TYPE_NET, cloudNetWork.getNetName(), cloudNetWork.getPrjId(),
					ConstantClazz.LOG_STATU_ERROR, e);
			throw e;
		}
        
        return JSONObject.toJSONString(cloudNetWork);
    }*/
    /**
     * 变更私有网络配置接口
     * @param request
     * @param cloudOrderNetWork
     * @return
     */
    @RequestMapping(value = "/changenetwork", method = RequestMethod.POST)
    @ResponseBody
    public String changeNetWork(HttpServletRequest request, @RequestBody CloudOrderNetWork cloudOrderNetWork) throws Exception {
    	SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    	JSONObject json = new JSONObject();
    	String errMsg = new String();
    	try {
    		errMsg = netWorkService.changeNetWork(cloudOrderNetWork, sessionUser);
    		if (StringUtil.isEmpty(errMsg)) {
    		    json.put("respCode", ConstantClazz.SUCCESS_CODE);
    		    json.put("orderNo", cloudOrderNetWork.getOrderNo());
    		} else {
    		    json.put("respCode", ConstantClazz.WARNING_CODE);
    		    json.put("respMsg", errMsg);
    		}
    	} catch (Exception e) {
    	    if ("余额不足".equals(e.getMessage())) {
    	        json.put("respCode", ConstantClazz.WARNING_CODE);
                json.put("respMsg", "CHANGE_OF_BALANCE");
            } else {
                json.put("respCode", ConstantClazz.ERROR_CODE);
                json.put("respMsg", e.getMessage());
            }
    		log.error(e.getMessage(), e);
    	}
    	return json.toJSONString();
    }
    
    @RequestMapping(value = "/updatenetworkname", method = RequestMethod.POST)
    @ResponseBody
    public String updateNetWorkName(HttpServletRequest request, @RequestBody Map<String, String> map) {
    	CloudNetWork cloudNetWork = new CloudNetWork();
    	cloudNetWork.setDcId(map.get("dcId").toString());
    	cloudNetWork.setPrjId(map.get("prjId").toString());
    	cloudNetWork.setNetId(map.get("netId").toString());
    	cloudNetWork.setNetName(map.get("netName").toString());
    	try {
    		cloudNetWork = netWorkService.updateNetWorkName(cloudNetWork);
    		logService.addLog( "编辑私有网络", ConstantClazz.LOG_TYPE_NET, cloudNetWork.getNetName(), cloudNetWork.getPrjId(),
                    ConstantClazz.LOG_STATU_SUCCESS, null);
    	} catch (Exception e) {
    	    logService.addLog( "编辑私有网络", ConstantClazz.LOG_TYPE_NET, cloudNetWork.getNetName(), cloudNetWork.getPrjId(),
                    ConstantClazz.LOG_STATU_ERROR, e);
    		throw e;
    	}
    	return JSONObject.toJSONString(cloudNetWork);
    }
    /**
     * 修改网络
     * @param request
     * @param customer
     * @return
     * @throws AppException
     * @throws IOException 
     */
    /*@RequestMapping(value = "/updateNetWork", method = RequestMethod.POST)
    @ResponseBody
    public String updateNetWork(HttpServletRequest request, @RequestBody CloudNetWork cloudNetWork) {
    	try {
    		cloudNetWork = netWorkService.updateNetWork(cloudNetWork);
			logService.addLog( "编辑私有网络", ConstantClazz.LOG_TYPE_NET, cloudNetWork.getNetName(), cloudNetWork.getPrjId(),
					ConstantClazz.LOG_STATU_SUCCESS, null);
		}catch (Exception e) {
			logService.addLog( "编辑私有网络", ConstantClazz.LOG_TYPE_NET, cloudNetWork.getNetName(), cloudNetWork.getPrjId(),
					ConstantClazz.LOG_STATU_ERROR, e);
			throw e;
		}
        return JSONObject.toJSONString(cloudNetWork);
    }*/
    /**
     * 删除指定网络
     * @param request
     * @param page
     * @param cloudNetWork
     * @return
     * @throws AppException
     * @throws IOException 
     */
    @RequestMapping(value = "/delNetWorkByNetId", method = RequestMethod.POST)
    @ResponseBody
    public String delNetWorkByNetId(HttpServletRequest request, Page page, @RequestBody CloudNetWork cloudNetWork) {
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        cloudNetWork.setCusId(sessionUser.getCusId());
        boolean bool=true;
    	try {
    		bool = netWorkService.delNetWorkByNetId(cloudNetWork);
    		if(bool){
    			logService.addLog( "删除私有网络", ConstantClazz.LOG_TYPE_NET, cloudNetWork.getNetName(), cloudNetWork.getPrjId(),
    					ConstantClazz.LOG_STATU_SUCCESS, null);
    		}
		}catch (Exception e) {
			logService.addLog( "删除私有网络", ConstantClazz.LOG_TYPE_NET, cloudNetWork.getNetName(), cloudNetWork.getPrjId(),
					ConstantClazz.LOG_STATU_ERROR, e);
			throw e;
		}
        
        return JSONObject.toJSONString(bool);
    }

    /**
     * 私有网络续费
     * @author zhangfan
     * @param request
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/renewnetwork", method = RequestMethod.POST)
    @ResponseBody
    public String renewNetWork(HttpServletRequest request, @RequestBody Map map) throws Exception {
        log.info("续费私有网络开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        JSONObject json = new JSONObject();
        Map<String, String> respMap;
        try {
            respMap = netWorkService.renewNetwork(sessionUser, map);
            if(respMap.containsKey(ConstantClazz.WARNING_CODE)){
                json.put("respCode", ConstantClazz.WARNING_CODE);
                json.put("message", respMap.get(ConstantClazz.WARNING_CODE));
            }else if(respMap.containsKey(ConstantClazz.SUCCESS_CODE)){
                json.put("respCode", ConstantClazz.SUCCESS_CODE);
                json.put("message", respMap.get(ConstantClazz.SUCCESS_CODE));
                json.put("orderNo", respMap.get("orderNo"));
            }
            json.put("bandwidth",respMap.get("bandwidth"));
        } catch(Exception e) {
            log.error("续费私有网络异常", e);
        }
        return json.toJSONString();
    }

    @RequestMapping(value = "/checkNetworkOrderExist" , method = RequestMethod.POST)
    @ResponseBody
    public String checkNetworkOrderExist(HttpServletRequest request, @RequestBody String netId) throws Exception{
        log.info("检查当前是否已存在私有网络续费或变配的未完成订单");
        JSONObject json = new JSONObject();
        try{
            boolean flag = netWorkService.checkNetworkOrderExist(netId);
            json.put("flag", flag);
        }catch(Exception e){
            throw e;
        }
        return json.toJSONString();
    }
    /**
     * 获取项目下可以购买的网络数量
     * @author gaoxiang
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value = "/getnetworkquotasbyprjid", method = RequestMethod.POST)
    @ResponseBody
    public String getNetworkQuotasByPrjId(HttpServletRequest request, @RequestBody Map<String, String> map) {
        JSONObject json = new JSONObject();
        String prjId = map.get("prjId");
        int count = netWorkService.getNetworkQuotasByPrjId(prjId);
        json.put("respCode", ConstantClazz.SUCCESS_CODE);
        json.put("netQuotas", count);
        return json.toJSONString();
    }
    /**
     * 获取项目下可以购买的带宽数量
     * @author gaoxiang
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value = "/getbandquotasbyprjid", method = RequestMethod.POST)
    @ResponseBody
    public String getBandQuotasByPrjId(HttpServletRequest request, @RequestBody Map<String, String> map) {
        JSONObject json = new JSONObject();
        String prjId = map.get("prjId");
        int bandCount = netWorkService.getBandQuotasByPrjId(prjId);
        json.put("respCode", ConstantClazz.SUCCESS_CODE);
        json.put("bandQuotas", bandCount);
        return json.toJSONString();
    }
    /**
     * 获取价格接口
     * @author gaoxiang
     * @param request
     * @param cloudOrderNetWork
     * @return
     */
    @RequestMapping(value = "/getprice", method = RequestMethod.POST)
    @ResponseBody
    public String getPrice(HttpServletRequest request, @RequestBody CloudOrderNetWork cloudOrderNetWork) {
        EayunResponseJson json = new EayunResponseJson();
        try {
            BigDecimal price = netWorkService.getPrice(cloudOrderNetWork);
            json.setRespCode(ConstantClazz.SUCCESS_CODE);
            json.setData(price);
        } catch (Exception e) {
            log.error(e.toString(),e);
            json.setRespCode(ConstantClazz.ERROR_CODE);
            json.setMessage(e.getMessage());
        }
        return JSON.toJSONString(json);
    }
    /**
     * 查询指定网络
     * @param request
     * @param page
     * @param netId
     * @return
     */
    @RequestMapping(value = "/findNetWorkByNetId", method = RequestMethod.POST)
    @ResponseBody
    public String findNetWorkByNetId(HttpServletRequest request, Page page, @RequestBody String netId) {
        CloudNetWork cloudNetWork = netWorkService.findNetWorkByNetId(netId);
        return JSONObject.toJSONString(cloudNetWork);
    }
    /**
     * 根据routeId查询指定网络
     * @param request
     * @param page
     * @param netId
     * @return
     */
    @RequestMapping(value = "/findNetWorkByRouteId", method = RequestMethod.POST)
    @ResponseBody
    public String findNetWorkByRouteId(HttpServletRequest request, Page page, @RequestBody String routeId) {
        return JSONObject.toJSONString(netWorkService.findNetWorkByRouteId(routeId));
    }
    /**
     * 根据orderNo查询network类
     * @author gaoxiang
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value = "/getordernetworkbyorderno", method = RequestMethod.POST)
    @ResponseBody
    public String getOrderNetWorkByOrderNo(HttpServletRequest request, @RequestBody Map<String, String> map) {
        String orderNo = map.get("orderNo");
        EayunResponseJson json = new EayunResponseJson();
        CloudOrderNetWork network = orderNetworkService.getOrderNetWorkByOrderNo(orderNo);
        json.setRespCode(ConstantClazz.SUCCESS_CODE);
        json.setData(network);
        return JSONObject.toJSONString(json);
    }
    /**
     * 根据项目id获取设置了网关的私有网络
     * @author gaoxiang
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value = "/getnetworklisthavegateway", method = RequestMethod.POST)
    @ResponseBody
    public String getNetworkListByPrjIdAndRouteIdNotNull (HttpServletRequest request, @RequestBody Map<String, String> map) {
        EayunResponseJson json = new EayunResponseJson();
        String prjId = map.get("prjId");
        List<CloudNetWork> list = netWorkService.getNetworkListByPrjIdAndRouteIdNotNull(prjId);
        json.setRespCode(ConstantClazz.SUCCESS_CODE);
        json.setData(list);
        return JSONObject.toJSONString(json);
    }
}

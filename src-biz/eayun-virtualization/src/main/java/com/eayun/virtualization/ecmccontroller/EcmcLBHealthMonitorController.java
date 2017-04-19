/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.virtualization.ecmccontroller;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.ecmcuser.util.EcmcSessionUtil;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.virtualization.ecmcservice.EcmcLBHealthMonitorService;
import com.eayun.virtualization.model.BaseCloudLdMonitor;
import com.eayun.virtualization.model.CloudLdMonitor;
import com.eayun.virtualization.model.CloudLdPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *                       
 * @Filename: EcmcLBHealthMonitorController.java
 * @Description: 
 * @Version: 1.0
 * @Author: fangjun.yang
 * @Email: fangjun.yang@eayun.com
 * @History:<br>
 *<li>Date: 2016年4月8日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Controller
@RequestMapping("/ecmc/virtual/loadbalance/healthmonitor")
@Scope("prototype")
public class EcmcLBHealthMonitorController {

    private final static Logger        log = LoggerFactory.getLogger(EcmcLBHealthMonitorController.class);

    @Autowired
    private EcmcLBHealthMonitorService ecmcLBHealthMonitorService;
    @Autowired
    private EcmcLogService ecmcLogService;
    
    @RequestMapping("/listmonitor")
    @ResponseBody
    public Object listMonitor(@RequestBody ParamsMap paramsMap) throws AppException {
        log.info("查询负载均衡监听开始");
        return ecmcLBHealthMonitorService.listMonitor(paramsMap);
    }

    /**
     * 查询本地所有监控绑定所有资源池的名称
     * @param request
     * @return
     * @throws AppException
     */
    @RequestMapping("/monitorbindpoolnames")
    @ResponseBody
    public Object monitorBindPoolNames(@RequestBody Map<String, String> params) throws AppException {
        log.info("查询本地所有监控绑定所有资源池的名称");
        EayunResponseJson resultJson = new EayunResponseJson();
        resultJson.setData(ecmcLBHealthMonitorService.getMonitorBindPoolNames(params.get("dcId"), params.get("prjId"), params.get("ldmId")));
        resultJson.setRespCode(ConstantClazz.SUCCESS_CODE);
        return resultJson;
    }

    /**
     * 查询本地当前资源池所有未绑定监控的集合
     * @param request
     * @return
     * @throws AppException
     */
    @RequestMapping("/bindmonitorlist")
    @ResponseBody
    public Object bindMonitorList(HttpServletRequest request, @RequestBody CloudLdPool pool) throws AppException {
        log.info("查询本地当前资源池所有未绑定监控的集合开始");
        try {
            return ecmcLBHealthMonitorService.getNotBindMonitorListByPool(pool);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException("error.globe.system", e);
        }
    }

    /**
     * 根据资源池Id查询本地资源池与监控关联关系表，得到该资源池已绑定的监控
     * @param paramsMap
     * @return
     * @throws AppException
     */
    @RequestMapping("/poolmonitorlist")
    @ResponseBody
    public Object poolMonitorList(@RequestBody Map<String, String> params) throws AppException {
        log.info("查询资源池绑定的监控开始");
        try {
            return ecmcLBHealthMonitorService.poolMonitorList(params);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException("error.globe.system", e);
        }
    }

    /**
     * 创建监控的信息
     * @param ldMonitor 
     * @param request
     * @return
     * @throws AppException
     */
    @RequestMapping("/createMonitor")
    @ResponseBody
    public Object createMonitor(@RequestBody CloudLdMonitor ldMonitor) throws AppException {
        log.info("创建监控的信息开始");
        EayunResponseJson reJson = new EayunResponseJson();
        BaseCloudLdMonitor result = null;
        try {
        	ldMonitor.setCreateName(EcmcSessionUtil.getUser().getAccount());
            result = ecmcLBHealthMonitorService.createMonitor(ldMonitor);
            if (result != null) {
                reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
                reJson.setData(result);
                String ldmId = result.getLdmId()==null?"----":result.getLdmId();
                ecmcLogService.addLog("创建健康检查", ConstantClazz.LOG_TYPE_HEALTHMIR, ldMonitor.getLdmName(), ldMonitor.getPrjId(), 1, ldmId, null);
            } else {
                reJson.setRespCode(ConstantClazz.ERROR_CODE);
            }
        } catch (AppException e) {
            ecmcLogService.addLog("创建健康检查", ConstantClazz.LOG_TYPE_HEALTHMIR, ldMonitor.getLdmName(), ldMonitor.getPrjId(), 0, "----", e);
            log.error(e.getMessage(), e);
            throw e;
        }
        return reJson;
    }

    /**
     * 删除监控的信息
     * @param request
     * @return
     * @throws AppException
     */
    @RequestMapping("/delete")
    @ResponseBody
    public Object delete(@RequestBody Map<String, String> params) throws AppException {
        log.info("删除监控开始");
        EayunResponseJson delJson = new EayunResponseJson();
        try {
            delJson.setData(ecmcLBHealthMonitorService.deleteMonitor(params.get("datacenterId"), params.get("projectId"), params.get("id")));
            delJson.setRespCode(ConstantClazz.SUCCESS_CODE);
            ecmcLogService.addLog("删除健康检查", ConstantClazz.LOG_TYPE_HEALTHMIR, params.get("ldmName"), params.get("projectId"), 1, params.get("id"), null);
        } catch (AppException e) {
            delJson.setRespCode(ConstantClazz.ERROR_CODE);
            ecmcLogService.addLog("删除健康检查", ConstantClazz.LOG_TYPE_HEALTHMIR, params.get("ldmName"), params.get("projectId"), 0, params.get("id"), e);
            throw e;
        }
        return delJson;
    }

    @RequestMapping(value = "/deleteHealthMonitor", method = RequestMethod.POST)
    @ResponseBody
    public String deleteHealthMonitor(HttpServletRequest request,@RequestBody CloudLdMonitor cloudLdMonitor) throws Exception{
        JSONObject json = new JSONObject ();
        String datacenterId = cloudLdMonitor.getDcId();
        String projectId = cloudLdMonitor.getPrjId();
        String id = cloudLdMonitor.getLdmId();
        try {
            ecmcLBHealthMonitorService.deleteMonitor(datacenterId, projectId, id);
            ecmcLogService.addLog("删除健康检查", ConstantClazz.LOG_TYPE_HEALTHMIR, cloudLdMonitor.getLdmName(), projectId, 1, id, null);
            json.put("respCode",ConstantClazz.SUCCESS_CODE);
        }catch (Exception e) {
            ecmcLogService.addLog("删除健康检查", ConstantClazz.LOG_TYPE_HEALTHMIR, cloudLdMonitor.getLdmName(), projectId, 0,id, e);
            json.put("respCode",ConstantClazz.ERROR_CODE);
            throw e;
        }
        return json.toJSONString();
    }

    /**
     * 资源池解除监控的方法
     * @param request
     * @return
     * @throws AppException
     */
    @RequestMapping("/detachHealthMonitor")
    @ResponseBody
    public Object detachHealthMonitor(@RequestBody Map<String, String> params) throws AppException {
        log.info("资源池解除监控开始");
        EayunResponseJson resultJson = new EayunResponseJson();
        try {
        	boolean result = ecmcLBHealthMonitorService.detachHealthMonitor(params.get("datacenterId"), params.get("projectId"), params.get("poolId"), params.get("monitorId"));
            resultJson.setData(result);
            resultJson.setRespCode(ConstantClazz.SUCCESS_CODE);
            ecmcLogService.addLog("解除健康检查", ConstantClazz.LOG_TYPE_HEALTHMIR, params.get("poolName"), params.get("projectId"), 1, params.get("monitorId"), null);
        } catch (AppException e) {
            resultJson.setRespCode(ConstantClazz.ERROR_CODE);
            ecmcLogService.addLog("解除健康检查", ConstantClazz.LOG_TYPE_HEALTHMIR, params.get("poolName"), params.get("projectId"), 0, params.get("monitorId"), e);
            throw e;
        }
        return resultJson;
    }

    /**
     * 修改监控
     * @param params
     * @param monitor 
     * @return
     * @throws AppException
     */
    @RequestMapping("/updatemonitor")
    @ResponseBody
    public Object updateMonitor(@RequestBody CloudLdMonitor monitor) throws AppException {
        log.info("修改监控开始");
        BaseCloudLdMonitor result = null;
        EayunResponseJson reJson = new EayunResponseJson();
        try {
            result = ecmcLBHealthMonitorService.updateMonitor(monitor);
            if (result != null) {
                reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
                reJson.setData(result);
            } else {
                reJson.setRespCode(ConstantClazz.ERROR_CODE);
            }
            ecmcLogService.addLog("编辑健康检查", ConstantClazz.LOG_TYPE_HEALTHMIR, monitor.getLdmName(), monitor.getPrjId(), 1, monitor.getLdmId(), null);
        } catch (AppException e) {
            ecmcLogService.addLog("编辑健康检查", ConstantClazz.LOG_TYPE_HEALTHMIR, monitor.getLdmName(), monitor.getPrjId(), 0,  monitor.getLdmId(), e);
            throw e;
        }

        return reJson;
    }
    
    /**
     * 验证健康检查是否重名
     * @param params
     * @return
     * @throws AppException
     */
    @RequestMapping("/checkhealthmonitorname")
    @ResponseBody
    public Object checkHealthMonitorName(@RequestBody Map<String, String> params) throws AppException{
    	EayunResponseJson resultJson = new EayunResponseJson();
    	boolean exists = ecmcLBHealthMonitorService.checkHealthMonitorName(params.get("prjId"), params.get("ldmName"), params.get("ldmId"));
    	resultJson.setData(exists);
    	resultJson.setRespCode(ConstantClazz.SUCCESS_CODE);
    	return resultJson;
    }

    /**
     * 查询项目的健康检查
     *
     * @author zhouhaitao
     * @param request
     * @param cloudLdMonitor
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getMonitorListByPool" , method = RequestMethod.POST)
    @ResponseBody
    public String getMonitorListByPool(HttpServletRequest request, @RequestBody CloudLdPool pool) throws Exception{
        log.info("根据负载均衡器查询健康检查列表");
        JSONObject json = new JSONObject();
        List<CloudLdMonitor> monitorList = new ArrayList<CloudLdMonitor>();
        try{
            monitorList = ecmcLBHealthMonitorService.getMonitorListByPool(pool);
            json.put("data",monitorList);
        }catch(Exception e){
            log.error(e.toString(),e);
            throw e;
        }
        return json.toJSONString();
    }

    /**
     * 负载均衡器-健康检查
     * -------------
     * @author zhouhaitao
     * @param request
     * @param cloudLdPool
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/bindHealthMonitor", method = RequestMethod.POST)
    @ResponseBody
    public String bindHealthMonitor(HttpServletRequest request,@RequestBody CloudLdPool cloudLdPool) throws Exception{
        JSONObject json = new JSONObject ();
        List<CloudLdMonitor> monList = new ArrayList<CloudLdMonitor>();
        try {
            monList = ecmcLBHealthMonitorService.bindHealthMonitor(cloudLdPool);
            json.put("data",monList);
            json.put("respCode",ConstantClazz.SUCCESS_CODE);
            ecmcLogService.addLog("绑定健康检查", ConstantClazz.LOG_TYPE_HEALTHMIR, cloudLdPool.getPoolName(), cloudLdPool.getPrjId(), 1, cloudLdPool.getPoolId(), null);
        }catch (Exception e) {
            json.put("respCode",ConstantClazz.ERROR_CODE);
            ecmcLogService.addLog("绑定健康检查", ConstantClazz.LOG_TYPE_HEALTHMIR, cloudLdPool.getPoolName(), cloudLdPool.getPrjId(), 0, cloudLdPool.getPoolId(), e);
            throw e;
        }
        return json.toJSONString();
    }
    /**
	 * 负载均衡器->健康检查(解除健康检查)
	 * -------------
	 * @param request
	 * @param cloudLdPool
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/unBindHealthMonitor", method = RequestMethod.POST)
	@ResponseBody                      
	public String unBindHealthMonitor(HttpServletRequest request,@RequestBody CloudLdPool cloudLdPool) throws Exception{
		JSONObject json = new JSONObject ();
		List<CloudLdMonitor> cloudLdMonitor = new ArrayList<CloudLdMonitor>();
		try {
			cloudLdMonitor = ecmcLBHealthMonitorService.unBindHealthMonitorForPool(cloudLdPool);
			json.put("data",cloudLdMonitor);
			json.put("respCode",ConstantClazz.SUCCESS_CODE_OP);
            ecmcLogService.addLog("解除健康检查关联", ConstantClazz.LOG_TYPE_HEALTHMIR, cloudLdPool.getPoolName(), cloudLdPool.getPrjId(), 1, cloudLdPool.getPoolId(), null);
		}catch (Exception e) {
            ecmcLogService.addLog("解除健康检查关联", ConstantClazz.LOG_TYPE_HEALTHMIR, cloudLdPool.getPoolName(), cloudLdPool.getPrjId(), 0, cloudLdPool.getPoolId(), e);
			json.put("respCode",ConstantClazz.ERROR_CODE);
			throw e;
		}
		return json.toJSONString();
	}
}

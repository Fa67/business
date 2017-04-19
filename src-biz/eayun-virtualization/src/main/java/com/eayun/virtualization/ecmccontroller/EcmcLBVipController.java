/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.virtualization.ecmccontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.virtualization.ecmcservice.EcmcLBVipService;
import com.eayun.virtualization.model.BaseCloudLdVip;

/**
 *                       
 * @Filename: EcmcLBVipController.java
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
@RequestMapping("/ecmc/virtual/loadbalance/vip")
@Scope("prototype")
public class EcmcLBVipController {

    private final static Logger log = LoggerFactory.getLogger(EcmcLBVipController.class);

    @Autowired
    private EcmcLBVipService    ecmcLBVipService;

    /**
     * 用于校验Vip名称是否已经存在
     * @param params 参数map
     * @return
     * @throws AppException
     */
    @RequestMapping("/checkvipname")
    @ResponseBody
    public Object checkVipName(@RequestBody Map<String, String> params) throws AppException {
    	EayunResponseJson resultJson = new EayunResponseJson();
        try {
            boolean exists = ecmcLBVipService.checkVipName(params.get("datacenterId"), params.get("projectId"), params.get("uname"), params.get("id"));
            resultJson.setRespCode(ConstantClazz.SUCCESS_CODE);
            resultJson.setData(exists);
            return resultJson;
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException("error.openstack.system", e);
        }
    }

    /**
     * 根据datacenterId，projectId，查找所有内网的子网
     * @param request
     * @param page
     * @return 子网名称，子网id，网络id
     * @throws AppException
     */
    @RequestMapping("/querysubnetlist")
    @ResponseBody
    public Object querySubnetList(@RequestBody ParamsMap paramsMap) throws AppException {
        try {
            return ecmcLBVipService.querySubnetList((String) paramsMap.getParams().get("datacenterId"), (String) paramsMap.getParams().get("projectId"), paramsMap.getPageNumber(), paramsMap.getPageSize());
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException("error.globe.system", e);
        }
    }

    /**
     * 查询所有vip的信息
     * @param paramsMap
     * @return
     * @throws AppException
     */
    @RequestMapping("/listvip")
    @ResponseBody
    public Object listVip(@RequestBody ParamsMap paramsMap) throws AppException {
        return ecmcLBVipService.findVipList(paramsMap);
    }

    /**
     * 创建Vip
     * @param params
     * @return
     * @throws AppException
     */
    @RequestMapping("/createvip")
    @ResponseBody
    public Object createVip(@RequestBody Map<String, String> params) throws AppException {
        EayunResponseJson reJson = new EayunResponseJson();
        try {
            BaseCloudLdVip result = ecmcLBVipService.createVip(params);
            if (result != null) {
                reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
                reJson.setData(result);
            } else {
                reJson.setRespCode(ConstantClazz.ERROR_CODE);
                reJson.setMessage("该vip在当前数据中心已存在");
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException("error.globe.system", e);
        }
        return reJson;
    }

    /**
     * 删除Vip
     * @param params
     * @return
     * @throws AppException
     */
    @RequestMapping("/deletevip")
    @ResponseBody
    public Object deleteVip(@RequestBody Map<String, String> params) throws AppException {
        EayunResponseJson delJson = new EayunResponseJson();
        try {
            if (ecmcLBVipService.deleteVip(params.get("datacenterId"), params.get("projectId"), params.get("id"))) {
                delJson.setRespCode(ConstantClazz.SUCCESS_CODE);
            }else{
            	delJson.setRespCode(ConstantClazz.ERROR_CODE);
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.toString(),e);
            throw new AppException("error.globe.system", e);
        }
        return delJson;
    }

    /**
     * 修改vip的信息
     * @param params
     * @return
     * @throws AppException
     */
    @RequestMapping("/updatevip")
    @ResponseBody
    public Object updateVip(@RequestBody Map<String, String> params) throws AppException {
        EayunResponseJson reJson = new EayunResponseJson();
        try {
            BaseCloudLdVip result = ecmcLBVipService.updateVip(params);
            if (result != null) {
                reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
                reJson.setData(result);
            } else {
                reJson.setRespCode(ConstantClazz.ERROR_CODE);
                reJson.setMessage("该vip在当前数据中心已存在");
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.toString(),e);
            throw new AppException("error.globe.system", e);
        }
        return reJson;
    }
    
    /**
     * 检查VIP是否可删除
     * @param params
     * @return
     * @throws AppException
     */
    @RequestMapping("/checkfordel")
    @ResponseBody
    public Object checkForDel(@RequestBody Map<String, String> params) throws AppException{
    	EayunResponseJson reJson = new EayunResponseJson();
    	if(ecmcLBVipService.existMember(params.get("poolId"))){
    		reJson.setData(false);
    		reJson.setMessage("VIP的资源池下存在成员，不能删除VIP");
    	}else{
    		reJson.setData(true);
    	}
    	reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
    	return reJson;
    }
}

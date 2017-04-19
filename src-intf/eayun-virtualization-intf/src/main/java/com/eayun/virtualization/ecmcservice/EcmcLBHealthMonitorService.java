/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.virtualization.ecmcservice;

import java.util.List;
import java.util.Map;

import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.virtualization.model.BaseCloudLdMonitor;
import com.eayun.virtualization.model.CloudLdMonitor;
import com.eayun.virtualization.model.CloudLdPool;

/**
 *                       
 * @Filename: EcmcLBHealthMonitorService.java
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
public interface EcmcLBHealthMonitorService {
    
    public Page listMonitor(ParamsMap paramsMap) throws AppException;
    
    public String getMonitorBindPoolNames(String dcId,String prjId,String ldmId) throws AppException;
    
    public Page getBindMonitorList(ParamsMap paramsMap) throws AppException;
    
    public List<CloudLdMonitor> getNotBindMonitorListByPool(CloudLdPool pool) throws AppException;
    
    public List<CloudLdMonitor> poolMonitorList(Map<String, String> params) throws AppException;
    
    public BaseCloudLdMonitor createMonitor(CloudLdMonitor ldMonitor) throws AppException;
    
    public boolean deleteMonitor(String datacenterId, String projectId, String id) throws AppException;
    
    public boolean detachHealthMonitor(String datacenterId, String projectId, String poolId, String monitorId) throws AppException;
    
    public BaseCloudLdMonitor updateMonitor(CloudLdMonitor monitor) throws AppException;

    public boolean checkHealthMonitorName(String prjId, String ldmName, String ldmId) throws AppException;

    public  List<CloudLdMonitor> getMonitorListByPool(CloudLdPool pool) throws AppException;

    public List<CloudLdMonitor> bindHealthMonitor(CloudLdPool cloudLdPool) throws AppException;
    
    public int getCountByPrjId(String prjId) throws AppException;
    
    public void deleteHealthMonitor(CloudLdMonitor cloudLdMonitor) throws Exception;

	/**
	 * 为负载均衡解除健康检查
	 * ------------------
	 * @author caoxiangyu
	 * @param pool
	 * @return
	 * @throws AppException
	 */
	public List<CloudLdMonitor> unBindHealthMonitorForPool(CloudLdPool pool) throws Exception;
}

/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.virtualization.ecmcservice;

import java.util.List;
import java.util.Map;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.virtualization.model.BaseCloudSubNetWork;
import com.eayun.virtualization.model.CloudRoute;

/**
 *                       
 * @Filename: EcmcRouteService.java
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
public interface EcmcRouteService {
    /**
     * 检查路由名是否存在
     * @param datacenterId
     * @param projectId
     * @param routeName
     * @param routeId
     * @return
     * @throws AppException
     */
    public boolean checkRouteName(String datacenterId, String projectId, String routeName, String routeId) throws AppException;
    /**
     * 查询路由剩余带宽
     * @param prjId
     * @return
     */
    public Map<String, Object> getRouteRateInfo (String prjId);
    /**
     * 查询路由
     * @param datacenterId
     * @param prjName
     * @param cusOrg
     * @param name
     * @param queryMap
     * @return
     * @throws AppException
     */
    public Page queryRoute(String datacenterId, String prjName, String cusOrg, String name, QueryMap  queryMap) throws AppException;
    
    /**
     * 添加路由
     * @param cloudRoute
     * @throws AppException
     */
    public void addRoute(CloudRoute cloudRoute) throws AppException;
    
    /**
     * 修改路由
     * @param cloudRoute
     * @throws AppException
     */
    public void updateRoute(CloudRoute cloudRoute) throws AppException;
    
    /**
     * 设置网关
     * @param cloudRoute
     * @throws AppException
     */
    public void setGateWay(String routeId, String netId, String dcId) throws AppException;
    
    /**
     * 连接子网
     * @param routeId
     * @param datacenterId
     * @param subNetworkId
     * @return
     * @throws AppException
     */
    public BaseCloudSubNetWork attachSubnet(String routeId, String datacenterId, String subNetworkId) throws AppException;
    
    /**
     * 移除网关
     * @param routeId
     * @param datacenterId
     * @throws AppException
     */
    public void removeGateway(String routeId, String datacenterId) throws AppException;
    
    /**
     * 解绑子网
     * @param routeId
     * @param subNetworkId
     * @param datacenterId
     * @throws AppException
     */
    public void detachSubnet(String routeId, String subNetworkId, String datacenterId) throws AppException;
    
    /**
     * 删除路由
     * @param datacenterId
     * @param id
     * @return
     * @throws AppException
     */
    public boolean deleteRoute(String datacenterId, String id) throws AppException;
    
    /**
     * 查询路由详情
     * @param routeId
     * @return
     * @throws AppException
     */
    public CloudRoute findRouteDetailById(String routeId) throws AppException;
   /**
    * 分页查询路由子网列
    * @param datacenterId
    * @param routeId
    * @return
    * @throws AppException
    */
    public List<Map<String, Object>> getSubnetList(String datacenterId, String routeId) throws AppException;
    
    /**
     * 根据项目ID查询带宽使用量
     * @author zengbo
     * @param prjId
     * @return
     */
    public int getQosNumByPrjId(String prjId);
    
    /**
     * 根据项目ID查询路由使用量
     * @param prjId
     * @return
     */
    public int getCountByPrjId(String prjId);
    /**
     * 分页查询私有网络下的受管子网 
     * @param datacenterId
     * @param routeId
     * @return
     * @throws AppException
     */
	public Page getSubnetList(Page page, String dcId, String routeId, QueryMap queryMap);
	/**
	 * 根据路由ID查看私有网络是否创建端口映射或者网管
	 * @param routeId
	 * @return
	 */
	public EayunResponseJson checkForCle(String routeId);
	/**
	 * 受管子网解绑路由的判断
	 * @param subnetId
	 * @return
	 */
	public EayunResponseJson checkDetachSubnet(String subnetId);

}

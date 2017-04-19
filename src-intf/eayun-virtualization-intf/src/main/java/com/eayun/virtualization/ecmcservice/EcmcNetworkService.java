/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.virtualization.ecmcservice;

import java.util.List;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.virtualization.model.CloudNetWork;

/**
 *                       
 * @Filename: EcmcNetworkService.java
 * @Description: 私有网络服务类
 * @Version: 1.0
 * @Author: fangjun.yang
 * @Email: fangjun.yang@eayun.com
 * @History:<br>
 *<li>Date: 2016年3月31日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public interface EcmcNetworkService {
    
    /**
     * 分页查询私有网络
     * @param paramsMap
     * @param queryMap
     * @return
     * @throws Exception
     */
	public Page getNetworkList(String netName, String dcId, String prjName, String cusName, QueryMap queryMap) throws Exception;
    
    /**
     * 添加网络
     * @param cloudNetWork
     * @return
     */
    public CloudNetWork addNetWork(CloudNetWork cloudNetWork);
    
    /**
     * 验证网络名称是否存在
     * @param datacenterId
     * @param projectId
     * @param netName
     * @param netId
     * @return
     * @throws AppException
     */
    public boolean checkNetworkName(String datacenterId,String projectId,String netName,String netId) throws AppException;
    
    /**
     * 修改网络
     * @param cloudNetWork
     * @return
     * @throws AppException
     */
    public CloudNetWork updateNetwork(CloudNetWork cloudNetWork) throws AppException;
    
    /**
     * 检查网络是否可删除
     * @param networkId
     * @return
     * @throws AppException
     */
    public EayunResponseJson checkForDel(String networkId) throws AppException;
    /**
     * 删除网络
     * @param datacenterId
     * @param projectId
     * @param networkId
     * @return
     * @throws AppException
     */
    public boolean deleteNetwork(CloudNetWork cloudNetWork) throws AppException;

    /**
     * 获取网络详情
     * @param id
     * @return
     * @throws AppException
     */
    public CloudNetWork getNetworkById(String id) throws AppException;
    
    /**
     * 根据项目查询私有网络使用量
     * @author zengbo
     * @param prjId
     * @return
     */
    public int getCountByPrjId(String prjId);
    
    /**
     * 查询项目下未绑定路由的私有网络
     * @param prjId
     * @return
     * @throws AppException
     */
    public List<CloudNetWork> getNotBindRouteNetworkByPrjId(String prjId) throws AppException;
    /**
     * 获取项目下的私有网络列表
     * @param prjId
     * @return
     * @throws AppException
     */
    public List<CloudNetWork> getNetworkListByPrjId(String prjId) throws AppException;
}

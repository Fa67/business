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
import com.eayun.virtualization.model.EcmcCloudNetwork;

/**
 *                       
 * @Filename: EcmcOutNetworkService.java
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
public interface EcmcOutNetworkService {
    
    /**
     * 分页获取外部网络
     * @param datacenterId 数据中心Id
     * @param pageNo 当前页码
     * @param pageSize 每页查询条数
     * @return
     * @throws AppException
     */
    public Page getOutNetworkList(Page page,ParamsMap paramsMap) throws AppException;
    /**
     * 获取所有的外部网络
     * @param map
     * @return
     */
    public List<EcmcCloudNetwork> getAllOutNetworkList(Map<String,String> map);
    
    /**
     * 修改外部网络
     * @param networkId
     * @param datacenterId
     * @param netName
     * @param admStateup
     * @return
     * @throws AppException
     */
    public EcmcCloudNetwork modifyOutNetwork(Map<String,String> map) throws AppException;
    
   
    /**
     * 查询指定外部网络
     * @param netId
     * @return
     * @throws AppException
     */
    public EcmcCloudNetwork getCloudNetworkById(Map<String,String> map) throws AppException;
    /**
     * 验证外部网络重名
     * @param map
     * @return
     */
	public boolean checkNetName(Map<String, String> map);
}

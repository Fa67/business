/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.virtualization.ecmcservice;

import java.util.List;
import java.util.Map;

import com.eayun.common.exception.AppException;
import com.eayun.virtualization.model.EcmcCloudSubNetwork;

/**
 *                       
 * @Filename: EcmcOutSubNetworService.java
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
public interface EcmcOutSubNetworService {
    /**
     * 获取外网子网列表信息
     * @param dcId 数据中心Id
     * @param netId 外网网络Id
     * @return
     * @throws AppException
     */
	public List<EcmcCloudSubNetwork> getOutSubNetworkList(Map<String,String> map) throws AppException ;
    
    /**
     * 添加外网子网
     * @param reqVo
     * @return
     * @throws AppException
     */
    public EcmcCloudSubNetwork addSubNetwork(EcmcCloudSubNetwork ecmcCloudSubNetwork) throws AppException;
    
    /**
     * 修改子网
     * @param subnetId
     * @param datacenterId
     * @param subnetName
     * @param gatewayIp
     * @param isForbidGateway
     * @return
     * @throws AppException
     */
    public EcmcCloudSubNetwork updateSubNetwork(Map<String,String> map) throws AppException;
    
    /**
     * 添加子网
     * @param datacenterId
     * @param id
     * @return
     * @throws AppException
     */
    public boolean deleteSubNetwork(Map<String,String> map) throws AppException;
    
    /**
     * 检查外网子网是否重名
     * @param netId
     * @param subnetName
     * @param subnetId
     * @return
     * @throws AppException
     */
    public boolean checkOutSubnetName(Map<String,String> map) throws AppException;

}

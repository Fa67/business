/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.virtualization.ecmcservice;

import java.util.List;
import java.util.Map;

import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.eayunstack.model.SubNetwork;
import com.eayun.virtualization.model.CloudSubNetWork;
import com.eayun.virtualization.model.EcmcCloudSubNetwork;

/**
 *                       
 * @Filename: EcmcSubNetworkService.java
 * @Description: 
 * @Version: 1.0
 * @Author: fangjun.yang
 * @Email: fangjun.yang@eayun.com
 * @History:<br>
 *<li>Date: 2016年4月1日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public interface EcmcSubNetworkService {
    
    /**
     * 获取子网列表
     * @param datacenterId
     * @param networkid
     * @return
     * @throws AppException
     */
    public List<Map<String, Object>> getSubNetworkList(String datacenterId, String networkid) throws AppException;
    
    /**
     * 添加子网
     * @param reqVo
     * @return
     * @throws AppException
     */
    public CloudSubNetWork addSubNetwork(CloudSubNetWork reqVo) throws AppException;
    
    /**
     * 修改子网
     * @param subnetId
     * @param datacenterId
     * @param projectId
     * @param subnetName
     * @param gatewayIp
     * @param isforbidgateway
     * @return
     * @throws AppException
     */
    public SubNetwork updateSubNetwork(CloudSubNetWork subNetWork) throws AppException;
    
    /**
     * 删除子网
     * @param datacenterId
     * @param id
     * @return
     * @throws AppException
     */
    public boolean deleteSubNetwork(String datacenterId, String id) throws AppException;
    /**
     * 获取子网详情
     * @param id
     * @return
     * @throws AppException
     */
    public EcmcCloudSubNetwork getSubNetworkById(String id) throws AppException;
    /**
     * 验证子网IP是否存在
     * @param subnetIP
     * @param netId
     * @return
     * @throws AppException
     */
    public boolean checkSameSubNetIP(String subnetIP, String netId) throws AppException;
    /**
     * 检查子网名称是否重名
     * @param datacenterId
     * @param projectId
     * @param subnetName
     * @param subnetId
     * @return
     * @throws AppException
     */
    public boolean checkSubNetWorkName(String datacenterId,String projectId,String subnetName,String subnetId) throws AppException;
    
    /**
     * 根据项目查询子网使用量
     * @author zengbo
     * @param prjId
     * @return
     */
    public int getCountByPrjId(String prjId);

	public List<EcmcCloudSubNetwork> getSubNetListByNetId(String netId);
	
	/**
	 * 查询网络下未绑定路由的子网
	 * @param datacenterId
	 * @param projectId
	 * @param netWorkId
	 * @return
	 * @throws AppException
	 */
	public List<CloudSubNetWork> getNotBindRouteSubnetList(String datacenterId,String projectId,String netWorkId) throws AppException;

	/**
	 * 判断子网是否允许删除
	 * @param subnetId
	 * @return
	 */
	public EayunResponseJson checkForDel(Map<String, String> map);

}

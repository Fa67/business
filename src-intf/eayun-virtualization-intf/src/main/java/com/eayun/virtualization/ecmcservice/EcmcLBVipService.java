/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.virtualization.ecmcservice;

import java.util.Map;

import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.virtualization.model.BaseCloudLdVip;
import com.eayun.virtualization.model.CloudLdVip;

/**
 *                       
 * @Filename: EcmcLBVipService.java
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
public interface EcmcLBVipService {

    /**
     * 验证vip名称是否重复
     * @param datacenterId
     * @param projectId
     * @param vipName
     * @param vipId
     * @return
     * @throws AppException
     */
    public boolean checkVipName(String datacenterId, String projectId, String vipName, String vipId) throws AppException;
    
    /**
     * 查询vip子网
     * @param datacenterId
     * @param projectId
     * @param pageNo
     * @param pageSize
     * @return
     * @throws AppException
     */
    public Page querySubnetList(String datacenterId, String projectId, Integer pageNo, Integer pageSize) throws AppException;
    
    /**
     * 分页查询vip
     * @param paramsMap
     * @return
     * @throws AppException
     */
    public Page findVipList(ParamsMap paramsMap) throws AppException;
    
    /**
     * 创建Vip
     * @param params
     * @return
     * @throws AppException
     */
    public BaseCloudLdVip createVip(Map<String, String> params) throws AppException;
    /**
     * 删除Vip
     * @param datacenterId
     * @param projectId
     * @param id
     * @return
     * @throws AppException
     */
    public boolean deleteVip(String datacenterId, String projectId, String id) throws AppException;
    /**
     * 修改Vip信息
     * @param params
     * @return
     * @throws AppException
     */
    public BaseCloudLdVip updateVip(Map<String, String> params) throws AppException;
    /**
     * 资源池下是否存在成员
     * @param poolId
     * @return
     * @throws AppException
     */
    public boolean existMember(String poolId) throws AppException;

    public BaseCloudLdVip addVip(CloudLdVip vip) throws AppException;

    public BaseCloudLdVip modifyVip(CloudLdVip ldVip) throws AppException;

    public boolean deleteVip(CloudLdVip cloudLdVip) throws AppException;
}

package com.eayun.eayunstack.service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.eayunstack.model.KeyPairs;
import com.eayun.eayunstack.model.VmUserData;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2017年2月24日
 */
public interface OpenstackSecretkeyService extends OpenstackBaseService<KeyPairs>{

    /**
     * 根据名称精确查询密钥
     */
    public KeyPairs getByName(String datacenterId, String projectId, String secretkeyName)throws AppException;
    
    /**
     * 查询虚拟机UserData
     * @param datacenterId
     * @param projectId
     * @param vm_id
     * @return
     * @throws AppException
     */
    public String getVmUserData(String datacenterId, String projectId, String vm_id)throws AppException;
    /**
     * 绑定/解绑密钥
     * @param datacenterId
     * @param projectId
     * @param vm_id
     * @param data
     * @return
     * @throws AppException
     */
    public String bindSecretKey(String datacenterId, String projectId,String vm_id, JSONObject data)throws AppException;
    /**
     * 获取云主机Metadata
     * @param datacenterId
     * @param projectId
     * @param vm_id
     * @return
     * @throws AppException
     */
    public String getVmMetadata(String datacenterId, String projectId,String vm_id)throws AppException;
    /**
     * 修改云主机Metadata
     * @param datacenterId
     * @param projectId
     * @param vm_id
     * @param data
     * @return
     * @throws AppException
     */
    public String updateVmMetadata(String datacenterId, String projectId,String vm_id, JSONObject data)throws AppException;
}

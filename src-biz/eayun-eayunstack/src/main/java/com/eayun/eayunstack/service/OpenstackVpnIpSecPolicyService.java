package com.eayun.eayunstack.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.eayunstack.model.VpnIpSecPolicy;
import com.eayun.virtualization.model.BaseCloudVpnIpSecPolicy;

public interface OpenstackVpnIpSecPolicyService extends OpenstackBaseService<VpnIpSecPolicy> {

    public List<VpnIpSecPolicy> listAll(String datacenterId) throws AppException;
    
    public List<VpnIpSecPolicy> list(String datacenterId, String projectId) throws AppException;
    
    public VpnIpSecPolicy getById(String datacenterId, String projectId, String id) throws AppException;
    
    public VpnIpSecPolicy create(String datacenterId, String projectId, JSONObject data) throws AppException;
    
    public VpnIpSecPolicy update(String datacenterId, String projectId, JSONObject data, String id) throws AppException;
    
    public boolean delete(String datacenterId, String projectId, String id) throws AppException;
    
    public BaseCloudVpnIpSecPolicy getStackById(String datacenterId, String id) throws AppException;
}

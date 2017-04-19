package com.eayun.eayunstack.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.eayunstack.model.VpnIkePolicy;
import com.eayun.virtualization.model.BaseCloudVpnIkePolicy;

public interface OpenstackVpnIkePolicyService extends OpenstackBaseService<VpnIkePolicy> {

    public List<VpnIkePolicy> listAll(String datacenterId) throws AppException;
    
    public List<VpnIkePolicy> list(String datacenterId, String projectId) throws AppException;
    
    public VpnIkePolicy getById(String datacenterId, String projectId, String id) throws AppException;
    
    public VpnIkePolicy create(String datacenterId, String projectId, JSONObject data) throws AppException;
    
    public VpnIkePolicy update(String datacenterId, String projectId, JSONObject data, String id) throws AppException;
    
    public boolean delete(String datacenterId, String projectId, String id) throws AppException;
    
    public BaseCloudVpnIkePolicy getStackById (String datacenterId, String id) throws AppException;
}

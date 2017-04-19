package com.eayun.eayunstack.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.VpnConnection;
import com.eayun.virtualization.model.BaseCloudVpnConn;

public interface OpenstackVpnConnectionService extends OpenstackBaseService<VpnConnection> {

    public List<VpnConnection> listAll(String datacenterId) throws AppException;
    
    public List<VpnConnection> list(String datacenterId, String projectId) throws AppException;
    
    public VpnConnection getById(String datacenterId, String projectId, String id) throws AppException;
    
    public VpnConnection create(String datacenterId, String projectId, JSONObject data) throws AppException;
    
    public VpnConnection update(String datacenterId, String projectId, JSONObject data, String id) throws AppException;
    
    public boolean delete(String datacenterId, String projectId, String id) throws AppException;
    
    public JSONObject get(String dcId, String id) throws Exception;
    
    public List<BaseCloudVpnConn> getStackList(BaseDcDataCenter datacenter) throws AppException;
}

package com.eayun.eayunstack.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.eayunstack.model.Vpn;
import com.eayun.virtualization.model.BaseCloudVpn;

public interface OpenstackVpnService extends OpenstackBaseService<Vpn> {
    
    public List<Vpn> listAll(String datacenterId) throws AppException;
    
    public List<Vpn> list(String datacenterId, String projectId) throws AppException;
    
    public Vpn getById(String datacenterId, String projectId, String id) throws AppException;
    
    public Vpn create(String datacenterId, String projectId, JSONObject data) throws AppException;
    
    public Vpn update(String datacenterId, String projectId, JSONObject data, String id) throws AppException;
    
    public boolean delete(String datacenterId, String projectId, String id) throws AppException;
    
    public JSONObject get(String dcId, String id) throws Exception;
    
    public BaseCloudVpn getStackById(String dcId, String id) throws Exception;
    
}

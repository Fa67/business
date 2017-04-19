package com.eayun.eayunstack.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.PortMapping;
import com.eayun.virtualization.model.BaseCloudPortMapping;


public interface OpenstackPortMappingService extends OpenstackBaseService<PortMapping> {
    
    public List<PortMapping> listAll(String datacenterId) throws AppException;
    
    public List<PortMapping> list(String datacenterId, String projectId) throws AppException;
    
    public PortMapping getById(String datacenterId, String projectId, String id) throws AppException;
    
    public PortMapping create(String datacenterId, String projectId, JSONObject data) throws AppException;
    
    public PortMapping update(String datacenterId, String projectId, JSONObject data, String id) throws AppException;
    
    public boolean delete(String datacenterId, String projectId, String id) throws AppException;
    
    public List<BaseCloudPortMapping> getStackList(BaseDcDataCenter datacenter) throws AppException;
    
}

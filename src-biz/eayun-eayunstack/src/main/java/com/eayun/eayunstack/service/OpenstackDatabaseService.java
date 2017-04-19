package com.eayun.eayunstack.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.database.database.model.BaseCloudRDSDatabase;

public interface OpenstackDatabaseService {

    public void list(String datacenterId, String projectId, String instanceId) throws AppException;
    
    public void create(String datacenterId, String projectId, String instanceId, JSONObject data) throws AppException;
    
    public boolean delete(String datacenterId, String projectId, String instanceId, String databaseName) throws AppException;
    
    public List<BaseCloudRDSDatabase> getStackList (String datacenterId, String prjId, String instanceId) throws AppException;
}

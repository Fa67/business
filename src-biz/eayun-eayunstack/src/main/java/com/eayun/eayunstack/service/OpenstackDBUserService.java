package com.eayun.eayunstack.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.database.account.model.CloudRDSAccount;
import com.eayun.eayunstack.model.RDSDBUser;

public interface OpenstackDBUserService {

    public void list(String datacenterId, String instanceId) throws AppException;
    
    public void databaseListManaged(String datacenterId, String instanceId, String name) throws AppException;
    
    public void create(String datacenterId, String projectId, String instanceId, JSONObject data) throws AppException;
    
    public void update(String datacenterId, String instanceId, JSONObject data) throws AppException;
    
    public boolean delete(String datacenterId, String instanceId, String name) throws AppException;
    
    public void grantAccess(String datacenterId, String instanceId, String name, JSONObject data) throws AppException;
    
    public boolean revokeAccess(String datacenterId, String instanceId, String name, String databaseName) throws AppException;
    
    public RDSDBUser enableRootUser (String datacenterId, String tenantId, String instanceId, JSONObject data) throws AppException;
    
    public List<CloudRDSAccount> getStackList (String datacenterId, String projectId, String instanceId) throws AppException;
}

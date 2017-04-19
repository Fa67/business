package com.eayun.eayunstack.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.database.account.model.CloudRDSAccount;
import com.eayun.eayunstack.model.RDSDBUser;
import com.eayun.eayunstack.model.Databases;
import com.eayun.eayunstack.service.OpenstackDBUserService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;
@Service
public class OpenstackDBUserServiceImpl extends 
        OpenstackBaseServiceImpl<RDSDBUser> implements OpenstackDBUserService {

    @Override
    public void list(String datacenterId, String instanceId) throws AppException {
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
                OpenstackUriConstant.TROVE_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.RDS_INSTANCES_URI + "/" + instanceId + OpenstackUriConstant.USER_URI);
        List<JSONObject> result = restService.list(restTokenBean,
                OpenstackUriConstant.USER_DATA_NAMES);
    }
    
    @Override
    public void databaseListManaged(String datacenterId, String instanceId, String name) throws AppException {
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
                OpenstackUriConstant.TROVE_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.RDS_INSTANCES_URI + "/" + instanceId + OpenstackUriConstant.USER_URI + "/" + name);
        restService.list(restTokenBean,
                OpenstackUriConstant.USER_DATA_NAME);
    }
    
    @Override
    public void create(String datacenterId, String projectId, String instanceId, JSONObject data) throws AppException {
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
                OpenstackUriConstant.TROVE_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.RDS_INSTANCES_URI + "/" + instanceId + OpenstackUriConstant.USER_URI);
        restService.create(restTokenBean, null, data);
    }
    
    @Override
    public void update(String datacenterId, String instanceId, JSONObject data) throws AppException {
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
                OpenstackUriConstant.TROVE_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.RDS_INSTANCES_URI + "/" + instanceId + OpenstackUriConstant.USER_URI);
        restService.update(restTokenBean, null, data);
    }
    
    @Override
    public boolean delete(String datacenterId, String instanceId, String name) throws AppException {
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
                OpenstackUriConstant.TROVE_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.RDS_INSTANCES_URI + "/" + instanceId + OpenstackUriConstant.USER_URI + "/" + name);
        return restService.delete(restTokenBean);
    }
    
    @Override
    public void grantAccess(String datacenterId, String instanceId, String name, JSONObject data) throws AppException {
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
                OpenstackUriConstant.TROVE_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.RDS_INSTANCES_URI + "/" + instanceId + OpenstackUriConstant.USER_URI + "/" + name + OpenstackUriConstant.RDS_DATABASE_URI);
        restService.update(restTokenBean, null, data);
    }
    
    @Override
    public boolean revokeAccess(String datacenterId, String instanceId, String name, String databaseName) throws AppException {
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
                OpenstackUriConstant.TROVE_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.RDS_INSTANCES_URI +"/" + instanceId +
                OpenstackUriConstant.USER_URI + "/" + name + 
                OpenstackUriConstant.RDS_DATABASE_URI + "/" + databaseName);
        return restService.delete(restTokenBean);
    }
    
    @Override
    public RDSDBUser enableRootUser (String datacenterId, String tenantId, String instanceId, JSONObject data) throws AppException {
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId, tenantId,
                OpenstackUriConstant.TROVE_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.RDS_INSTANCES_URI + "/" + instanceId +
                OpenstackUriConstant.ROOT_URI);
        JSONObject result = restService.create(restTokenBean, OpenstackUriConstant.RDS_USER_DATA_NAME, data);
        RDSDBUser dbUser = restService.json2bean(result, RDSDBUser.class);
        return dbUser;
    }
    
    @Override
    public List<CloudRDSAccount> getStackList (String datacenterId, String projectId, String instanceId) throws AppException {
        List<CloudRDSAccount> list = new ArrayList<CloudRDSAccount>();
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
                OpenstackUriConstant.TROVE_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.RDS_INSTANCES_URI + "/" + instanceId + OpenstackUriConstant.USER_URI);
        List<JSONObject> result = restService.list(restTokenBean,
                OpenstackUriConstant.USER_DATA_NAMES);
        if (null != result && result.size() > 0) {
            for (JSONObject json : result) {
                RDSDBUser dbUser = restService.json2bean(json, RDSDBUser.class);
                CloudRDSAccount account = new CloudRDSAccount();
                account.setAccountName(dbUser.getName());
                account.setInstanceId(instanceId);
                account.setPrjId(projectId);
                account.setDcId(datacenterId);
                List<String> dbNameList = new ArrayList<String>();
                for (Databases database : dbUser.getDatabases()) {
                    dbNameList.add(database.getName());
                }
                account.setDbNameList(dbNameList);
                account.setRemark("");
                list.add(account);
            }
        }
        return list;
    }
}

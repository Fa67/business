package com.eayun.eayunstack.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.database.database.model.BaseCloudRDSDatabase;
import com.eayun.eayunstack.model.RDSDatabase;
import com.eayun.eayunstack.service.OpenstackDatabaseService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;
@Service
public class OpenstackDatabaseServiceImpl extends
        OpenstackBaseServiceImpl<RDSDatabase> implements OpenstackDatabaseService {
    
    @Override
    public void list(String datacenterId, String projectId, String instanceId) throws AppException {
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId, 
                OpenstackUriConstant.TROVE_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.RDS_INSTANCES_URI + "/" + instanceId + OpenstackUriConstant.RDS_DATABASE_URI);
        List<JSONObject> result = restService.list(restTokenBean,
                OpenstackUriConstant.RDS_DATABASE_DATA_NAME);
    }
    
    @Override
    public void create(String datacenterId, String projectId, String instanceId, JSONObject data) throws AppException {
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
                OpenstackUriConstant.TROVE_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.RDS_INSTANCES_URI + "/" + instanceId + OpenstackUriConstant.RDS_DATABASE_URI);
        restService.create(restTokenBean, null, data);
    }
    
    @Override
    public boolean delete(String datacenterId, String projectId, String instanceId, String databaseName) throws AppException {
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
                OpenstackUriConstant.TROVE_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.RDS_INSTANCES_URI + "/" + instanceId + OpenstackUriConstant.RDS_DATABASE_URI + "/" + databaseName);
        return restService.delete(restTokenBean);
    }
    
    @Override
    public List<BaseCloudRDSDatabase> getStackList (String datacenterId, String projectjId, String instanceId) throws AppException {
        List<BaseCloudRDSDatabase> list = new ArrayList<BaseCloudRDSDatabase>();
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId, 
                OpenstackUriConstant.TROVE_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.RDS_INSTANCES_URI + "/" + instanceId + OpenstackUriConstant.RDS_DATABASE_URI);
        List<JSONObject> result = restService.list(restTokenBean,
                OpenstackUriConstant.RDS_DATABASE_DATA_NAMES);
        if (null != result && result.size() > 0) {
            for (JSONObject json : result) {
                RDSDatabase database = restService.json2bean(json,
                        RDSDatabase.class);
                BaseCloudRDSDatabase baseDB = new BaseCloudRDSDatabase(database, datacenterId, projectjId, instanceId);
                list.add(baseDB);
            }
        }
        return list;
    }
}

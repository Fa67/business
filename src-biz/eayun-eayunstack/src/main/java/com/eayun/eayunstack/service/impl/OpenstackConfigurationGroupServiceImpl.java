package com.eayun.eayunstack.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.database.configgroup.model.datastore.Datastores;
import com.eayun.eayunstack.service.OpenstackConfigurationGroupService;
import com.eayun.eayunstack.service.RestService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OpenstackConfigurationGroupServiceImpl extends OpenstackBaseServiceImpl<Object>
    implements OpenstackConfigurationGroupService{

    @Autowired
    private RestService restService ;


    @Override
    public JSONObject listConfigurationGroups(String dcId, String projectId) throws AppException {
        RestTokenBean restTokenBean = getRestTokenBean(dcId, projectId , OpenstackUriConstant.TROVE_SERVICE_URI);
        restTokenBean.setUrl("/configurations");
        return restService.getResponse(restTokenBean) ;
    }

    @Override
    public Datastores listDatastores(String dcId, String projectId) throws AppException {
        RestTokenBean restTokenBean = getRestTokenBean(dcId, projectId , OpenstackUriConstant.TROVE_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.RDS_DATASTORES_URI);
        JSONObject resp = restService.getResponse(restTokenBean) ;
        return JSONObject.parseObject(resp.toJSONString(), Datastores.class);
    }

    @Override
    public JSONObject queryConfigurationGroup(String dcId, String projectId, String groupId) throws AppException {
        RestTokenBean restTokenBean = getRestTokenBean(dcId, projectId , OpenstackUriConstant.TROVE_SERVICE_URI);
        restTokenBean.setUrl("/configurations/" + groupId);
        return restService.getResponse(restTokenBean) ;
    }

    @Override
    public JSONObject queryConfigurationGroupInstances(String dcId, String projectId, String groupId) throws AppException {
        RestTokenBean restTokenBean = getRestTokenBean(dcId, projectId , OpenstackUriConstant.TROVE_SERVICE_URI);
        restTokenBean.setUrl("/configurations/" + groupId + "/instances");
        return restService.getResponse(restTokenBean) ;
    }

    @Override
    public JSONObject createConfigurationGroup(String dcId, String projectId, JSONObject dataStore, String fileName, JSONObject configParams) throws AppException {
        RestTokenBean restTokenBean = getRestTokenBean(dcId, projectId , OpenstackUriConstant.TROVE_SERVICE_URI);
        restTokenBean.setUrl("/configurations");//"troves"
        JSONObject body = new JSONObject() ;
        JSONObject configuration = new JSONObject() ;
        configuration.put("name", fileName) ;
        configuration.put("description", fileName) ;
        configuration.put("datastore", dataStore) ;
        configuration.put("values", configParams) ;
        body.put("configuration", configuration) ;
        System.out.println(" ----- ----- ----- ");
        System.out.println(body.toJSONString());
        System.out.println(" ----- ----- ----- ");
        return restService.post(restTokenBean, body) ;
    }

    @Override
    public boolean deleteConfigurationGroup(String dcId, String projectId, String groupId) throws AppException {
        RestTokenBean restTokenBean = getRestTokenBean(dcId, projectId , OpenstackUriConstant.TROVE_SERVICE_URI);
        restTokenBean.setUrl("/configurations/" + groupId);
        return restService.delete(restTokenBean) ;
    }

    @Override
    public JSONObject updateConfigurationGroup(String dcId, String projectId, String groupId, JSONObject postContent) throws AppException {
        RestTokenBean restTokenBean = getRestTokenBean(dcId, projectId , OpenstackUriConstant.TROVE_SERVICE_URI);
        restTokenBean.setUrl("/configurations/" + groupId);//"troves"
        JSONObject body = new JSONObject() ;
        JSONObject configuration = new JSONObject() ;
        configuration.put("values", postContent) ;
        body.put("configuration", configuration) ;
        return restService.put(restTokenBean, body) ;
    }

    @Override
    public JSONObject attachConfigurationGroupToInstance(String dcId, String projectId, String groupId, String instanceId) throws AppException {
        RestTokenBean restTokenBean = getRestTokenBean(dcId, projectId , OpenstackUriConstant.TROVE_SERVICE_URI);
        restTokenBean.setUrl("/instances/" + instanceId);//"troves"
        JSONObject body = new JSONObject() ;
        JSONObject instance = new JSONObject() ;
        instance.put("configuration", groupId) ;
        body.put("instance", instance) ;
        return restService.put(restTokenBean, body) ;
    }

    @Override
    public JSONObject detachConfigurationGroupToInstance(String dcId, String projectId, String instanceId) throws AppException {
        RestTokenBean restTokenBean = getRestTokenBean(dcId, projectId , OpenstackUriConstant.TROVE_SERVICE_URI);
        restTokenBean.setUrl("/instances/" + instanceId);//"troves"
        JSONObject body = new JSONObject() ;
        body.put("instance", new JSONObject()) ;
        return restService.put(restTokenBean, body) ;
    }

    @Override
    public List<Object> listAll(String datacenterId) throws AppException {
        return null;
    }

    @Override
    public List<Object> list(String datacenterId, String projectId) throws AppException {
        return null;
    }

    @Override
    public Object getById(String datacenterId, String projectId, String id) throws AppException {
        return null;
    }

    @Override
    public Object create(String datacenterId, String projectId, JSONObject data) throws AppException {
        return null;
    }

    @Override
    public boolean delete(String datacenterId, String projectId, String id) throws AppException {
        return false;
    }

    @Override
    public Object update(String datacenterId, String projectId, JSONObject data, String id) throws AppException {
        return null;
    }
}

package com.eayun.eayunstack.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.eayunstack.model.VpnIkePolicy;
import com.eayun.eayunstack.service.OpenstackVpnIkePolicyService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;
import com.eayun.virtualization.model.BaseCloudVpnIkePolicy;
@Service
public class OpenstackVpnIkePolicyServiceImpl extends
OpenstackBaseServiceImpl<VpnIkePolicy> implements OpenstackVpnIkePolicyService {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory
            .getLogger(OpenstackVpnIkePolicyServiceImpl.class);
    
    @Override
    public List<VpnIkePolicy> listAll(String datacenterId) throws AppException {
        List<VpnIkePolicy> list = null;
        
        return list;
    }
    
    @Override
    public List<VpnIkePolicy> list(String datacenterId, String projectId) throws AppException {
        List<VpnIkePolicy> list = null;
        
        return list;
    }
    
    @Override
    public VpnIkePolicy getById(String datacenterId, String projectId, String id) throws AppException {
        
        return new VpnIkePolicy();
    }
    
    @Override
    public VpnIkePolicy create(String datacenterId, String projectId, JSONObject data) throws AppException {
        VpnIkePolicy vpnIkePolicy = null;
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
                OpenstackUriConstant.NETWORK_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.IKE_URI);
        JSONObject result = restService.create(restTokenBean,
                OpenstackUriConstant.IKE_DATA_NAME, data);
        vpnIkePolicy = restService.json2bean(result, VpnIkePolicy.class);
        return vpnIkePolicy;
    }
    
    @Override
    public VpnIkePolicy update(String datacenterId, String projectId, JSONObject data, String id) throws AppException {
        return new VpnIkePolicy();
    }
    
    @Override
    public boolean delete(String datacenterId, String projectId, String id) throws AppException {
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
                OpenstackUriConstant.NETWORK_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.IKE_URI + "/" + id);
        return restService.delete(restTokenBean);
    }
    
    @Override
    public BaseCloudVpnIkePolicy getStackById (String datacenterId, String id) throws AppException {
        BaseCloudVpnIkePolicy ikePolicy = null;
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
                OpenstackUriConstant.NETWORK_SERVICE_URI);
        JSONObject result = restService.getById(restTokenBean,
                OpenstackUriConstant.IKE_URI + "/",
                OpenstackUriConstant.IKE_DATA_NAME, id);
        if (result != null) {
            VpnIkePolicy data = restService.json2bean(result, VpnIkePolicy.class);
            ikePolicy = new BaseCloudVpnIkePolicy(datacenterId, data);
        }
        return ikePolicy;
    }

}

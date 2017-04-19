package com.eayun.eayunstack.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.eayunstack.model.VpnIpSecPolicy;
import com.eayun.eayunstack.service.OpenstackVpnIpSecPolicyService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;
import com.eayun.virtualization.model.BaseCloudVpnIpSecPolicy;
@Service
public class OpenstackVpnIpSecPolicyServiceImpl extends
        OpenstackBaseServiceImpl<VpnIpSecPolicy> implements OpenstackVpnIpSecPolicyService {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory
            .getLogger(OpenstackVpnIpSecPolicyServiceImpl.class);
    
    @Override
    public List<VpnIpSecPolicy> listAll(String datacenterId) throws AppException {
        List<VpnIpSecPolicy> list = null;
        
        return list;
    }
    
    @Override
    public List<VpnIpSecPolicy> list(String datacenterId, String projectId) throws AppException {
        List<VpnIpSecPolicy> list = null;
        
        return list;
    }
    
    @Override
    public VpnIpSecPolicy getById(String datacenterId, String projectId, String id) throws AppException {
        
        return new VpnIpSecPolicy();
    }
    
    @Override
    public VpnIpSecPolicy create(String datacenterId, String projectId, JSONObject data) throws AppException {
        VpnIpSecPolicy vpnIpSecPolicy = null;
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
                OpenstackUriConstant.NETWORK_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.IPSEC_URI);
        JSONObject result = restService.create(restTokenBean,
                OpenstackUriConstant.IPSEC_DATA_NAME, data);
        vpnIpSecPolicy = restService.json2bean(result, VpnIpSecPolicy.class);
        return vpnIpSecPolicy;
    }
    
    @Override
    public VpnIpSecPolicy update(String datacenterId, String projectId, JSONObject data, String id) throws AppException {
        return new VpnIpSecPolicy();
    }
    
    @Override
    public boolean delete(String datacenterId, String projectId, String id) throws AppException {
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
                OpenstackUriConstant.NETWORK_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.IPSEC_URI + "/" + id);
        return restService.delete(restTokenBean);
    }
    
    @Override
    public BaseCloudVpnIpSecPolicy getStackById(String datacenterId, String id) throws AppException {
        BaseCloudVpnIpSecPolicy vpnservice = null;
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
                OpenstackUriConstant.NETWORK_SERVICE_URI);
        JSONObject result = restService.getById(restTokenBean,
                OpenstackUriConstant.IPSEC_URI + "/",
                OpenstackUriConstant.IPSEC_DATA_NAME, id);
        if (result != null) {
            VpnIpSecPolicy data = restService.json2bean(result, VpnIpSecPolicy.class);
            vpnservice = new BaseCloudVpnIpSecPolicy(datacenterId, data);
        }
        return vpnservice;
    }
}

package com.eayun.eayunstack.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.eayunstack.model.Vpn;
import com.eayun.eayunstack.service.OpenstackVpnService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;
import com.eayun.virtualization.model.BaseCloudVpn;
@Service
public class OpenstackVpnServiceImpl extends
        OpenstackBaseServiceImpl<Vpn> implements OpenstackVpnService {
    
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory
            .getLogger(OpenstackVpnServiceImpl.class);
    
    @Override
    public List<Vpn> listAll(String datacenterId) throws AppException {
        List<Vpn> list = null;
        
        return list;
    }
    
    @Override
    public List<Vpn> list(String datacenterId, String projectId) throws AppException {
        List<Vpn> list = null;
        
        return list;
    }
    
    @Override
    public Vpn getById(String datacenterId, String projectId, String id) throws AppException {
        
        return new Vpn();
    }
    
    @Override
    public Vpn create(String datacenterId, String projectId, JSONObject data) throws AppException {
        Vpn vpn = null;
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
                OpenstackUriConstant.NETWORK_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.VPN_URI);
        JSONObject result = restService.create(restTokenBean,
                OpenstackUriConstant.VPN_DATA_NAME, data);
        vpn = restService.json2bean(result, Vpn.class);
        return vpn;
    }
    
    @Override
    public Vpn update(String datacenterId, String projectId, JSONObject data, String id) throws AppException {
        return new Vpn();
    }
    
    @Override
    public boolean delete(String datacenterId, String projectId, String id) throws AppException {
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
                OpenstackUriConstant.NETWORK_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.VPN_URI + "/" + id);
        return restService.delete(restTokenBean);
    }
    
    @Override
    public JSONObject get(String dcId, String id) throws Exception {
        RestTokenBean restTokenBean = getRestTokenBean(dcId,
                OpenstackUriConstant.NETWORK_SERVICE_URI);
        return restService.getJSONById(restTokenBean,
                OpenstackUriConstant.VPN_URI + "/", id);
    }
    
    @Override
    public BaseCloudVpn getStackById(String datacenterId, String id) throws Exception {
        BaseCloudVpn vpnservice = null;
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
                OpenstackUriConstant.NETWORK_SERVICE_URI);
        JSONObject result = restService.getById(restTokenBean,
                OpenstackUriConstant.VPN_URI + "/",
                OpenstackUriConstant.VPN_DATA_NAME, id);
        if (result != null) {
            Vpn data = restService.json2bean(result, Vpn.class);
            vpnservice = new BaseCloudVpn(datacenterId, data);
        }
        return vpnservice;
    }

}

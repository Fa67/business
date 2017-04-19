package com.eayun.eayunstack.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.VpnConnection;
import com.eayun.eayunstack.service.OpenstackVpnConnectionService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;
import com.eayun.virtualization.model.BaseCloudVpnConn;
@Service
public class OpenstackVpnConnectionServiceImpl extends
OpenstackBaseServiceImpl<VpnConnection> implements OpenstackVpnConnectionService {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory
            .getLogger(OpenstackVpnConnectionServiceImpl.class);
    
    @Override
    public List<VpnConnection> list(String datacenterId, String projectId) throws AppException {
        List<VpnConnection> list = null;
        list = this.listAll(datacenterId);
        if (list != null && list.size() > 0) {
            List<VpnConnection> temp = new ArrayList<VpnConnection>();
            for (VpnConnection vpnConn : list) {
                if (vpnConn.getTenant_id().equals(projectId)) {
                    temp.add(vpnConn);
                }
            }
            list = temp;
        }
        return list;
    }
    
    @Override
    public List<VpnConnection> listAll(String datacenterId) throws AppException {
        List<VpnConnection> list = null;
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
                OpenstackUriConstant.NETWORK_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.IPSEC_CONNECTION_URI);
        List<JSONObject> result = restService.list(restTokenBean,
                OpenstackUriConstant.IPSEC_CONNECTION_DATA_NAME);

        if (result != null && result.size() > 0) {
            for (JSONObject jsonObject : result) {
                if (list == null) {
                    list = new ArrayList<VpnConnection>();
                }
                VpnConnection data = restService.json2bean(jsonObject, VpnConnection.class);
                list.add(data);
            }
        }
        return list;
    }
    
    @Override
    public VpnConnection getById(String datacenterId, String projectId, String id) throws AppException {
        
        return new VpnConnection();
    }
    
    @Override
    public VpnConnection create(String datacenterId, String projectId, JSONObject data) throws AppException {
        VpnConnection vpnConnection = null;
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
                OpenstackUriConstant.NETWORK_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.IPSEC_CONNECTION_URI);
        JSONObject result = restService.create(restTokenBean,
                OpenstackUriConstant.IPSEC_CONNECTION_DATA_NAME, data);
        vpnConnection = restService.json2bean(result, VpnConnection.class);
        return vpnConnection;
    }
    
    @Override
    public VpnConnection update(String datacenterId, String projectId, JSONObject data, String id) throws AppException {
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
                OpenstackUriConstant.NETWORK_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.IPSEC_CONNECTION_URI + "/" + id);
        JSONObject result = restService.update(restTokenBean,
                OpenstackUriConstant.IPSEC_CONNECTION_DATA_NAME, data);
        VpnConnection vpnConnection = restService.json2bean(result, VpnConnection.class);
        return vpnConnection;
    }
    
    @Override
    public boolean delete(String datacenterId, String projectId, String id) throws AppException {
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
                OpenstackUriConstant.NETWORK_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.IPSEC_CONNECTION_URI + "/" + id);
        return restService.delete(restTokenBean);
    }
    
    @Override
    public JSONObject get(String dcId, String id) throws Exception {
        RestTokenBean restTokenBean = getRestTokenBean(dcId,
                OpenstackUriConstant.NETWORK_SERVICE_URI);
        return restService.getJSONById(restTokenBean,
                OpenstackUriConstant.IPSEC_CONNECTION_URI + "/", id);
    }
    
    @Override
    public List<BaseCloudVpnConn> getStackList(BaseDcDataCenter datacenter) throws AppException {
        List<BaseCloudVpnConn> list = new ArrayList<BaseCloudVpnConn>();
        RestTokenBean restTokenBean = getRestTokenBean(datacenter.getId(),
                OpenstackUriConstant.NETWORK_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.IPSEC_CONNECTION_URI);
        List<JSONObject> result = restService.list(restTokenBean,
                OpenstackUriConstant.IPSEC_CONNECTION_DATA_NAMES);
        if (result != null && result.size() > 0) {                                                              
            for (JSONObject jsonObject : result) {                                                                
                VpnConnection data = restService.json2bean(jsonObject,                                             
                            VpnConnection.class);                                                                            
                BaseCloudVpnConn cvc = new BaseCloudVpnConn(data, datacenter.getId());                           
                    list.add(cvc);                                                                                    
                }                                                                                                   
            }                                                                                                     
        return list;
    }
}

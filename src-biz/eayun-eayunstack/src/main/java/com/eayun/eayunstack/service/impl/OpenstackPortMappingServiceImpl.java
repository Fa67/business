package com.eayun.eayunstack.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.Network;
import com.eayun.eayunstack.model.PortMapping;
import com.eayun.eayunstack.service.OpenstackPortMappingService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;
import com.eayun.virtualization.model.BaseCloudPortMapping;
@Service
public class OpenstackPortMappingServiceImpl extends
        OpenstackBaseServiceImpl<PortMapping> implements OpenstackPortMappingService {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory
            .getLogger(OpenstackPortMappingServiceImpl.class);
    
    @SuppressWarnings("unused")
    public List<PortMapping> listAll(String datacenterId) throws AppException {
        List<PortMapping> list = null;
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
                OpenstackUriConstant.NETWORK_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.PORTMAPPING_URI);
        List<JSONObject> result = restService.list(restTokenBean,
                OpenstackUriConstant.PORTMAPPING_DATA_NAMES);

        if (result != null && result.size() > 0) {
            for (JSONObject jsonObject : result) {
                if (list == null) {
                    list = new ArrayList<PortMapping>();
                }
                Network data = restService.json2bean(jsonObject, Network.class);
//                initData(data, jsonObject);
//                list.add(data);
            }
        }
        return list;
    }
    
    public List<PortMapping> list(String datacenterId, String projectId) throws AppException {
        List<PortMapping> list = new ArrayList<PortMapping>();
        
        return list;
    }
    
    public PortMapping getById(String datacenterId, String projectId, String id) throws AppException {
        PortMapping portMapping = null;
        
        return portMapping;
    }
    
    public PortMapping create(String datacenterId, String projectId, JSONObject data) throws AppException {
        PortMapping portMapping = null;
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
                OpenstackUriConstant.NETWORK_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.PORTMAPPING_URI);
        JSONObject result = restService.create(restTokenBean,
                OpenstackUriConstant.PORTMAPPING_DATA_NAME, data);
        portMapping = restService.json2bean(result, PortMapping.class);
        return portMapping;
    }
    
    public PortMapping update(String datacenterId, String projectId, JSONObject data, String id) throws AppException {
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
                OpenstackUriConstant.NETWORK_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.PORTMAPPING_URI + "/" + id);
        JSONObject result = restService.update(restTokenBean,
                OpenstackUriConstant.PORTMAPPING_DATA_NAME, data);
        PortMapping portMapping = restService.json2bean(result, PortMapping.class);
        return portMapping;
    }
    
    public boolean delete(String datacenterId, String projectId, String id) throws AppException {
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
                OpenstackUriConstant.NETWORK_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.PORTMAPPING_URI + "/" + id);
        return restService.delete(restTokenBean);
    }
    
    @Override
    public List<BaseCloudPortMapping> getStackList(BaseDcDataCenter datacenter) throws AppException {
        List<BaseCloudPortMapping> list = new ArrayList<BaseCloudPortMapping>();
        RestTokenBean restTokenBean = getRestTokenBean(datacenter.getId(),
                OpenstackUriConstant.NETWORK_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.PORTMAPPING_URI);
        List<JSONObject> result = restService.list(restTokenBean,
                OpenstackUriConstant.PORTMAPPING_DATA_NAMES);
        if (result != null && result.size() > 0) {                                                              
            for (JSONObject jsonObject : result) {                                                                
                PortMapping data = restService.json2bean(jsonObject,                                             
                        PortMapping.class);                                                                            
                BaseCloudPortMapping cpm = new BaseCloudPortMapping(data, datacenter.getId());                           
                    list.add(cpm);                                                                                    
                }                                                                                                   
            }                                                                                                     
        return list;
    }
}

package com.eayun.eayunstack.service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.eayunstack.model.LabelRule;
import com.eayun.eayunstack.model.MeteringLabel;

public interface OpenstackMeterLabelService {
    
    public LabelRule create(String datacenterId, JSONObject data) throws AppException;

    public boolean delete(String datacenterId, String labelRuleId) throws AppException;

    public MeteringLabel labelCreate(String datacenterId, JSONObject data) throws AppException;
    
    public JSONObject labelShow(String datacenterId,String labelId) throws AppException;
    
    public boolean labelDelete(String datacenterId, String labelId) throws AppException;

}

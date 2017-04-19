package com.eayun.eayunstack.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.eayunstack.model.LabelRule;
import com.eayun.eayunstack.model.MeteringLabel;
import com.eayun.eayunstack.service.OpenstackMeterLabelService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;

@Service
public class OpenstackMeterLabelServiceImpl extends OpenstackBaseServiceImpl<LabelRule> implements
                                                                                       OpenstackMeterLabelService {
    private static final Log log = LogFactory.getLog(OpenstackMeterLabelService.class);

    @Override
    public LabelRule create(String datacenterId, JSONObject data) throws AppException {
        LabelRule labelRule = null;
        try {
            RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
                OpenstackUriConstant.NETWORK_SERVICE_URI);
            restTokenBean.setUrl(OpenstackUriConstant.METERING_LABELS_RULE_CREATE);
            JSONObject result = restService.create(restTokenBean,
                OpenstackUriConstant.METERING_LABELS_RULE_DATANAME, data);
            labelRule = restService.json2bean(result, LabelRule.class);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error(e, e);
            return null;
        }
        return labelRule;
    }

    @Override
    public boolean delete(String datacenterId, String labelRuleId) throws AppException {
        try {
            RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
                OpenstackUriConstant.NETWORK_SERVICE_URI);
            restTokenBean.setUrl(OpenstackUriConstant.METERING_LABELS_RULE_CREATE + "/"
                                 + labelRuleId);
            return restService.delete(restTokenBean);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error(e, e);
            return false;
        }
    }

    @Override
    public MeteringLabel labelCreate(String datacenterId, JSONObject data) throws AppException {
        MeteringLabel meteringLabel = null;
        try {
            RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
                OpenstackUriConstant.NETWORK_SERVICE_URI);
            restTokenBean.setUrl(OpenstackUriConstant.METERING_LABELS_CREATE);
            JSONObject result = restService.create(restTokenBean,
                OpenstackUriConstant.METERING_LABELS_DATANAME, data);
            meteringLabel = restService.json2bean(result, MeteringLabel.class);
        } catch (AppException e) {
            log.error(e, e);
            throw e;
        } catch (Exception e) {
            log.error(e, e);
            return null;
        }
        return meteringLabel;
    }

    @Override
    public JSONObject labelShow(String datacenterId, String labelId) throws AppException {
        try {
            RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
                OpenstackUriConstant.NETWORK_SERVICE_URI);
            restTokenBean.setUrl(OpenstackUriConstant.METERING_LABELS_CREATE + "/" + labelId);
            JSONObject result = restService.get(restTokenBean,
                OpenstackUriConstant.METERING_LABELS_DATANAME);
            return result;
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error(e, e);
            return null;
        }
    }

    @Override
    public boolean labelDelete(String datacenterId, String labelId) throws AppException {
        try {
            RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
                OpenstackUriConstant.NETWORK_SERVICE_URI);
            restTokenBean.setUrl(OpenstackUriConstant.METERING_LABELS_CREATE + "/" + labelId);
            return restService.delete(restTokenBean);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error(e, e);
            return false;
        }
    }
}

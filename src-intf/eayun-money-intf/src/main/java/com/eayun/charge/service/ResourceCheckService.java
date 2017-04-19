package com.eayun.charge.service;

import com.eayun.charge.bean.ResourceCheckBean;
import com.eayun.charge.model.ChargeRecord;

/**
 * Created by ZH.F on 2016/10/17.
 */
public interface ResourceCheckService {

    ResourceCheckBean isExisted(String resId, String resType);

    void updateChargeRecordState(ChargeRecord chargeRecord, String isValid, String resStatus);
}

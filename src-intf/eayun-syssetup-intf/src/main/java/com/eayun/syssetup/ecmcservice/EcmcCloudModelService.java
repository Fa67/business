package com.eayun.syssetup.ecmcservice;

import com.eayun.common.exception.AppException;
import com.eayun.syssetup.model.CloudModel;

import java.util.List;

/**
 * Created by eayun on 2016/5/6.
 */
public interface EcmcCloudModelService {

    public List<CloudModel> getModelListByCustomer(String customerId) throws AppException;
}

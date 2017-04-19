package com.eayun.syssetup.ecmcservice.impl;

import com.eayun.common.exception.AppException;
import com.eayun.common.util.BeanUtils;
import com.eayun.syssetup.dao.CloudModelDao;
import com.eayun.syssetup.ecmcservice.EcmcCloudModelService;
import com.eayun.syssetup.model.BaseCloudModel;
import com.eayun.syssetup.model.CloudModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eayun on 2016/5/6.
 */
@Service
@Transactional
public class EcmcCloudModelServiceImpl implements EcmcCloudModelService {
    private static final Logger log = LoggerFactory.getLogger(EcmcCloudModelServiceImpl.class);

    @Autowired
    private CloudModelDao cloudModelDao;

    public List<CloudModel> getModelListByCustomer(String customerId) throws AppException {
        List<BaseCloudModel> baseCloudModelList = cloudModelDao.getModelListByCustomer(customerId);
        List<CloudModel> cloudModelList = new ArrayList<CloudModel>();
        for(BaseCloudModel baseCloudModel : baseCloudModelList){
            CloudModel cloudModel = new CloudModel();
            BeanUtils.copyPropertiesByModel(cloudModel, baseCloudModel);
            cloudModelList.add(cloudModel);
        }

        return cloudModelList;
    }
}

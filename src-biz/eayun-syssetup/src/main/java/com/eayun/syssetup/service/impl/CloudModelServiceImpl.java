package com.eayun.syssetup.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.util.BeanUtils;
import com.eayun.syssetup.dao.CloudModelDao;
import com.eayun.syssetup.model.BaseCloudModel;
import com.eayun.syssetup.model.CloudModel;
import com.eayun.syssetup.service.CloudModelService;
/** 
 * @Filename: CloudModelServiceImpl.java
 * @Description: 业务接口配置Service实现
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2015年9月22日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Service
@Transactional
public class CloudModelServiceImpl implements CloudModelService {

    private static final Logger   log = LoggerFactory.getLogger(CloudModelServiceImpl.class);
    @Autowired
    private CloudModelDao cloudModelDao;
    
    @Override
    public List<CloudModel> getModelListByCustomer(String modelCusid) throws AppException {
        log.info("云主机型号列表查询");
        
        List<BaseCloudModel> baseCloudModelList = cloudModelDao.getModelListByCustomer(modelCusid);
        List<CloudModel> cloudModelList = new ArrayList<CloudModel>();
        for(BaseCloudModel baseCloudModel : baseCloudModelList){
            CloudModel cloudModel = new CloudModel();
            BeanUtils.copyPropertiesByModel(cloudModel, baseCloudModel);
            cloudModelList.add(cloudModel);
        }
        
        return cloudModelList;
    }

    @Override
    public CloudModel addCloudModel(CloudModel cloudModel) throws AppException {
        log.info("添加云主机型号");
        Assert.notNull(cloudModel.getModelName());
        
        int modelNum = cloudModelDao.getModelCountByCustomer(cloudModel.getModelCusid());
        if(modelNum >=3){
            cloudModel.setErrorMessage("云主机型号总数已达上限");
            return cloudModel;
        }
        int NameNum = cloudModelDao.getModelCountByCusAndName(cloudModel.getModelCusid(), cloudModel.getModelName());
        if(NameNum > 0){
            cloudModel.setErrorMessage("云主机类型名称重复");
            return cloudModel;
        }
        BaseCloudModel baseCloudModel = new BaseCloudModel();
        BeanUtils.copyPropertiesByModel(baseCloudModel, cloudModel);
        cloudModelDao.saveEntity(baseCloudModel);
        BeanUtils.copyPropertiesByModel(cloudModel, baseCloudModel);
        return cloudModel;
    }

    @Override
    public CloudModel updateCloudModel(CloudModel cloudModel) throws AppException {
        log.info("编辑云主机型号");
        Assert.notNull(cloudModel);
        Assert.notNull(cloudModel.getModelName());
        
        int NameNum = cloudModelDao.getModelCountByCusAndNameID(cloudModel.getModelCusid() , cloudModel.getModelName() , cloudModel.getModelId());
        if(NameNum > 0){
            cloudModel.setErrorMessage("云主机类型名称重复");
            return cloudModel;
        }
        
        BaseCloudModel baseCloudModel = new BaseCloudModel();
        BeanUtils.copyPropertiesByModel(baseCloudModel, cloudModel);
        cloudModelDao.saveOrUpdate(baseCloudModel);
        BeanUtils.copyPropertiesByModel(cloudModel, baseCloudModel);
        return cloudModel;
    }

    @Override
    public void deleteCloudModel(String modelId) throws AppException {
        log.info("删除云主机型号");
        Assert.notNull(modelId);
        
        cloudModelDao.delete(modelId);
    }

    @Override
    public CloudModel findCloudModelById(String modelId) throws AppException {
        log.info("获取单个云主机型号信息");
        
        BaseCloudModel baseCloudModel = cloudModelDao.findOne(modelId);
        CloudModel cloudModel = new CloudModel();
        BeanUtils.copyPropertiesByModel(cloudModel, baseCloudModel);
        
        return cloudModel;
    }

    @Override
    public Page getModelListByPage(Page page , String modelName, String modelVcpus , 
                                   String modelRam , QueryMap queryMap) throws AppException {
        log.info("分页测试");
        List<Object> list = new ArrayList<Object>();
        StringBuffer hql = new StringBuffer("from BaseCloudModel where 1=1 ");  //组合条件查询
        if(null!=modelName && !modelName.trim().equals("")){
            hql.append(" and modelName like ? ");
            list.add("%"+modelName+"%");
        }
        if(null!=modelVcpus && !modelVcpus.trim().equals("")){
            int vcpus = Integer.parseInt(modelVcpus);
            hql.append(" and modelVcpus = ? ");
            list.add(vcpus);
        }
        if(null!=modelRam && !modelRam.trim().equals("")){
            int ram = Integer.parseInt(modelRam);
            hql.append(" and modelRam = ? ");
            list.add(ram);
        }
        page = cloudModelDao.pagedQuery(hql.toString(), queryMap, list.toArray());
        return page;
    }

    @Override
    public boolean checkCloudNumlByCus(String modelCusid) {
        log.info("校验客户云主机个数");
        int num = cloudModelDao.getModelCountByCustomer(modelCusid);
        boolean isAllowAdd = false;
        if(num >= 3){
            isAllowAdd = false;
        }else{
            isAllowAdd = true;
        }
        return isAllowAdd;
    }

    @Override
    public boolean checkNamelByCusAndName(String modelCusid, String modelName , String modelId) {
        log.info("校验云主机名称");
        int num;
        if(null == modelId || modelId.equals("")){
            num = cloudModelDao.getModelCountByCusAndName(modelCusid, modelName);
        }else{
            num = cloudModelDao.getModelCountByCusAndNameID(modelCusid, modelName, modelId);
        }
        boolean isUpdateName = false;
        if(num > 0){
            isUpdateName = false;
        }else{
            isUpdateName = true;
        }
        return isUpdateName;
    }
    
}

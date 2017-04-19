package com.eayun.syssetup.service;

import java.util.List;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.syssetup.model.CloudModel;
/**
 * 
 *                       
 * @Filename: CloudModelService.java
 * @Description: 业务接口配置Service接口
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2015年9月22日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public interface CloudModelService {
    /**
     * 列表查询该用户已创建的云主机列表，无条件
     * @param modelCusid
     * @return
     * @throws AppException
     */
    public List<CloudModel> getModelListByCustomer(String modelCusid) throws AppException;
    /**
     * 用户添加云主机型号记录
     * @param cloudModel
     * @return
     * @throws AppException
     */
    public CloudModel addCloudModel(CloudModel cloudModel) throws AppException;
    /**
     * 用户修改云主机型号记录
     * @param cloudModel
     * @return
     * @throws AppException
     */
    public CloudModel updateCloudModel(CloudModel cloudModel) throws AppException;
    /**
     * 用户删除云主机型号记录
     * @param modelId
     * @throws AppException
     */
    public void deleteCloudModel(String modelId) throws AppException;
    /**
     * 根据id返回一条云主机型号记录
     * @param modelId
     * @return
     * @throws AppException
     */
    public CloudModel findCloudModelById(String modelId) throws AppException;
    /**
     * 分页测试
     * @param page
     * @param queryMap
     * @return
     * @throws AppException
     */
    public Page getModelListByPage(Page page , String modelName, String modelVcpus , String modelRam , QueryMap queryMap) throws AppException;
    /**
     * 检查该用户已建云主机是否超过三台
     * @param modelCusid
     * @return
     */
    public boolean checkCloudNumlByCus(String modelCusid);
    /**
     * 检查该用户的填写的主机名是否重复
     * @param modelCusid
     * @param modelName
     * @param modelId
     * @return
     */
    public boolean checkNamelByCusAndName(String modelCusid , String modelName , String modelId);
    
}

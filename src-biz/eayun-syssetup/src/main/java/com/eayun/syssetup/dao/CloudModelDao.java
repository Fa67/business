package com.eayun.syssetup.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eayun.common.dao.IRepository;
import com.eayun.syssetup.model.BaseCloudModel;

/**                    
 * @Filename: CloudModelDao.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2015年9月22日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public interface CloudModelDao extends IRepository<BaseCloudModel, String>{

    @Query("from BaseCloudModel t where t.modelCusid =:modelCusid order by t.modelVcpus , t.modelRam")
    public List<BaseCloudModel> getModelListByCustomer(@Param("modelCusid") String modelCusid);
    
    @Query("select count(*) from BaseCloudModel t where t.modelCusid =:modelCusid")
    public int getModelCountByCustomer(@Param("modelCusid") String modelCusid);
    
    @Query("select count(*) from BaseCloudModel t where t.modelCusid = ? and t.modelName = ?")
    public int getModelCountByCusAndName(String modelCusid , String modelName);
    
    @Query("select count(*) from BaseCloudModel t where t.modelCusid = ? and t.modelName = ? and t.modelId <> ?")
    public int getModelCountByCusAndNameID(String modelCusid , String modelName ,String modelId);
}

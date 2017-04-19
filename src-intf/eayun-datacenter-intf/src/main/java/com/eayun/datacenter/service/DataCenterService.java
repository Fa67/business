package com.eayun.datacenter.service;

import java.util.List;

import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.datacenter.model.DcDataCenter;

/**
 * DataCenterService
 *                       
 * @Filename: DataCenterService.java
 * @Description: 
 * @Version: 1.0
 * @Author: chenhao
 * @Email: hao.chen@eayun.com
 * @History:<br>
 *<li>Date: 2015年9月1日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public interface DataCenterService {
    
    /**
     * 得到所有数据中心
     * 
     * @return
     */
    public List<DcDataCenter> getAllList();
    
    public BaseDcDataCenter getById(String dcId);
    
    public List<BaseDcDataCenter> getDcList(String sql,Object[] values);

}

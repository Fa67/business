package com.eayun.virtualization.apiservice.impl;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.annotation.ApiService;
import com.eayun.virtualization.apiservice.SubNetworkApiService;
import com.eayun.virtualization.baseservice.BaseSubNetworkService;
import com.eayun.virtualization.dao.CloudSubNetWorkDao;
import com.eayun.virtualization.model.BaseCloudSubNetWork;
/**
 * 
 * 子网api业务
 * @author gaoxiang
 * @date 2016-12-2
 *
 */
@ApiService
@Service
@Transactional
public class SubNetworkApiServiceImpl extends BaseSubNetworkService implements SubNetworkApiService {
    @Autowired
    private CloudSubNetWorkDao subNetDao;
    
    /**
     * 根据子网id获取子网对象
     * @author gaoxiang
     * @param subNetId
     * @return
     */
    public BaseCloudSubNetWork getSubNetworkById(String subNetId){
        return subNetDao.findOne(subNetId);
    }
    
    /**
     * 判断受管子网是否绑定路由。count > 0 :已绑定，count = 0 : 未绑定。
     * @author gaoxiang
     * @param subnetId
     * @return
     */
    public int getSubBindRouteCount(String subnetId){
        StringBuffer sqlBuf = new StringBuffer();
        sqlBuf.append(" select count(*) ");
        sqlBuf.append(" from cloud_subnetwork ");
        sqlBuf.append(" where subnet_type = '1'  ");
        sqlBuf.append(" and (route_id is not null or route_id != '') ");
        sqlBuf.append(" and subnet_id = ? ");
        Query query = subNetDao.createSQLNativeQuery(sqlBuf.toString(), subnetId);
        Object result = query.getSingleResult();
        int count = result == null ? 0 : Integer.parseInt(result.toString());
        return count;
    }
}

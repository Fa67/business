package com.eayun.unit.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.unit.model.BaseWebDataCenterIp;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2017年3月17日
 */
public interface WebDataCenterIpDao extends IRepository<BaseWebDataCenterIp, String>{

    @Query("from BaseWebDataCenterIp where ip = ? ")
    public List<BaseWebDataCenterIp> getByServiceIP(String ip);
    
    @Modifying
    @Query( "delete  from BaseWebDataCenterIp  where webId=?")
    public void deleteWebDataCenterIp(String webId);
    
}

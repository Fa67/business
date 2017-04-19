package com.eayun.ecmcapi.dao;

import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.ecmcapi.model.BaseApiBlackList;

public interface EcmcApiBlackListDao extends IRepository<BaseApiBlackList, String>{

	/**
     * 查询所有黑名单客户数量
     * @author liyanchao
     * @return
     */
    @Query("select count(*) from BaseApiBlackList t where t.apiType = 'blackCus'")
    public int getBlackCusCount();
    
	/**
     * 查询所有黑名单IP数量
     * @author liyanchao
     * @return
     */
    @Query("select count(*) from BaseApiBlackList t where t.apiType = 'blackIp'")
    public int getBlackIpCount();
    
    
	
}

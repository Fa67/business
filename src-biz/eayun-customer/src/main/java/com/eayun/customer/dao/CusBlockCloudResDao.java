package com.eayun.customer.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eayun.common.dao.IRepository;
import com.eayun.customer.model.CusBlockResource;

public interface CusBlockCloudResDao  extends IRepository<CusBlockResource, String> {
	/**
     * 查询是否有上次冻结失败的资源信息
     * @author liyanchao
     * @return
     */
	 @Query("from CusBlockResource c where updateTime=(select MAX(updateTime) from CusBlockResource) and c.cusId = ? and c.isBlocked = '1' and c.blockopStatus = '0' ")
	 public CusBlockResource getResourceByCusId(String cusId);
	  
	 /**
     * 查询最新冻结的资源信息
     * @author liyanchao
     * @return
     */
	 @Query("from CusBlockResource c where updateTime=(select MAX(updateTime) from CusBlockResource blockTime where blockTime.cusId = :cusId ) and c.cusId = :cusId and c.isBlocked = '1' and c.blockopStatus = '1' ")
	 public CusBlockResource getBlockedResourceByCusId(@Param("cusId") String cusId);
}

package com.eayun.virtualization.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eayun.common.dao.IRepository;
import com.eayun.virtualization.model.BaseCloudOrderSnapshot;

public interface CloudSnapshotOrderDao extends IRepository<BaseCloudOrderSnapshot, String> {
	
	@Query("from BaseCloudOrderSnapshot t where orderNo=:orderNo")
	public BaseCloudOrderSnapshot getSnapOrderByOrderNo(@Param("orderNo") String orderNo);

    
}

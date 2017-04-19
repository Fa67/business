package com.eayun.virtualization.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eayun.common.dao.IRepository;
import com.eayun.virtualization.model.BaseCloudOrderVolume;

public interface CloudVolumeOrderDao extends IRepository<BaseCloudOrderVolume, String> {
	
	@Query("from BaseCloudOrderVolume t where orderNo=:orderNo")
	public BaseCloudOrderVolume getVolOrderByOrderNo(@Param("orderNo") String orderNo);

    
}

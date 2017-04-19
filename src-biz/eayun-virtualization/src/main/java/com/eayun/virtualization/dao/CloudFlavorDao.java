package com.eayun.virtualization.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.virtualization.model.BaseCloudFlavor;

public interface CloudFlavorDao extends IRepository<BaseCloudFlavor, String> {
	
	@Query("from BaseCloudFlavor where flavorId=?")
	public List<BaseCloudFlavor> getflavorId(String id);
	
}

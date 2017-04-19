package com.eayun.virtualization.dao;

import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.common.exception.AppException;
import com.eayun.virtualization.model.BaseCloudFireWall;

public interface CloudFireWallDao extends IRepository<BaseCloudFireWall, String> {
	@Query("select count(*) from BaseCloudFireWall fw where fw.prjId = ?")
	int countFireWallByPrjId(String prjId);

	@Query("select count(*) from BaseCloudFireWall fw where fw.fwName = ? ")
	public int getCountFireWallByName(String name) throws AppException;
	
}

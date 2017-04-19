package com.eayun.unit.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.unit.model.BaseWebSiteInfo;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2016年12月21日
 */
public interface WebSiteInfoDao extends IRepository<BaseWebSiteInfo, String>{
	
	@Query("from BaseWebSiteInfo where unitId=?")
	public List<BaseWebSiteInfo> getByUnitId(String id);
	
	@Query("from BaseWebSiteInfo where serviceIp=?")
	public List<BaseWebSiteInfo> getByServiceIP(String ip);

}

package com.eayun.unit.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.unit.model.BaseUnitWeb;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2016年12月21日
 */
public interface UnitWebDao extends IRepository<BaseUnitWeb, String>{
	@Query("from BaseUnitWeb where applyId=?")
	public List<BaseUnitWeb> getWebIdByUnitid(String id);
	
	@Query("from BaseUnitWeb where applyId=? and webId=?")
	public BaseUnitWeb getolWeb(String id,String webid);

	@Query("from BaseUnitWeb where webId=?")
	public BaseUnitWeb getWeb(String webid);
	
}

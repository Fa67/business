package com.eayun.log.dao;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.log.model.OperLog;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2016年3月29日
 */
public interface EcmcLogDao extends IRepository<OperLog,String>{

	@Modifying
	@Query("delete from OperLog where operDate<'?'")
	public int deleteTiming(String date);
	
}

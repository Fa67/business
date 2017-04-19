package com.eayun.physical.ecmcservice;

import java.util.List;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.physical.ecmcvoe.DcSwitchVOE;
import com.eayun.physical.model.BaseDcSwitch;

public interface EcmcSwitchService {
	Page query(String name,String datacenterid,QueryMap queryMap)throws AppException;
	void delete(String switchId)throws AppException;
	void update(BaseDcSwitch model,String state)throws AppException;
	List<BaseDcSwitch> checkNameExist(String name,String dcid)throws AppException;
	List<BaseDcSwitch> checkNameExist(String name,String dcid,String id)throws AppException;
	
	void addswitch(BaseDcSwitch model,String user,String state)throws AppException;
	
	DcSwitchVOE queryById(String switchid)throws AppException;
	
	
	
	/**
	 * 2016-04-12
	 * 
	 * */
	
	int getcountswitch(String id)throws AppException;
}

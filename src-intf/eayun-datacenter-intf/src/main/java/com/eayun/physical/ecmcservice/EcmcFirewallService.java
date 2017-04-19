package com.eayun.physical.ecmcservice;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.physical.ecmcvoe.DcFirewallVOE;
import com.eayun.physical.model.BaseDcFirewall;

public interface EcmcFirewallService {
	public Page query( String name, String dcId,QueryMap queryMap)throws AppException;
	public DcFirewallVOE queryById( String id)throws AppException;
	public void updatefirewall(BaseDcFirewall model,String state)throws AppException;
	public void createfirewall(BaseDcFirewall model,String state,String userid)throws AppException;
	public void delete(String id)throws AppException;
	public boolean addcheckNameExist(String name,String id)throws AppException;
	public boolean updatecheckNameExist(String name,String id,String datacenterid)throws AppException;
	
	
	
	
	
	/**
	 * 2016-04-12
	 * */
	public int getcountfirewall(String id)throws AppException;

}

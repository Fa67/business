package com.eayun.physical.ecmcservice;

import java.util.List;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.physical.ecmcvoe.DcServerVOE;
import com.eayun.physical.model.BaseDcServer;
import com.eayun.physical.model.DcServerModel;

public interface EcmcServerService {
	Page queryserver(String dcid,String type,String anyName,QueryMap queryMap)throws AppException;
	List<BaseDcServer> querybyid(String dcid,String id,String name)throws AppException;
	List<DcServerModel> queryByServerModel()throws AppException;
	DcServerModel getByServerModel(String serverid)throws AppException;
	void saveServer(BaseDcServer dcserver,String user,String startlocation) throws AppException;

	void deleteserver(String id)throws AppException;
	void deletecabinetrf(String idstr)throws AppException;
	DcServerVOE getByDcServerId(String id) throws AppException;
	void update(BaseDcServer dcserver,String startlocation)throws AppException;
	
	
	
	/**
	 * 2016-04-12
	 * */
	
	int getcountserver(String id)throws AppException;
}

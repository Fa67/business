package com.eayun.physical.ecmcservice;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.physical.ecmcvoe.DcStorageVOE;
import com.eayun.physical.model.BaseDcStorage;

public interface EcmcStorageService {

	void queryName(String name,JSONObject object)throws AppException;
	Page querybystoragelist(String dcid,String name,QueryMap queryMap)throws AppException;
	DcStorageVOE  queryDcStorageById(String id)throws AppException;
	void queryDcStorageCreate(BaseDcStorage model,String state,String user)throws AppException;
	void queryDcStorageUpdate(BaseDcStorage model,String state)throws AppException;
	void queryDcStorageDel(String id)throws AppException;
	public void getDatacenter(JSONObject object)throws AppException;
	List<BaseDcStorage> checkNameExist(String name,String dcid)throws AppException;
	List<BaseDcStorage> checkNameExistOfEdit(String name,String id,String dcid)throws AppException;
	
	
	
	
	/**
	 * 2016-04-12
	 * 
	 * */
	int getcountstorage(String id)throws AppException;
}

package com.eayun.physical.ecmcservice;

import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.DcDataCenter;
import com.eayun.physical.model.BaseDcCabinet;
import com.eayun.physical.model.DcCabinet;



public interface EcmcCabinetService {
	public Page query(String cabinetName,String datacenterId,QueryMap queryMap)throws AppException ;
	
	public String delete(String cabinetId, String dataCenterId)throws AppException;
	public DcCabinet queryById(String cabinetId)throws AppException;
	public String update(String cabinetId,String dataCenterId,String cabinetName,String totalCapacity)throws AppException;
	public void add(BaseDcCabinet model)throws AppException ;
	public String [] add(String dcId, String cabinetName, String totalCapacity,
			String cabinetNum, String user)throws AppException ;
	
	
	@SuppressWarnings("rawtypes")
    public List getCabinet( String datacenterid,String equipmentId)throws AppException;
	//public void getCabinet(JSONObject object, String datacenterid)throws Exception;
	
	public Page queryEquById( String id, String dcId,Page page ,QueryMap queryMap )throws AppException;
	
	public int getCountByHql(String id)throws AppException;
	public int getCountByHql(String id,String cabinetid)throws AppException;
	
	public DcDataCenter getDateCenterById(String id) throws AppException;
	public JSONArray getstateByCabinet(String dataCenterId, String cabinetId,
			String spec, String id)throws AppException;
	
	public int getMaxUsedLocation(String id) throws AppException;
	
	public List<BaseDcCabinet> checkNameExist(List<String> name,String datacenterid) throws AppException;
	public List<BaseDcCabinet> checkNameExist(String name,String datacenterid,String id)throws AppException;
	

	
	
	
	/**
	 * 2016-04-12
	 * */
	
	public int getcountcabinet(String id) throws AppException;
}


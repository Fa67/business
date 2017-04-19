package com.eayun.unit.ecmcservice;

import java.util.List;

import com.eayun.bean.NewAccessExcel;
import com.eayun.bean.NewRecordExcel;
import com.eayun.bean.NewWebExcel;
import com.eayun.bean.UnitWebSVOE;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.unit.model.BaseUnitInfo;

public interface EcmcRecordService {
	
	

	public Page getecmcrecordlist(QueryMap queryMap, String recordType, String status, String dcid,String queryName)throws AppException;
	
	public Object getecmcrecordcount()throws AppException;
	public List updaterecord(String id,String status,String status1)throws AppException;
	public List updaterecord(String id)throws AppException;
	public void deletedrecord(String id)throws AppException;
	public UnitWebSVOE getbyid(String id)throws AppException;
	
	public void recordRe(UnitWebSVOE voe)throws AppException;
	
	public BaseUnitInfo updatedetail(BaseUnitInfo model) throws AppException;
	
	public boolean updatedetail(UnitWebSVOE model) throws AppException;
	
	public BaseUnitInfo getUnitInfo(String id) throws AppException;
	/**
	 * 获取新增备案的excel信息
	 * @param applyId
	 * @return
	 */
	public List<NewRecordExcel> getNewRecordExcel(String applyId);
	/**
	 * 获取新增网站的excel信息
	 * @param applyId
	 * @return
	 */
	public List<NewWebExcel> getNewWebExcel(String applyId);
	/**
	 * 获取新增接入的excel信息
	 * @param applyId
	 * @return
	 */
	public List<NewAccessExcel> getNewAccessExcel(String applyId);
	
}

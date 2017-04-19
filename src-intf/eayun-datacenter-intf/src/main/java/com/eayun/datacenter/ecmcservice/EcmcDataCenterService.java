package com.eayun.datacenter.ecmcservice;

import java.util.List;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.datacenter.model.DcDataCenter;


public interface EcmcDataCenterService {
	/**查询
	 * 
	 * @param cabinetName
	 * @param page
	 * @param dcId
	 * @param user
	 * @throws AppException
	 */
	public Page query(String dataCenterName,QueryMap map)throws AppException;
	
	
	/**删除
	 * 
	 * @param dataCenterId
	 * @throws AppException
	 */
	
	public void delete(String dataCenterId)throws AppException ;
	
	
	
	
	
	
	/**检查数据中心是否能删除
	 * 
	 * @param json
	 * @param dataCenterId
	 * @return
	 * @throws AppAppException
	 */
	public boolean checkDataCenterRemoveCannot(String dataCenterId)throws AppException;
	
	
	
	
	
	/**添加是检查数据中心是否可以链接
	 * 
	 * @param model
	 * @param user
	 * @param state
	 * @return
	 * @throws AppAppException
	 */
	public boolean  checkDataCenterLinked(BaseDcDataCenter model)throws AppException;
	
	public boolean checkDataCenterLinkeddcid(BaseDcDataCenter model) throws AppException;
	
	
	/**
	 * 检查名称是否可用
	 * */
	
	public boolean checkNameExist(String name)throws AppException;
	
	
	/**
	 * 修改时检查名称是否存在
	 * 
	 * */
	
	public boolean checkNameExist(String name,String id)throws AppException;

	/**
	 * 修改前查询
	 * */
	DcDataCenter querybyid(String id) throws AppException;
	
	
	/**
	 * 查询数据中心机柜数
	 * */
	
	public int queryDatacentercabinetNum(String id)throws AppException;
	
	//public DcDataCenter querydatacenterByIdOrName(String id,String name) throws AppException;
	
	@SuppressWarnings("rawtypes")
    public List queryip(String ip)throws AppException;


	void add(BaseDcDataCenter model, String user) throws AppException;


	void update(BaseDcDataCenter model) throws AppException;
	
	List<DcDataCenter> getAllList() throws AppException;
	DcDataCenter getdatacenterbyid(String id)throws AppException;

	BaseDcDataCenter getdatacenterbyname(String name)throws AppException;
	
	
	String getdatacenterName(String id)throws AppException;
	BaseDcDataCenter querysyndatacenterbyid(String dcid) throws AppException;
	/**
	 * 修改数据中心状态
	 * @param operation
	 * @param dcId
	 * @return
	 * @throws AppException
	 */
	public int operationApiSwitchById(String operation, String dcId) throws AppException;

	/**
	 * 校验数据中心API代号
	 * @param apiDcCode
	 * @param dcId
	 * @return
	 */
	public boolean checkApiDcCode(String apiDcCode, String dcId);
	/**
	 * 获取数据中心分布地点
	 * @param id
	 * @return
	 */
	public String getProvinces(String id);
}

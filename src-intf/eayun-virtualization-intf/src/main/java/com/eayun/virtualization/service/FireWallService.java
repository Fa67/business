package com.eayun.virtualization.service;

import java.util.List;
import java.util.Map;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.virtualization.model.BaseCloudFireWall;
import com.eayun.virtualization.model.CloudFireWall;


public interface FireWallService {

	public Page getFireWallList(Page page, String prjId, String dcId, String fireName,QueryMap queryMap)throws Exception;

	public BaseCloudFireWall addFireWall(String dcId, String prjId,String createName, String fireWallName, String policyId)throws AppException;

	public boolean deleteFireWall(CloudFireWall fw)throws AppException;

	@SuppressWarnings("rawtypes")
    public boolean getFireWallByName(Map map)throws AppException;

	public boolean updateFireWall(CloudFireWall fw)throws AppException;
	
	public int countFireWallByPrjId(String prjId)throws  AppException;

	public boolean updateFw(CloudFireWall cloudFw) throws Exception ;
	
	public List<BaseCloudFireWall> queryCloudFirewallListByDcId (String dcId);
	
	public boolean updateCloudFirewallFromStack(BaseCloudFireWall fw);

	/**
	 * 创建防火墙，同时创建策略和规则
	 * @param map
	 * @return
	 * @throws AppException
	 */
	public BaseCloudFireWall createFwAndFwpAndFwR(Map<String, Object> map,String createName) throws AppException;
	
	/**
	 * 删除防火墙及策略和规则
	 * @param map
	 * @return
	 * @throws AppException
	 */
	public boolean deleteFwAndFwpAndFwr(Map<String, String> map) throws AppException;

	/**
	 * 根据防火墙ID 获取防火墙
	 * @param fwId
	 * @return
	 * @throws AppException
	 */
	public BaseCloudFireWall getFwById(String fwId) throws AppException;
	
	/**
	 * 修改防火墙名称或者描述
	 * @param type  {name,desc}
	 * @param value
	 * @return
	 * @throws AppException
	 */
	public boolean updateFwNameorDesc(CloudFireWall fw) throws AppException;
	
}

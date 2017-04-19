package com.eayun.virtualization.ecmcservice;

import java.util.List;
import java.util.Map;

import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.virtualization.ecmcvo.CloudLdpoolVoe;
import com.eayun.virtualization.model.BaseCloudLdPool;
import com.eayun.virtualization.model.CloudLdPool;

public interface EcmcLBPoolService {

	/**
	 * 查询资源池
	 * @param paramsMap
	 * @return
	 * @throws AppException
	 */
	Page queryPool(ParamsMap paramsMap) throws AppException;
	
	CloudLdpoolVoe getById(String poolId) throws AppException;
	
	public boolean checkPoolName(String prjId, String poolName, String poolId);
	
	CloudLdpoolVoe createPool(BaseCloudLdPool pool) throws AppException;
	
	boolean bindHealthMonitor(String poolId, String healthMonitorId);
	
	CloudLdpoolVoe update(CloudLdPool pool);
	
	boolean delete(String poolId) throws AppException;
	
	/**
	 * 根据项目查询资源池使用情况
	 * @author zengbo
	 * @param prjId
	 * @return
	 */
	public int getCountByPrjId(String prjId);
	/**
	 * 绑定浮动IP
	 * @param poolId
	 * @param floatId
	 * @return
	 * @throws AppException
	 */
	public boolean bindFloatIp(String poolId, String floatId, String vipId) throws AppException;
	/**
	 * 解绑浮动IP
	 * @param poolId
	 * @param floatId
	 * @return
	 * @throws AppException
	 */
	public boolean unbindFloatIp(String poolId, String floatId) throws AppException;
	/**
	 * 查询未绑定floatIP的资源池
	 * @param subnetId
	 * @return
	 * @throws AppException
	 */
	public List<Map<String, Object>> getNotbindFloatIpPools(String subnetId) throws AppException;
	/**
	 * 查询数据中心/项目下的资源池
	 * @param dcId
	 * @param prjId
	 * @return
	 * @throws AppException
	 */
	public List<CloudLdPool> getPoolList(String dcId, String prjId) throws AppException;

	public  Page getPoolList(String dcId, String poolName, String cusOrg, String prjName, QueryMap queryMap)  throws Exception;

	public CloudLdPool createBalancer(CloudLdPool pool) throws Exception;

	public CloudLdPool updateBalancer(CloudLdPool pool) throws Exception;

	public boolean deleteBalancer(CloudLdPool pool) throws AppException;

    public CloudLdPool getLoadBalanceById(String poolId) throws AppException;
    
    /**
     * 获取所有可见状态下的负载均衡(所有数据中心,所有项目)
     * @return
     * @throws Exception
     */
    public List<CloudLdPool> getAllPoolList() throws Exception;
    /**
     * 清除负载均衡(成员状态计划任务)
     * @param cloudLdPool
     * @throws Exception
     */
    public void deletePool(CloudLdPool cloudLdPool) throws Exception;
}

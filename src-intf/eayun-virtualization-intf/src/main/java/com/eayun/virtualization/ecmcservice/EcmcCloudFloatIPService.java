package com.eayun.virtualization.ecmcservice;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.virtualization.model.*;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2016年4月6日
 */
public interface EcmcCloudFloatIPService {

	/**
	 * 获取IP集合
	 * @param floIpmin
	 * @param floIpmax
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getIPList(String floIpmin,String floIpmax) throws Exception;
	/**
	 * 获取IP关联对象（云主机）
	 * @param prjId
	 * @return
	 * @throws Exception
	 */
	public List<BaseCloudVm> findFloatIpOne(String prjId) throws Exception;
	/**
	 * 浮动IP列表
	 * @param name
	 * @param datacenterId
	 * @param projectId
	 * @return
	 * @throws Exception
	 */
	public Page getFloatlist(String name, String datacenterId, String projectId,Page page,QueryMap queryMap,String ip,String[] pns,String[] cns)throws Exception;
	
	/**
	 * 根据ID获取单个浮动IP
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public BaseCloudFloatIp getById(String id) throws Exception;
	
	/**
	 * 分配浮动IP 
	 * @param datacenterId
	 * @param projectId
	 * @param request
	 * @param pool
	 * @return
	 * @throws Exception
	 */
	public CloudFloatIp allocateIp(String datacenterId,String projectId) throws Exception ;
	
	/**
	 * 根据项目查询浮动IP使用量
	 * @author zengbo
	 * @param prjId
	 * @return
	 */
	public int findBindCountByPriId(String prjId) throws AppException;
	public int getCountByPrjId(String prjId) throws AppException;
	
	/**
	 * 绑定负载均衡
	 * @param datacenterId
	 * @param projectId
	 * @param portId
	 * @param floatId
	 * @throws AppException
	 */
	public CloudFloatIp binLb(CloudFloatIp cloudFloatIp) throws AppException;
	/**
	 * 释放
	 * @param datacenterId
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public boolean deallocateFloatIp(String datacenterId, String projectId,String id) throws AppException;
	/**
	 * 查询未绑定浮动ip的云主机列表
	 * @param subnetworkId
	 * @return
	 * @throws AppException
	 */
	public List<BaseCloudVm> getVmBySubNetWork(String subnetworkId) throws AppException;
	/**
	 * 项目下浮动IP个数
	 * @param prjId
	 * @return
	 * @throws AppException
	 */
	public int getCountByPro(String prjId) throws AppException;
	/**
	 * 绑定云主机
	 * @param cloudFloatIp
	 * @return
	 * @throws AppException
	 */
	public CloudFloatIp binDingVmIp(CloudFloatIp cloudFloatIp) throws AppException;
	/**
	 * 解绑云主机
	 * @param cloudFloatIp
	 * @return
	 * @throws AppException
	 */
	public CloudFloatIp unBinDingVmIp(CloudFloatIp cloudFloatIp) throws AppException;
	/**
	 * 查询项目下未绑定云主机的浮动 IP列表
	 * @param prjId
	 * @return
	 * @throws AppException
	 */
	public List<BaseCloudFloatIp> getUnBindFloatIp(String prjId) throws AppException;

	public BaseCloudFloatIp getFloatIpByResourceId(String resourceId,
			String resourceType);

	public List<BaseCloudNetwork> getNetworkByPrj(String prjId) throws Exception;

	public List<BaseCloudSubNetWork> getSubnetByNetId(String netId) throws Exception;

	public CloudFloatIp bindResource(CloudFloatIp cloudFloatIp) throws Exception;

	public CloudFloatIp unbundingResource(CloudFloatIp cloudFloatIp) throws Exception;

    /**
     * 解除已删除云主机与弹性公网IP的关系
     * @param vmId  云主机ID
     */
	public void refreshFloatIpByVm(String vmId);
	
	public boolean checkFloWebSite(String floIp) throws Exception;
	
	/**
	 * 查询弹性公网IP的资源使用情况，包含：
	 * 1.弹性公网ip保有量最多的客户名称及数量
	 * 2.已分配/总数/未分配数量
	 * @Author: duanbinbin
	 * @return
	 * @throws Exception
	 *<li>Date: 2017年4月17日</li>
	 */
	public JSONObject getFloatIpUsedSituation() throws Exception;
}

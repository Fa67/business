package com.eayun.eayunstack.service;

import java.util.List;
import java.util.Map;

import javax.sound.sampled.Port;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.BandWidth;
import com.eayun.eayunstack.model.Route;

public interface OpenstackRouterService extends OpenstackBaseService<Route> {
	/**
	 * 获取指定数据中心的指定项目下的路由子网络列表
	 */
	public List<Port> listport(String datacenterId, String routerid)
			throws AppException;
	/**
	 * 修改路由
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 *            json字符串，包含待修改网络的具体配置信息
	 * @return
	 * @throws AppException
	 */
	public Route update(String datacenterId, String projectId,
			JSONObject netrouteObject, String id) throws AppException;
	/**
	 * 设置网关
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 *            json字符串，包含待修改网络的具体配置信息
	 * @return
	 * @throws AppException
	 */
	public Route setGateway(String datacenterId, String id, String networkid)
			throws AppException;
	
	/**
	 * 清除网关
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 *            json字符串，包含待修改网络的具体配置信息
	 * @return
	 * @throws AppException
	 */
	public Route removeGateway(String datacenterId, String id)
			throws AppException;
	
	/**
	 * 连接内部网络
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 *            json字符串，包含待修改网络的具体配置信息
	 * @return
	 * @throws AppException
	 */
	public Route attachInterface(String datacenterId, String id,
			String subnetworkid) throws AppException;
	
	/**
	 * 解绑内部子网络
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 *            json字符串，包含待修改网络的具体配置信息
	 * @return
	 * @throws AppException
	 */
	public Route detachInterface(String datacenterId, String id,
			String subnetworkid) throws AppException;
	/**
	 * 删除指定id的路由
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public boolean delete(String datacenterId, String projectId, String id)
			throws AppException;
	
	/**                                                        
	 * 查询底层 数据中心的云资源                             
	 * -------------------                                   
	 * @author zhouhaitao                                    
	 * @param dataCenter                                     
	 * @return                                               
	 */                                                      
	@SuppressWarnings("rawtypes")
	public Map<String,List> getStackList (BaseDcDataCenter dataCenter);
/**************************************************带宽设置开始****************************************************/
	/**
	 * 创建路由带宽
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 * json字符串，具体配置信息
	 * @return  BandWidth
	 * @throws AppException
	 */
	public BandWidth createBandWidth(String datacenterId, String projectId, JSONObject data) throws AppException;
	/**
	 * 删除指定id路由的带宽
	 * 
	 * @param datacenterId
	 * @param qosId
	 * @return
	 * @throws AppException
	 */
	public boolean deleteQos(String datacenterId, String qosId) throws AppException;
	/**
	 * 修改带宽
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 * json字符串，包含待修改网络的具体配置信息
	 * @return
	 * @throws AppException
	 */
	public BandWidth updateBandWidth(String datacenterId, JSONObject bandWidthObject, String id)throws AppException;
}

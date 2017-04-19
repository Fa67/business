package com.eayun.virtualization.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestBody;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.virtualization.model.BaseCloudNetwork;
import com.eayun.virtualization.model.BaseCloudRoute;
import com.eayun.virtualization.model.BaseCloudSubNetWork;
import com.eayun.virtualization.model.CloudRoute;

/**
 * RouteService
 * 
 * @Filename: RouteService.java
 * @Description:
 * @Version: 1.0
 * @Author: liyanchao
 * @Email: yanchao.li@eayun.com
 * @History:<br> <li>Date: 2015年11月4日</li> <li>Version: 1.0</li> <li>Content:
 *               create</li>
 */
public interface RouteService {

	/**
	 * 获取路由的列表信息，用于在前端页面展示，从数据库中保存的信息
	 * 
	 * @param datacenterId
	 *            数据中心id
	 * @param projectId
	 *            项目id
	 * @param name
	 *            前端页面输入的查询内容，用于过滤不匹配的记录
	 * @return
	 */
	public Page getRouteList(Page page, String datacenterId, String projectId,
			String name, QueryMap queryMap);

	@SuppressWarnings("rawtypes")
    public BaseCloudRoute addRoute(HttpServletRequest request,
			@RequestBody Map map) throws Exception;

	/**
	 * 查询DB中外部网络的列表信息
	 * 
	 * @param datacenterId
	 *            数据中心id
	 * @return
	 */
	public List<BaseCloudNetwork> getOutNetList(String datacenterId);

	// 用于判断重名--创建、编辑时
	public boolean getRouteByIdOrName(String dcId, String sgId, String sgName);

	/**
	 * 设置网关
	 * 
	 * @param datacenterId
	 * @param routeid
	 * @param networkid
	 */
	public CloudRoute setGateWay(String routeid, String datacenterId,
			String networkid);

	/**
	 * 清除网关
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param id
	 */
	public BaseCloudRoute deleteGateway(String routeid, String datacenterId);

	/**
	 * 修改路由
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param id
	 */
	public BaseCloudRoute editRoute(String dcId,String routeId, String routeName,@RequestBody Map<String,String> map);

	// 用于判断重名--编辑时
	public boolean getRouteById(String dcId, String routeId, String routeName);
	/**
	 * 路由连接子网
	 * @param dcId
	 * @param routeId
	 * @param subnetworkid
	 */
	public BaseCloudSubNetWork connectSubnet(String dcId,String routeId,String subnetworkId);
	/*根据数据中心、项目、路由Id获取实体*/
	public CloudRoute getRouteDetail(String dcId, String prjId, String routeId);
	/**
	 * 路由解绑子网   
	 * @param datacenterId
	 * @param projectId
	 * @param id
	 */
	public BaseCloudSubNetWork detachSubnet(String dcId,String routeId,String subnetworkId);
	/**
	 * 删除指定id的路由，包括两步删除操作，
	 * @param datacenterId
	 * @param projectId
	 * @param id
	 * @return
	 */
	public boolean delete(String datacenterId,String routeId,String qosId);
	/*
	 *根据prjId查询个数 
	 */
	public int getCountByPrjId(String prjId);
	/**
     * 创建页面中查询当前项目下已有路由所有的带宽
     * @author liyanchao
     * @param request
     * @param map
     * @return
     */
	public int getHaveBandCount(String prjId,String routeId);
	/**
     * 创建页面中查询当前项目设置的路由带宽的配额
     * @author liyanchao
     * @param request
     * @param map
     * @return
     */
	public int getPrjBandCount(String prjId);
	/*
	 *根据prjId查询已用带宽数 
	 */
	public int getQosNumByPrjId(String prjId);

	/**
	 * 判断网关是否允许移除
	 * @author liuzhuangzhuang
	 * @param netId
	 * @return
	 */
	public EayunResponseJson checkForCle(String netId);

	/**
	 * 判断子网是否能够解绑路由
	 * @author liuzhuangzhuang
	 * @param subnetId
	 * @return
	 */
	public EayunResponseJson checkDetachSubnet(String subnetId);
}

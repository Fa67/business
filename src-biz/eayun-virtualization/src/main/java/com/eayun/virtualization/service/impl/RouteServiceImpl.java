package com.eayun.virtualization.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.tools.DictUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.database.instance.service.RDSInstanceService;
import com.eayun.eayunstack.model.Route;
import com.eayun.eayunstack.service.OpenstackRouterService;
import com.eayun.project.service.ProjectService;
import com.eayun.virtualization.dao.CloudRouteDao;
import com.eayun.virtualization.dao.CloudVpnDao;
import com.eayun.virtualization.dao.PortMappingDao;
import com.eayun.virtualization.model.BaseCloudNetwork;
import com.eayun.virtualization.model.BaseCloudRoute;
import com.eayun.virtualization.model.BaseCloudSubNetWork;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.model.CloudRoute;
import com.eayun.virtualization.service.EayunQosService;
import com.eayun.virtualization.service.NetWorkService;
import com.eayun.virtualization.service.RouteService;
import com.eayun.virtualization.service.SubNetWorkService;
import com.eayun.virtualization.service.TagService;

/**
 * RouteServiceImpl
 * 
 * @Filename: RouteServiceImpl.java
 * @Description:
 * @Version: 1.0
 * @Author: liyanchao
 * @Email: yanchao.li@eayun.com
 * @History:<br> <li>Date: 2015年11月4日</li> <li>Version: 1.0</li> <li>Content:
 *               create</li>
 * 
 */
@Service
@Transactional
public class RouteServiceImpl implements RouteService {

	@Autowired
	private CloudRouteDao routeDao;
	@Autowired
	private OpenstackRouterService openstackService;
	@Autowired
	private SubNetWorkService subNetWorkService;
	@Autowired
	private NetWorkService netWorkService;
	@Autowired
	private TagService tagService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private EayunQosService eayunQosService;
	@Autowired
    private PortMappingDao  portMappingDao;
	@Autowired
    private CloudVpnDao     vpnDao;
	@Autowired
	private RDSInstanceService instanceService;
	
	/*
	 *根据prjId查询个数 
	 */
	public int getCountByPrjId(String prjId){
		int routeCount = routeDao.getCountByPrjId(prjId);
        int orderCount = getRouterCountInOrder(prjId);
        return routeCount + orderCount;
	}
	/**
     * 获取订单状态为待创建或者创建中的资源的个数
     * @author gaoxiang
     * @param prjId
     * @return
     */
	private int getRouterCountInOrder(String prjId) {
	    StringBuffer sql = new StringBuffer();
        sql.append("select ");
        sql.append("   count(*) ");
        sql.append("from ");
        sql.append("   order_info ");
        sql.append("left join ");
        sql.append("   cloudorder_network network ");
        sql.append("on ");
        sql.append("   order_info.order_no = network.order_no ");
        sql.append("where ");
        sql.append("   order_info.order_type = 0 ");
        sql.append("   and order_info.resource_type = 3 ");
        sql.append("   and (order_info.order_state = 1 or order_info.order_state = 2)");
        sql.append("   and network.prj_id = ?");
        Query query = routeDao.createSQLNativeQuery(sql.toString(), prjId);
        Object result = query.getSingleResult();
        int orderCount = result == null ? 0 : Integer.parseInt(result.toString());
        return orderCount;
	}
	/*
	 *根据prjId查询已用带宽数 
	 */
	public int getQosNumByPrjId(String prjId){
		String cun =routeDao.getQosNumByPrjId(prjId);
		int created = 0;
		if(cun!=null){
			created = Integer.valueOf(cun);
		}
		int toBeCreated = getQosNumInOrder(prjId) + getQosNumUpgradedInOrder(prjId);
		return created + toBeCreated;
	}
	/**
	 * 获取订单状态为待支付或者创建中的资源的带宽总数
	 * @author gaoxiang
	 * @param prjId
	 * @return
	 */
	private int getQosNumInOrder(String prjId) {
	    StringBuffer sql = new StringBuffer();
        sql.append("select ");
        sql.append("   sum(network.rate) ");
        sql.append("from ");
        sql.append("   order_info ");
        sql.append("left join ");
        sql.append("   cloudorder_network network ");
        sql.append("on ");
        sql.append("   order_info.order_no = network.order_no ");
        sql.append("where ");
        sql.append("   order_info.order_type = 0 ");
        sql.append("   and order_info.resource_type = 3 ");
        sql.append("   and (order_info.order_state = 1 or order_info.order_state = 2)");
        sql.append("   and network.prj_id = ?");
        Query query = routeDao.createSQLNativeQuery(sql.toString(), prjId);
        Object result = query.getSingleResult();
        int orderCount = result == null ? 0 : Integer.parseInt(result.toString());
        return orderCount;
	}
	/**
	 * 获取订单状态为待支付或者升级中的资源的带宽总数
	 * @author gaoxiang
	 * @param prjId
	 * @return
	 */
	private int getQosNumUpgradedInOrder(String prjId) {
	    StringBuffer sql = new StringBuffer();
        sql.append("select ");
        sql.append("   sum(network.rate - router.rate) ");
        sql.append("from ");
        sql.append("   order_info ");
        sql.append("left join ");
        sql.append("   cloudorder_network network ");
        sql.append("on ");
        sql.append("   order_info.order_no = network.order_no ");
        sql.append("left join ");
        sql.append("   cloud_route router ");
        sql.append("on ");
        sql.append("   network.net_id = router.network_id ");
        sql.append("where ");
        sql.append("   order_info.order_type = 2 ");
        sql.append("   and order_info.resource_type = 3 ");
        sql.append("   and (order_info.order_state = 1 or order_info.order_state = 2)");
        sql.append("   and network.prj_id = ?");
        Query query = routeDao.createSQLNativeQuery(sql.toString(), prjId);
        Object result = query.getSingleResult();
        int orderCount = result == null ? 0 : Integer.parseInt(result.toString());
        return orderCount;
	}
	// 用于判断重名--编辑时
	public boolean getRouteById(String dcId, String routeId, String routeName) {
		boolean isExist = false;
		StringBuffer sql = new StringBuffer();
		List<Object> list = new ArrayList<Object>();

		sql.append("select route.* from cloud_route  as route  where 1=1 ");
		// 数据中心
		if (!"".equals(dcId) && dcId != null && !"undefined".equals(dcId)
				&& !"null".equals(dcId)) {
			sql.append("and route.dc_id = ? ");
			list.add(dcId);
		}
		// routeID
		if (!"null".equals(routeId) && null != routeId && !"".equals(routeId)
				&& !"undefined".equals(routeId)) {
			sql.append(" and route.route_id<> ? ");
			list.add(routeId);
		}

		// route名称
		if (!"".equals(routeName) && routeName != null
				&& !"undefined".equals(routeName) && !"null".equals(routeName)) {
			sql.append("and binary route.route_name = ? ");
			list.add(routeName);
		}
		javax.persistence.Query query = routeDao.createSQLNativeQuery(
				sql.toString(), list.toArray());
		List listResult = query.getResultList();
		if (listResult.size() > 0) {
			isExist = true;// 返回true 代表存在此名称
		}
		return isExist;

	}
	/**
     * 创建页面中查询当前项目下已有路由所有的带宽
     * @author liyanchao
     * @param request
     * @param map
     * @return
     */
	public int getHaveBandCount(String prjId,String routeId){
		StringBuffer sql = new StringBuffer();
		sql.append("select sum(r.rate) from cloud_route as r where 1 = 1");
		List<Object> listParams = new ArrayList<Object>();
		if (!"null".equals(prjId) && null != prjId&& !"".equals(prjId)&& !"undefined".equals(prjId)) {
			sql.append(" and r.prj_id = ? ");
			listParams.add(prjId);
		}
		if (!"null".equals(routeId) && null != routeId && !"".equals(routeId)
				&& !"undefined".equals(routeId)) {
			sql.append(" and r.route_id <> ? ");
			listParams.add(routeId);
		}
		javax.persistence.Query queryCount = routeDao.createSQLNativeQuery(sql.toString(), listParams.toArray());
		List listResult = queryCount.getResultList();
		if(null==listResult.get(0)){
			return 0;
		}
		int objs = Integer.parseInt(listResult.get(0)+"");
		return objs;
	}
	/**
     * 创建页面中查询当前项目设置的路由带宽的配额
     * @author liyanchao
     * @param request
     * @param map
     * @return
     */
	public int getPrjBandCount(String prjId){
//		int countRate = 0;
		 List<CloudProject> list =  projectService.getListByPrj(prjId);
		 int countRate = list.get(0).getCountBand();
		 
		 return countRate;
	}
	/**
	 * 查询DB中外部网络的列表信息
	 * 
	 * @param datacenterId
	 *            数据中心id
	 * @return
	 */
	public List<BaseCloudNetwork> getOutNetList(String datacenterId) {

		/* 以下只查询网络列表 */
		StringBuffer sql = new StringBuffer();
		sql.append("from BaseCloudNetwork where routerExternal=1");
		List<Object> listParams = new ArrayList<Object>();
		if (!"null".equals(datacenterId) && null != datacenterId
				&& !"".equals(datacenterId)
				&& !"undefined".equals(datacenterId)) {
			sql.append(" and dcId= ? ");
			listParams.add(datacenterId);

		}
		List<BaseCloudNetwork> list = netWorkService.getCloudNetworkList(sql.toString(),listParams.toArray());

		return list;

	}

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
			String name, QueryMap queryMap) {
		try {
			List<Object> listParams = new ArrayList<Object>();
			StringBuffer sql = new StringBuffer();
			sql.append("select cr.route_name as routeName,count(cs.subnet_id) as subnetCount,cr.route_status as routeStatus,cr.net_id as netId,cp.prj_name as prjName,");
			sql.append(" cr.route_id as routeId,cr.prj_id as prjId,cr.dc_id as dcId,dc.dc_name as dcName,cr.create_time as createTime ");
			sql.append(" ,cn.net_name AS netName, cr.qos_id as qosId, cr.rate as rate,cp.count_band as countBand ");

			sql.append(" from cloud_route cr");
			sql.append(" left join dc_datacenter dc on cr.dc_id=dc.id");
			sql.append(" left join cloud_project cp on cr.prj_id=cp.prj_id");
			sql.append(" left join cloud_network cn on cr.net_id=cn.net_id");// 设置是否外部网络
			sql.append(" left join cloud_subnetwork cs on cs.route_id=cr.route_id");
			sql.append(" where 1=1");
			if (!"null".equals(projectId) && null != projectId
					&& !"".equals(projectId) && !"undefined".equals(projectId)) {
				sql.append(" and cr.prj_id= ? ");
				listParams.add(projectId);
			}
			if (!"null".equals(datacenterId) && null != datacenterId
					&& !"".equals(datacenterId)
					&& !"undefined".equals(datacenterId)) {
				sql.append(" and cr.dc_id= ? ");
				listParams.add(datacenterId);
			}

			if (!"null".equals(name) && null != name && !"".equals(name)
					&& !"undefined".equals(name)) {
				name = name.replaceAll("\\_", "\\\\_");
				sql.append(" and binary cr.route_name like ? ");
				listParams.add("%" + name + "%");

			}

			sql.append(" group by cr.route_id order by if ( isnull(cr.create_time), 1, 0 ), cr.create_time desc ");
			page = routeDao.pagedNativeQuery(sql.toString(), queryMap,
					listParams.toArray());
			List listResult = (List) page.getResult();
			for (int i = 0; i < listResult.size(); i++) {
				Object[] objs = (Object[]) listResult.get(i);
				CloudRoute route = new CloudRoute();
				route.setRouteName(String.valueOf(objs[0]));
				route.setConnectsubnetnum(String.valueOf(objs[1]));
				route.setRouteStatus(String.valueOf(objs[2]));
				route.setStatusForRoute(DictUtil.getStatusByNodeEn("route", String.valueOf(objs[2])));
				route.setNetId(String.valueOf(objs[3]));
				route.setPrjName(String.valueOf(objs[4]));
				route.setRouteId(String.valueOf(objs[5]));
				route.setPrjId(String.valueOf(objs[6]));
				route.setDcId(String.valueOf(objs[7]));
				route.setDcName(String.valueOf(objs[8]));
				route.setCreateTime(DateUtil.stringToDate(String
						.valueOf(objs[9]) == "null" ? "" : String
						.valueOf(objs[9])));
				route.setNetName(String.valueOf(objs[10]) == "null" ? ""
						: String.valueOf(objs[10]));
				route.setQosId(String.valueOf(objs[11])=="null" ? "" : String.valueOf(objs[11]));
				String rate = String.valueOf(objs[12]);
				if(null != rate && !"null".equals(rate)){
					route.setRate(Integer.parseInt(rate));
				}
				route.setBandCount(String.valueOf(objs[13]));
				route.setIsDeleting("not");
				listResult.set(i, route);
			}

			return page;

		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 创建路由
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param id
	 */
	public BaseCloudRoute addRoute(HttpServletRequest request, @RequestBody Map map) throws Exception {
		BaseCloudRoute baseRoute = new BaseCloudRoute();
		Map projectMap = (Map) map.get("project");
		String dcId = projectMap.get("dcId").toString();
		String prjId = projectMap.get("projectId").toString();
		
		// 网络数据
		String name = map.get("name").toString();
		JSONObject net = new JSONObject();
		net.put("name", name);
		//创建页面增加设置网关选项
		/*String outNetId = map.get("outNetId").toString();
		  if (null != outNetId && !"".equals(outNetId) && !"空".equals(outNetId)) {
			JSONObject netId = new JSONObject();
			netId.put("network_id", outNetId);
			net.put("external_gateway_info", netId);
		}*/
		JSONObject resultData = new JSONObject();
		resultData.put("router", net);
		
		try {
			Route route = openstackService.create(dcId, prjId, resultData);
			String rate = map.get("rate").toString();
			// openstack平台创建成功后，新建CloudRoute实例对象，并保持到数据库
			if ( null!= route) {
				baseRoute.setRouteId(route.getId());
				baseRoute.setRouteName(route.getName());
				baseRoute.setRouteStatus(route.getStatus());
				// 从session中获取当前用户名
				String userName = String.valueOf(map.get("userName"));
				if(StringUtil.isEmpty(userName)){
					SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
					userName = sessionUser.getUserName();
				}
				baseRoute.setCreateName(userName);
				baseRoute.setPrjId(route.getTenant_id());
				baseRoute.setDcId(dcId);
				baseRoute.setCreateTime(new Date());
				/************************开始设置路由带宽*****************************/
				baseRoute.setRate(Integer.parseInt(rate));
				/*if (null != outNetId && !"".equals(outNetId)
						&& !"空".equals(outNetId)) {
					baseRoute.setNetId(outNetId);
				}*/
				routeDao.save(baseRoute);

			}
			
			
			return baseRoute;
		} catch (Exception e) {
			throw e;
		}
	}

	// 用于判断重名--创建、编辑时
	public boolean getRouteByIdOrName(String dcId, String sgId, String routeName) {
		boolean isExist = false;
		StringBuffer sql = new StringBuffer();
		List<Object> list = new ArrayList<Object>();

		sql.append("select route.* from cloud_route  as route  where 1=1 ");
		// 数据中心
		if (!"".equals(dcId) && dcId != null && !"undefined".equals(dcId)
				&& !"null".equals(dcId)) {
			sql.append("and route.dc_id = ? ");
			list.add(dcId);
		}
		// 路由ID
		if (!"null".equals(sgId) && null != sgId && !"".equals(sgId)
				&& !"undefined".equals(sgId)) {
			sql.append(" and route.route_id<> ? ");
			list.add(sgId);
		}

		// 路由名称
		if (!"".equals(routeName) && routeName != null && !"undefined".equals(routeName) && !"null".equals(routeName)) {
			sql.append("and binary route.route_name = ? ");
			list.add(routeName);
		}
		javax.persistence.Query query = routeDao.createSQLNativeQuery(
				sql.toString(), list.toArray());
		List listResult = query.getResultList();
		if (listResult.size() > 0) {
			isExist = true;// 返回true 代表存在此名称
		}
		return isExist;

	}

	/**
	 * 修改路由
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param id
	 */
	public BaseCloudRoute editRoute(String dcId,String routeId, String routeName,@RequestBody Map<String,String> map) {
		BaseCloudRoute baseRoute = new BaseCloudRoute();
		JSONObject net = new JSONObject();
		net.put("name", routeName);
		// 用于提交的完整数据
		JSONObject resultData = new JSONObject();
		resultData.put("router", net);
		// 创建网络 
		Route route = openstackService.update(dcId, null, resultData, routeId);
		/**********************************************编辑路由带宽设置开始*********************************************/
		String rate = map.get("rate").toString();
		if (null != route) {
			baseRoute = routeDao.findOne(routeId);
			if (baseRoute != null) {
				baseRoute.setRouteName(routeName);
				int perRate = baseRoute.getRate();
				baseRoute.setRate(Integer.parseInt(rate));
				if(!StringUtils.isEmpty(baseRoute.getQosId())){
					eayunQosService.updateQos(baseRoute,perRate);
				}
				routeDao.saveOrUpdate(baseRoute);
			}
		}
		return baseRoute;

	}

	/**
	 * 设置网关
	 * 
	 * @param datacenterId
	 * @param routeid
	 * @param networkid
	 */
	public CloudRoute setGateWay(String routeid, String datacenterId,
			String networkid) {
		CloudRoute voe = new CloudRoute();

		Route route = openstackService.setGateway(datacenterId, routeid,
				networkid);
		if (route != null
				&& null != route.getExternal_gateway_info().getNetwork_id()) {
			BaseCloudRoute baseRoute = routeDao.findOne(routeid);
			baseRoute
					.setNetId(route.getExternal_gateway_info().getNetwork_id());
			baseRoute.setGatewayIp(route.getIp_address());
			routeDao.saveOrUpdate(baseRoute);
			BeanUtils.copyPropertiesByModel(voe, baseRoute);
		}
		return voe;

	}

	/**
	 * 清除网关
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param id
	 */
	public BaseCloudRoute deleteGateway(String routeid, String datacenterId) {
		BaseCloudRoute baseRoute = new BaseCloudRoute();
		// 创建网络
		Route route = openstackService.removeGateway(datacenterId, routeid);
		if (route != null) {
			baseRoute = routeDao.findOne(routeid);
			baseRoute.setNetId(null);
			baseRoute.setGatewayIp(null);
			routeDao.saveOrUpdate(baseRoute);
		} else {
			baseRoute = routeDao.findOne(routeid);
		}
		return baseRoute;
	}
	/**
	 * 路由连接子网
	 * @param datacenterId
	 * @param routeid
	 * @param subnetworkid
	 */
	public BaseCloudSubNetWork connectSubnet(String dcId,String routeId,String subnetworkId){		
				
			Route route = openstackService.attachInterface(dcId, routeId, subnetworkId);
			if(route!=null){
				BaseCloudRoute cloudRoute =  routeDao.findOne(routeId);
				if(StringUtils.isEmpty(cloudRoute.getQosId())){
					eayunQosService.createQos(cloudRoute);
				}
				//如果路由连接子网未成功，直接throw AppException 不再操作本地DB
				BaseCloudSubNetWork subnet=subNetWorkService.getSubNetworkById(subnetworkId);
				subnet.setRouteId(routeId);
				subNetWorkService.saveOrUpdate(subnet);
				routeDao.saveOrUpdate(cloudRoute);
				return subnet;
			}else{
				return null;
			}
			
		
	}
	/*根据数据中心、项目、路由Id获取实体*/
	public CloudRoute getRouteDetail(String dcId, String prjId, String routeId) {
		StringBuffer sql = new StringBuffer();
		List<Object> list = new ArrayList<Object>();
		List<CloudRoute> listRoute = new ArrayList<CloudRoute>();
		sql.append(" select r.route_id,r.route_name,r.route_status,dc.dc_name,cp.prj_name,net.net_name,r.rate as rate from cloud_route  as r ");
		sql.append(" left join cloud_network as net on r.net_id=net.net_id");
		sql.append(" left join cloud_project as cp on r.prj_id=cp.prj_id");
		sql.append(" left join dc_datacenter as dc on dc.id=r.dc_id");
		sql.append(" where 1=1 ");
		// 数据中心
		if (!"".equals(dcId) && dcId != null && !"undefined".equals(dcId)
				&& !"null".equals(dcId)) {
			sql.append("and r.dc_id = ? ");
			list.add(dcId);
		}
		// prjID
		if (!"null".equals(prjId) && null != prjId && !"".equals(prjId)
				&& !"undefined".equals(prjId)) {
			sql.append(" and r.prj_id = ? ");
			list.add(prjId);
		}
		// 路由ID
		if (!"null".equals(routeId) && null != routeId && !"".equals(routeId)
				&& !"undefined".equals(routeId)) {
			sql.append(" and r.route_id = ? ");
			list.add(routeId);
		}
		javax.persistence.Query query =routeDao.createSQLNativeQuery(sql.toString(), list.toArray());
		List listResult = query.getResultList();
		for (int i = 0; i < listResult.size(); i++) {
			Object[] objs = (Object[]) listResult.get(i);
			CloudRoute route = new CloudRoute();
			route.setRouteId(String.valueOf(objs[0]));
			route.setRouteName(String.valueOf(objs[1]));
			route.setRouteStatus(String.valueOf(objs[2]));
			route.setStatusForRoute(DictUtil.getStatusByNodeEn("route", String.valueOf(objs[2])));
			route.setDcName(String.valueOf(objs[3]));
			route.setPrjName(String.valueOf(objs[4]));
			
			route.setNetName(null!=String.valueOf(objs[5])&&!"".equals(String.valueOf(objs[5]))?String.valueOf(objs[5]):"");
			route.setRate(Integer.parseInt(String.valueOf(objs[6])));
			listRoute.add(route);
		}
		if(listRoute.size()>0){
			return listRoute.get(0);
		}
		
		return null;
	}
	/**
	 * 路由解绑子网   
	 * @param datacenterId
	 * @param projectId
	 * @param id
	 */
	public BaseCloudSubNetWork detachSubnet(String dcId,String routeId,String subnetworkId){		
		BaseCloudSubNetWork subNet=new BaseCloudSubNetWork();
			//底层解绑子网需要用到下面3个参数
			Route route = openstackService.detachInterface(dcId, routeId, subnetworkId);
			if(route!=null){
				//根据子网id 查处绑定了改路由的子网，然后置空子网的routeId
				subNet = subNetWorkService.getSubNetworkById(subnetworkId);
				 if(null!=subNet.getRouteId()&&!"".equals(subNet.getRouteId())){
					 subNet.setRouteId(null);
				 }
				 subNetWorkService.saveOrUpdate(subNet);
			}
			return subNet;
	}
	/**
	 * 删除指定id的路由，包括两步删除操作，
	 * @param datacenterId
	 * @param projectId
	 * @param id
	 * @return
	 */
	public boolean delete(String datacenterId,String routeId,String qosId){
		
			boolean flag = openstackService.delete(datacenterId,null, routeId);
			if (flag) {
				if(null!=routeDao.findOne(routeId)){
					routeDao.delete(routeId);
					//删除资源后更新缓存接口
					tagService.refreshCacheAftDelRes("route", routeId);
					return true;
				}
				return false;
			}
	        return flag;
		
	}
	@Override
	public EayunResponseJson checkForCle(String netId) {
		EayunResponseJson json = new EayunResponseJson();
		BaseCloudRoute routeBean = routeDao.queryByNetworkId(netId);
		//创建的端口映射的数量
		int countPortMapping = portMappingDao.getCountByRouteId(routeBean.getRouteId());
		//创建的VPN的数量
		int countVpn = vpnDao.getCountByRouteId(routeBean.getRouteId());
		//有待创建的云主机或VPN占用了该网关的数量
		int countVmBuyFloatIp = this.getVmCountBuyFloatIpByNetId(netId);
		//网关已被云主机或负载均衡占用的数量
		int countExistResourceOccupyNet = this.getExistResourceOccupyNetByNetId(netId);
		//网关已被云数据库实例占用的数量
		int countRdsInstance = instanceService.getRdsInstanceCountByNetId(netId);
		//有待创建的云数据库实例占用的数量
		int countRdsInstanceToBeCreated = instanceService.getRdsInstanceToBeCreatedByNetId(netId);
		if (countVmBuyFloatIp > 0) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			json.setMessage("有待创建的云主机或VPN占用该网关，无法清除!");
		} else if (countPortMapping + countVpn > 0) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			json.setMessage("网关已被端口映射或VPN占用，请删除后操作!");
		} else if (countExistResourceOccupyNet > 0) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			json.setMessage("网关已被云主机或负载均衡占用，请先解绑其公网IP!");
		} else if (countRdsInstance > 0) {
		    json.setRespCode(ConstantClazz.ERROR_CODE);
		    json.setMessage("清除网关失败，已有云数据库在使用!");
		} else if (countRdsInstanceToBeCreated > 0) {
		    json.setRespCode(ConstantClazz.ERROR_CODE);
		    json.setMessage("清除网关失败，有待创建的云数据库使用!");
		}
		else{
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		}
		return json;
	}	
	/**
	 * 获取当前私有网络下有待创建的云主机或VPN占用了该网关的数量
	 * @param netId
	 * @return
	 */
	private int getVmCountBuyFloatIpByNetId(String netId) {
		StringBuffer sqlBuf = new StringBuffer();
		sqlBuf.append(" select vpn.vpn_count ");
		sqlBuf.append(" ,vm.vm_count ");
		sqlBuf.append(" from  ");
		sqlBuf.append(" ( ");
		sqlBuf.append("  select count(*) as vm_count  ");
		sqlBuf.append("  from cloudorder_vm cov  ");
		sqlBuf.append("  left join order_info oi on cov.order_no = oi.order_no ");
		sqlBuf.append("  where oi.order_type = '0' ");
		sqlBuf.append("  and (oi.order_state = '1' or oi.order_state = '2') ");
		sqlBuf.append("  and cov.buy_floatip = '1' ");
		sqlBuf.append("  and cov.net_id = ?  ");
		sqlBuf.append(" ) as vm, ");
		sqlBuf.append(" ( ");
		sqlBuf.append(" select count(*) as vpn_count ");
		sqlBuf.append(" from cloudorder_vpn cov left join order_info oi on cov.order_no = oi.order_no ");
		sqlBuf.append(" where oi.order_type = '0' ");
		sqlBuf.append(" and (oi.order_state = '1' or oi.order_state = '2') ");
		sqlBuf.append(" and cov.network_id = ? ");
		sqlBuf.append(" ) as vpn ");
		Query query = routeDao.createSQLNativeQuery(sqlBuf.toString(), new Object[]{netId, netId});
		List result = query.getResultList();
		int count = 0;
		if(!result.isEmpty() && result.size() > 0){
			Object[] objs = (Object[]) result.get(0);
			for(Object obj : objs){
				count += Integer.parseInt(obj != null ? String.valueOf(obj) : "0");
			}
		}
        return count;
	}
	/**
	 * 获取网关已被云主机或负载均衡占用的数量
	 * @param netId
	 * @return
	 */
	private int getExistResourceOccupyNetByNetId(String netId){
		StringBuffer sqlBuf = new StringBuffer();
		sqlBuf.append(" select vm.vm_count + ldpool_count as count ");
		sqlBuf.append(" from  ");
		sqlBuf.append(" ( ");
		sqlBuf.append(" select count(*) as vm_count ");
		sqlBuf.append(" from cloud_vm vm left join cloud_floatip flo on flo.resource_id = vm.vm_id ");
		sqlBuf.append(" where flo.flo_ip != '' and flo.flo_ip is not null and vm.is_deleted = '0' and vm.net_id = ? ");
		sqlBuf.append(" ) vm, ");
		sqlBuf.append(" ( ");
		sqlBuf.append(" select count(*) as ldpool_count ");
		sqlBuf.append(" from cloud_ldpool cl left join cloud_subnetwork cs on cs.subnet_id = cl.subnet_id ");
		sqlBuf.append(" left join cloud_floatip flo on flo.resource_id = cl.pool_id ");
		sqlBuf.append(" where flo.flo_ip != '' and flo.flo_ip is not null and cs.net_id = ? ");
		sqlBuf.append(" ) ldpool ");
		Query query = routeDao.createSQLNativeQuery(sqlBuf.toString(), new Object[]{netId, netId});
		Object result = query.getSingleResult();
        int count = result == null ? 0 : Integer.parseInt(result.toString());
        return count;
	}
	@Override
	public EayunResponseJson checkDetachSubnet(String subnetId) {
		EayunResponseJson json = new EayunResponseJson();
		boolean toBeCreateflag = this.getVmOrVpnBindSubnetBySubnetId(subnetId);
		boolean existFlag = this.getExistResourceBindSubnetBySubnetId(subnetId);
		boolean pmOrVpnBindSubnetFlag = this.getPmOrVpnBindSubnetBySubnetId(subnetId);
		int countRdsInstance = instanceService.getRdsInstanceCountBySubnet(subnetId);
		int countRdsInstanceToBeCreated = instanceService.getRdsInstanceToBeCreatedBySubnet(subnetId);
		if (existFlag) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			json.setMessage("断开路由失败，断开路由前需解绑子网内的云主机或负载均衡器绑定的公网IP!");
		} else if (pmOrVpnBindSubnetFlag){
			json.setRespCode(ConstantClazz.ERROR_CODE);
			json.setMessage("断开路由失败，断开路由前需删除VPN服务或端口映射!");
		} else if (toBeCreateflag){
			json.setRespCode(ConstantClazz.ERROR_CODE);
			json.setMessage("有待创建的云主机或VPN占用该受管子网，无法解绑!");
		} else if (countRdsInstance > 0) {
		    json.setRespCode(ConstantClazz.ERROR_CODE);
		    json.setMessage("断开路由失败，已有云数据库在使用!");
		} else if (countRdsInstanceToBeCreated > 0) {
		    json.setRespCode(ConstantClazz.ERROR_CODE);
		    json.setMessage("断开路由失败，有待创建的云数据库使用!");
		} else{
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		}
		return json;
	}
	/**
	 * 判断是否有待创建的云主机或VPN占用该受管子网
	 * @param subnetId
	 * @return
	 */
	private boolean getVmOrVpnBindSubnetBySubnetId(String subnetId) {
		boolean flag = false;
		StringBuffer sqlBuf = new StringBuffer();
		sqlBuf.append("    select vm.vm_count + vpn.vpn_count as count ");
		sqlBuf.append("    from  ");
		sqlBuf.append("    ( ");
		sqlBuf.append("    select count(*) as vm_count  ");
		sqlBuf.append("    from cloudorder_vm cov    ");
		sqlBuf.append("    left join order_info oi on cov.order_no = oi.order_no    ");
		sqlBuf.append("    where oi.order_type = '0'   ");
		sqlBuf.append("    and (oi.order_state = '1' or oi.order_state = '2')    ");
		//sqlBuf.append("    and cov.buy_floatip = '1'    ");
		sqlBuf.append("    and cov.subnet_id = ? ");
		sqlBuf.append("    ) vm, ");
		sqlBuf.append("    ( ");
		sqlBuf.append("    select count(*) as vpn_count  ");
		sqlBuf.append("    from cloudorder_vpn cov    ");
		sqlBuf.append("    left join order_info oi on cov.order_no = oi.order_no    ");
		sqlBuf.append("    where oi.order_type = '0'   ");
		sqlBuf.append("    and (oi.order_state = '1' or oi.order_state = '2')    ");
		sqlBuf.append("    and cov.subnet_id = ? ");
		sqlBuf.append("    ) vpn ");
		Query query = routeDao.createSQLNativeQuery(sqlBuf.toString(), new Object[]{subnetId, subnetId});
        Object result = query.getSingleResult();
        int count = result == null ? 0 : Integer.parseInt(result.toString());
        if(count > 0)
        	flag = true;
		return flag;
	}
	/**
	 * 判断子网内云主机或负载均衡器是否绑定公网IP
	 * @param subnetId
	 * @return
	 */
	private boolean getExistResourceBindSubnetBySubnetId(String subnetId) {
		boolean flag = false;
		StringBuffer sqlBuf = new StringBuffer();
		sqlBuf.append(" select (vm.vm_count + ldpool.ldpool_count) as count ");
		sqlBuf.append(" from   ");
		sqlBuf.append(" ( ");
		sqlBuf.append(" select count(*) as vm_count ");
		sqlBuf.append(" from cloud_vm vm left join cloud_floatip flo on flo.resource_id = vm.vm_id ");
		sqlBuf.append(" where flo.flo_ip != '' and flo.flo_ip is not null and vm.is_deleted = '0' and vm.subnet_id = ? ");
		sqlBuf.append(" ) vm, ");
		sqlBuf.append(" ( ");
		sqlBuf.append(" select count(*) as ldpool_count ");
		sqlBuf.append(" from cloud_ldpool cl left join cloud_floatip flo on flo.resource_id = cl.pool_id");
		sqlBuf.append(" where flo.flo_ip != '' and flo.flo_ip is not null and subnet_id = ? ");
		sqlBuf.append(" ) ldpool ");
		Query query = routeDao.createSQLNativeQuery(sqlBuf.toString(), new Object[]{subnetId, subnetId});
        Object result = query.getSingleResult();
        int count = result == null ? 0 : Integer.parseInt(result.toString());
        if(count > 0)
        	flag = true;
		return flag;
	}
	/**
	 * 判断VPN服务或端口映射使用该子网
	 * @param subnetId
	 * @return
	 */
	private boolean getPmOrVpnBindSubnetBySubnetId(String subnetId){
		boolean flag = false;
		StringBuffer sqlBuf = new StringBuffer();
		sqlBuf.append(" select portmapping.portmapping_count + vpn.vpnservice_count as count ");
		sqlBuf.append(" from ");
		sqlBuf.append(" ( ");
		sqlBuf.append(" select count(*) as portmapping_count ");
		sqlBuf.append(" from cloud_portmapping cp left join cloud_vm vm on vm.vm_id = cp.destiny_id ");
		sqlBuf.append(" where vm.subnet_id = ? ");
		sqlBuf.append(" ) as portmapping, ");
		sqlBuf.append(" ( ");
		sqlBuf.append("  select count(*) as vpnservice_count  ");
		sqlBuf.append("  from cloud_vpnservice  ");
		sqlBuf.append("  where subnet_id = ? ");
		sqlBuf.append(" ) vpn ");
		Query query = routeDao.createSQLNativeQuery(sqlBuf.toString(), new Object[]{subnetId, subnetId});
        Object result = query.getSingleResult();
        int count = result == null ? 0 : Integer.parseInt(result.toString());
        if(count > 0)
        	flag = true;
		return flag;
	}
}

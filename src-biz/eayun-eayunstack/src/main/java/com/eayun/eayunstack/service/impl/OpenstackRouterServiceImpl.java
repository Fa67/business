package com.eayun.eayunstack.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.Port;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.BandWidth;
import com.eayun.eayunstack.model.NetworkId;
import com.eayun.eayunstack.model.Route;
import com.eayun.eayunstack.service.OpenstackRouterService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;
import com.eayun.virtualization.model.BaseCloudRoute;
import com.eayun.virtualization.model.BaseCloudSubNetWork;

/**
 * openstack网络服务的service类
 */
@Service
public class OpenstackRouterServiceImpl extends OpenstackBaseServiceImpl<Route>implements OpenstackRouterService {

	/**
	 * 私有方法，用于将JSONObject对象中的一些无法自动转换的参数，手动设置到java对象中
	 * 
	 * @param vm
	 * @param object
	 */
	private void initData(Route route, JSONObject object) {
	}

	/**
	 * 获取指定数据中心的指定项目下的路由列表
	 */
	@Override
	public List<Route> list(String datacenterId, String projectId) throws AppException {
		List<Route> list = new ArrayList<Route>();
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.ROUTE_URI);

		List<JSONObject> result = restService.list(restTokenBean, OpenstackUriConstant.ROUTE_DATA_NAMES);
		if (result != null && result.size() > 0) {
			for (JSONObject jsonObject : result) {
				Route route = restService.json2bean(jsonObject, Route.class);
				initData(route, jsonObject);
				list.add(route);
			}
		}

		return list;
	}

	/**
	 * 获取指定数据中心下的所有项目的云主机总和的列表
	 */
	@Override
	public List<Route> listAll(String datacenterId) throws AppException {
		return list(datacenterId, null);
	}

	public List<Port> listport(String datacenterId, String routerid) throws AppException {
		List<Port> list = new ArrayList<Port>();
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean
				.setUrl(OpenstackUriConstant.PORT_URI + "?device_owner=network:router_interface&device_id=" + routerid);
		List<JSONObject> result = restService.list(restTokenBean, OpenstackUriConstant.PORT_DATA_NAMES);
		if (result != null && result.size() > 0) {
			for (JSONObject jsonObject : result) {
				Port port = restService.json2bean(jsonObject, Port.class);
				list.add(port);
			}
		}

		return list;
	}

	/**
	 * 获取指定数据中心下的指定id的云主机的详情
	 */
	public Route getById(String datacenterId, String projectId, String id) throws AppException {
		Route result = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, OpenstackUriConstant.NETWORK_SERVICE_URI);
		// 执行具体业务操作，并获取返回结果
		JSONObject json = restService.getById(restTokenBean, OpenstackUriConstant.ROUTE_URI + "/",
				OpenstackUriConstant.ROUTE_DATA_NAME, id);
		result = restService.json2bean(json, Route.class);

		return result;
	}

	/**
	 * 创建网络
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 *            json字符串，包含待创建的云主机的具体配置信息
	 * @return
	 * @throws AppException
	 */
	public Route create(String datacenterId, String projectId, JSONObject data) throws AppException {
		Route route = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.ROUTE_URI);
		// 执行具体业务操作，并获取返回结果
		JSONObject result = restService.create(restTokenBean, OpenstackUriConstant.ROUTE_DATA_NAME, data);
		// 将获取的JSONObject对象转换为model包中定义的与之对应的java对象
		route = restService.json2bean(result, Route.class);

		return route;
	}
	
	

	/**
	 * 修改网络
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 *            json字符串，包含待修改网络的具体配置信息
	 * @return
	 * @throws AppException
	 */
	public Route update(String datacenterId, String projectId, JSONObject netrouteObject, String id)
			throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.ROUTE_URI + "/" + id);
		JSONObject result = restService.update(restTokenBean, OpenstackUriConstant.ROUTE_DATA_NAME, netrouteObject);
		Route route = restService.json2bean(result, Route.class);
		initData(route, result);
		return route;
	}

	public Route setGateway(String datacenterId, String id, String networkid) throws AppException {
		Route route = null;
		// 根据操作类型设置request body 对象
		JSONObject edit = new JSONObject();
		JSONObject temp = new JSONObject();
		NetworkId networkId = new NetworkId();
		networkId.setNetwork_id(networkid);
		networkId.setEnable_snat(true);

		temp.put("external_gateway_info", networkId);
		edit.put("router", temp);
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.ROUTE_URI + "/" + id);
		JSONObject result = restService.update(restTokenBean, OpenstackUriConstant.ROUTE_DATA_NAME, edit);

		// 将获取的JSONObject对象转换为model包中定义的与之对应的java对象
		route = restService.json2bean(result, Route.class);
		if(null!=result){
			JSONObject json = result.getJSONObject("external_gateway_info");
			if(null!=json){
				JSONArray ips = json.getJSONArray("external_fixed_ips");
				if(null!=ips&&ips.size()==1){
					route.setIp_address(ips.getJSONObject(0).getString("ip_address"));
				}
			}
		}
		return route;
	}

	public Route removeGateway(String datacenterId, String id) throws AppException {
		Route route = null;
		// 根据操作类型设置request body 对象
		JSONObject edit = new JSONObject();
		JSONObject temp = new JSONObject();
		temp.put("external_gateway_info", "");
		edit.put("router", temp);

		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.ROUTE_URI + "/" + id);
		// 执行具体业务操作，并获取返回结果
		JSONObject result = restService.update(restTokenBean, OpenstackUriConstant.ROUTE_DATA_NAME, edit);
		// 将获取的JSONObject对象转换为model包中定义的与之对应的java对象
		route = restService.json2bean(result, Route.class);
		return route;
	}

	public Route attachInterface(String datacenterId, String id, String subnetworkid) throws AppException {
		Route route = null;
		// 根据操作类型设置request body 对象
		JSONObject edit = new JSONObject();
		edit.put("subnet_id", subnetworkid);
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.ROUTE_URI + "/" + id + "/add_router_interface");
		// 执行具体业务操作，并获取返回结果
		JSONObject result = restService.update(restTokenBean, null, edit);
		// 将获取的JSONObject对象转换为model包中定义的与之对应的java对象
		route = restService.json2bean(result, Route.class);
		return route;
	}

	public Route detachInterface(String datacenterId, String id, String subnetworkid) throws AppException {
		Route route = null;
		// 根据操作类型设置request body 对象
		JSONObject edit = new JSONObject();
		edit.put("subnet_id", subnetworkid);
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.ROUTE_URI + "/" + id + "/remove_router_interface");
		// 执行具体业务操作，并获取返回结果
		JSONObject result = restService.update(restTokenBean, null, edit);
		// 将获取的JSONObject对象转换为model包中定义的与之对应的java对象
		route = restService.json2bean(result, Route.class);
		return route;
	}

	/**
	 * 删除指定id的路由
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public boolean delete(String datacenterId, String projectId, String id) throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.ROUTE_URI + "/" + id);
		return restService.delete(restTokenBean);
	}

	/**
	 * 获取底层数据中心下的路由 -----------------
	 * 
	 * @author zhouhaitao
	 * @param dataCenter
	 * 
	 * @return
	 * 
	 */
	@SuppressWarnings("rawtypes")
	public Map<String, List> getStackList(BaseDcDataCenter dataCenter) {
		Map<String, List> map = new HashMap<String, List>();
		List<BaseCloudRoute> list = new ArrayList<BaseCloudRoute>();
		List<BaseCloudSubNetWork> subList = new ArrayList<BaseCloudSubNetWork>();
		RestTokenBean restTokenBean = getRestTokenBean(dataCenter.getId(), OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.ROUTE_URI);
		List<JSONObject> result = restService.list(restTokenBean, OpenstackUriConstant.ROUTE_DATA_NAMES);

		if (result != null && result.size() > 0) {
			for (JSONObject jsonObject : result) {
				Route data = restService.json2bean(jsonObject, Route.class);
				BaseCloudRoute ccn = new BaseCloudRoute(data, dataCenter.getId());
				initGateway(jsonObject,ccn);
				list.add(ccn);
				restTokenBean.setUrl(OpenstackUriConstant.PORT_URI + "?device_owner=network:router_interface&device_id="
						+ ccn.getRouteId());
				List<JSONObject> portJson = restService.list(restTokenBean, OpenstackUriConstant.PORT_DATA_NAMES);
				initBindSubnet(subList, portJson);
			}
		}
		map.put("RouteList", list);
		map.put("SubList", subList);
		return map;
	}
	
	private void initGateway (JSONObject json,BaseCloudRoute route){
		if(null!=json){
			JSONObject gateway = json.getJSONObject("external_gateway_info");
			if(null!=gateway){
				JSONArray ips = gateway.getJSONArray("external_fixed_ips");
				if(null!=ips&&ips.size()==1){
					JSONObject ip =ips.getJSONObject(0);
					if(null!=ip){
						route.setGatewayIp(ip.getString("ip_address"));
					}
				}
			}
		}
	}

	/**
	 * 获取路由连接的子网
	 * 
	 * @param subList
	 * @param portJson
	 */
	private void initBindSubnet(List<BaseCloudSubNetWork> subList, List<JSONObject> portJson) {
		if (null != portJson && portJson.size() > 0) {
			for(JSONObject json :portJson){
				if (null != json) {
					JSONArray ipArrays = json.getJSONArray("fixed_ips");
					if (null != ipArrays && ipArrays.size() > 0) {
						for(int i=0;i<ipArrays.size();i++){
							JSONObject ipJson = ipArrays.getJSONObject(i);
							if (null != ipJson) {
								BaseCloudSubNetWork net = new BaseCloudSubNetWork();
								net.setRouteId(json.getString("device_id"));
								net.setSubnetId(ipJson.getString("subnet_id"));
								subList.add(net);
							}
						}
					}
				}
			}
		}
	}
	/************************************************设置路由带宽方法开始*********************************************************/
	
	/**
	 * 创建路由带宽
	 * default_rate 
	 * direction 
	 * rate 
	 * default_rate 
	 * tenant_id
	 * target_type
	 * @param projectId
	 * @param data
	 * json字符串，具体配置信息
	 * @return
	 * @throws AppException
	 */
	public BandWidth createBandWidth(String datacenterId, String projectId, JSONObject data) throws AppException {
		BandWidth bandWidth = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.ROUTE_BANDWIDTH_URI);
		// 执行具体业务操作，并获取返回结果
		JSONObject result = restService.create(restTokenBean, OpenstackUriConstant.ROUTE_BANDWIDTH_DATA_NAME, data);
		// 将获取的JSONObject对象转换为model包中定义的与之对应的java对象
		bandWidth = restService.json2bean(result, BandWidth.class);

		return bandWidth;
	}
	/**
	 * 删除指定id路由的带宽
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public boolean deleteQos(String datacenterId, String qosId) throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.ROUTE_BANDWIDTH_URI + "/" + qosId);
		return restService.delete(restTokenBean);
	}
	/**
	 * 修改带宽
	 * @param datacenterId
	 * @param 带宽id
	 * json字符串（bandWidthObject），包含待修改带宽的具体配置信息
	 * @return
	 * @throws AppException
	 */
	public BandWidth updateBandWidth(String datacenterId, JSONObject bandWidthObject, String id)
			throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.ROUTE_BANDWIDTH_URI + "/" + id);
		JSONObject result = restService.update(restTokenBean, OpenstackUriConstant.ROUTE_BANDWIDTH_DATA_NAME, bandWidthObject);
		BandWidth bandWidth = restService.json2bean(result, BandWidth.class);
		return bandWidth;
	}
	
}

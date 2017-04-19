package com.eayun.virtualization.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.eayunstack.model.EayunQos;
import com.eayun.eayunstack.model.EayunQosFilter;
import com.eayun.eayunstack.model.EayunQosQueue;
import com.eayun.eayunstack.service.OpenstackEayunQosFilterService;
import com.eayun.eayunstack.service.OpenstackEayunQosQueueService;
import com.eayun.eayunstack.service.OpenstackEayunQosService;
import com.eayun.virtualization.model.BaseCloudRoute;
import com.eayun.virtualization.service.EayunQosService;

@Service
public class EayunQosServiceImpl implements EayunQosService {
	private static final int MAX_ROUTE_BANDWIDTY=1000;
	@Autowired
	private OpenstackEayunQosService openstackQosService;
	@Autowired
	private OpenstackEayunQosQueueService openstackQosQueueService;
	@Autowired
	private OpenstackEayunQosFilterService openstackQosFilterService;
	
	/**
	 * 
	 * 新增EayunQos信息<br>
	 * 	创建内网路由Qos<br>
	 * 	创建创建内网路由Queue<br>
	 * 	创建内网对应的3个Filter（10.0.0.0/8 ,172.16.0.0/12 ,192.168.0.0/16 ）<br>
	 * 
	 * @author zhouhaitao
	 * @param route
	 * @return
	 */
	public void createQos(BaseCloudRoute route){
		//Qos
		EayunQos eayunQos = createEayunQos(route);
		route.setQosId(eayunQos.getId());
		route.setDefaultQueueId(eayunQos.getDefault_queue_id());
		
		//Qos Filter(10.0.0.0/8 ,172.16.0.0/12 ,192.168.0.0/16)
//		createEayunQosFilter(route,eayunQos.getDefault_queue_id(),"10.0.0.0/8",1);
//		createEayunQosFilter(route,eayunQos.getDefault_queue_id(),"172.16.0.0/12",2);
//		createEayunQosFilter(route,eayunQos.getDefault_queue_id(),"192.168.0.0/16",3);
//		
//		//Qos Queue
//		EayunQosQueue eayunQosQueue = createEayunQosQueue(route,125000*route.getRate());
//		route.setFilterQueueId(eayunQosQueue.getId());
//		
//		//拦截剩余所有的连接
//		createEayunQosFilter(route,eayunQosQueue.getId(),"0.0.0.0/0",65535);
	}
	
	/**
	 * 修改EayunQos 信息
	 * @param route
	 * @return
	 */
	public void updateQos(BaseCloudRoute route,int perRate){
		//修改的QOS默认的QUEUE的带宽大小
		modifyEayunQos(route.getDcId(),route.getQosId(),route.getRate(),perRate);
		
		//修改创建的Filter的QUEUE的带宽的大小
//		modifyEayunQosQueue(route.getDcId(),route.getFilterQueueId(),125000*route.getRate());
//		
//		modifyEayunQos(route.getDcId(),route.getQosId(),0,0);
	}
	
	/**
	 * 根据qosId查询底层的QOS
	 * @param route
	 * @return
	 */
	public EayunQos getQosById(BaseCloudRoute route){
		EayunQos eayunQos = null;
		eayunQos = openstackQosService.get(route.getDcId(),route.getQosId());
		return eayunQos;
	}
	
	/**
	 * 修改QOS的目标路由 target_id
	 * @param route
	 */
	public void modifyTarget (BaseCloudRoute route){
		modifyEayunQos(route.getDcId(),route.getQosId(),route.getRouteId());
	}
	
	/**
	 * 提供给私有网络更改带宽的接口
	 */
	public void changeQos(String dcId, String qosId, int rate) {
	    modifyEayunQos(dcId, qosId, rate ,rate);
	}
	
	/**
	 * 创建EayunQos 
	 * @param route
	 * @return
	 */
	private EayunQos createEayunQos (BaseCloudRoute route){
		EayunQos eayunQos = null;
		JSONObject  data = new JSONObject ();
		JSONObject  qos = new JSONObject ();
		qos.put("direction", "egress");
		qos.put("rate", 125000*route.getRate());
		qos.put("default_rate", 125000*route.getRate());
		qos.put("tenant_id", route.getPrjId());
		qos.put("name", route.getRouteName());
		qos.put("target_type", "router");
		qos.put("target_id", route.getRouteId());
		qos.put("default_burst", 2500*route.getRate());
		qos.put("default_cburst", 2500*route.getRate());
		data.put("qos", qos);
		
		eayunQos = openstackQosService.create(route.getDcId(), data);
		
		return eayunQos;
	}
	
	/**
	 * 创建EayunQosQueue 
	 * @param route
	 * @return
	 */
	private EayunQosQueue createEayunQosQueue (BaseCloudRoute route,long rate){
		EayunQosQueue eayunQosQueue = null;
		JSONObject  data = new JSONObject ();
		JSONObject  qos = new JSONObject ();
		qos.put("qos_id", route.getQosId());
		qos.put("rate", rate);
		qos.put("tenant_id", route.getPrjId());
		qos.put("ceil", rate);
		qos.put("prio", 7);
		data.put("qos_queue", qos);
		
		eayunQosQueue = openstackQosQueueService.create(route.getDcId(), data);
		
		return eayunQosQueue;
	}
	/**
	 * 创建EayunQosFilter 
	 * @param route
	 * @return
	 */
	private EayunQosFilter createEayunQosFilter (BaseCloudRoute route,String queueId,String addrCidr,int prio){
		EayunQosFilter eayunQosFilter = null;
		JSONObject  data = new JSONObject ();
		JSONObject  qos = new JSONObject ();
		qos.put("qos_id", route.getQosId());
		qos.put("prio", prio);
		qos.put("tenant_id", route.getPrjId());
		qos.put("queue_id", queueId);
		if(!StringUtils.isEmpty(addrCidr)){
			qos.put("src_addr", addrCidr);
		}
		
		data.put("qos_filter", qos);
		
		eayunQosFilter = openstackQosFilterService.create(route.getDcId(), data);
		
		return eayunQosFilter;
	}
	
	/**
	 * 修改QOS
	 * @param dcId
	 * @param qosId
	 * @param rate
	 * @return
	 */
	private EayunQos modifyEayunQos(String dcId,String qosId,long rate,int perRate){
		EayunQos eayunQos = null;
		JSONObject  data = new JSONObject ();
		JSONObject  qos = new JSONObject ();
		if(rate!=0){
			qos.put("rate", 125000*rate);
			qos.put("default_rate", 125000*rate);
			qos.put("default_burst", 2500*rate);
			qos.put("default_cburst", 2500*rate);
		}
		data.put("qos", qos);
		eayunQos = openstackQosService.modify(dcId, data, qosId);
		return eayunQos;
	}
	
	/**
	 * 修改QOS
	 * @param dcId
	 * @param qosId
	 * @param rate
	 * @return
	 */
	private EayunQos modifyEayunQos(String dcId,String qosId,String routeId){
		EayunQos eayunQos = null;
		JSONObject  data = new JSONObject ();
		JSONObject  qos = new JSONObject ();
		qos.put("target_id", routeId);
		data.put("qos", qos);
		eayunQos = openstackQosService.modify(dcId, data, qosId);
		return eayunQos;
	}
	
	/**
	 * 修改QOS QUEUE
	 * @param dcId
	 * @param queueId
	 * @param rate
	 * @return
	 */
	private EayunQosQueue modifyEayunQosQueue(String dcId,String queueId,long rate){
		EayunQosQueue eayunQosQueue = null;
		JSONObject  data = new JSONObject ();
		JSONObject  qos = new JSONObject ();
		qos.put("rate", rate);
		qos.put("ceil", rate);
		data.put("qos_queue", qos);
		
		eayunQosQueue = openstackQosQueueService.modify(dcId, data, queueId);
		return eayunQosQueue;
	}
	

}

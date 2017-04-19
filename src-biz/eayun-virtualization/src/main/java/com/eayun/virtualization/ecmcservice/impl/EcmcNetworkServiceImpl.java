/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.virtualization.ecmcservice.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.charge.model.ChargeRecord;
import com.eayun.common.ConstantClazz;
import com.eayun.common.constant.PayType;
import com.eayun.common.constant.ResourceType;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.template.EayunRabbitTemplate;
import com.eayun.common.tools.DictUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.CloudResourceUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.eayunstack.model.Network;
import com.eayun.eayunstack.model.Route;
import com.eayun.eayunstack.service.OpenstackNetworkService;
import com.eayun.eayunstack.service.OpenstackRouterService;
import com.eayun.virtualization.baseservice.BaseNetworkService;
import com.eayun.virtualization.dao.CloudLdPoolDao;
import com.eayun.virtualization.dao.CloudNetWorkDao;
import com.eayun.virtualization.dao.CloudRouteDao;
import com.eayun.virtualization.dao.CloudSubNetWorkDao;
import com.eayun.virtualization.ecmcservice.EcmcNetworkService;
import com.eayun.virtualization.model.BaseCloudNetwork;
import com.eayun.virtualization.model.BaseCloudRoute;
import com.eayun.virtualization.model.CloudNetWork;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.model.CloudRoute;
import com.eayun.virtualization.service.EayunQosService;
import com.eayun.virtualization.service.TagService;

/**
 * 
 * @Filename: EcmcNetworkServiceImpl.java
 * @Description:
 * @Version: 1.0
 * @Author: fangjun.yang
 * @Email: fangjun.yang@eayun.com
 * @History:<br>
 * 				<li>Date: 2016年3月31日</li>
 *               <li>Version: 1.0</li>
 *               <li>Content: create</li>
 *
 */
@Service
@Transactional
public class EcmcNetworkServiceImpl extends BaseNetworkService implements EcmcNetworkService {

	private final static Logger log = LoggerFactory.getLogger(EcmcNetworkServiceImpl.class);

	@Autowired
	private CloudNetWorkDao cloudNetworkDao;

	@Autowired
	private CloudRouteDao cloudRouteDao;

	@Autowired
	private CloudLdPoolDao cloudLdPoolDao;
	
	@Autowired
	private CloudSubNetWorkDao cloudSubNetWorkDao;

	@Autowired
	private OpenstackNetworkService openstackNetworkService;

	@Autowired
	private OpenstackRouterService openstackRouterService;

	@Autowired
	private TagService tagService;
	
	@Autowired
	private EayunQosService eayunQosService;
	
	@Autowired
	private EayunRabbitTemplate rabbitTemplate;

	public Page getNetworkList(String netName, String dcId, String prjName, String cusOrg, QueryMap queryMap) throws Exception {
        List<Object> params = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer();
        sql.append("select cn.net_id,cn.net_name,cn.net_status,cn.admin_stateup,cn.prj_id,cn.dc_id,count(cs.subnet_id) subNetCount, rou.rate,");
        sql.append("rou.net_name extNetName,rou.route_name,rou.route_id,ss.cus_id,ss.cus_org,rou.net_id as extNetId,dc.dc_name as dcName,cp.prj_name as prjName");
        sql.append(" ,cn.pay_type as payType, cn.end_time as endTime, cn.charge_state as chargeState ");
        sql.append(" ,cn.create_time ");
        sql.append(" from cloud_network cn");
        sql.append(" left outer join cloud_subnetwork cs on cn.net_id=cs.net_id");
        sql.append(" left outer join dc_datacenter dc on dc.id=cn.dc_id");
        sql.append(" left outer join");
        sql.append(" (select cr.rate,cn1.net_name,cr.network_id,cr.route_name,cr.route_id,cr.net_id from cloud_route cr left outer join cloud_network cn1 on cn1.net_id=cr.net_id) rou");
        sql.append(" on rou.network_id=cn.net_id");
        sql.append(" left outer join cloud_project cp on cn.prj_id = cp.prj_id");
        sql.append(" left outer join sys_selfcustomer ss on cp.customer_id = ss.cus_id");
        sql.append(" where cn.router_external='0' and cn.is_visible = '1' ");
        int idx = 1;
        if(!StringUtil.isEmpty(prjName)){
        	sql.append(" and cp.prj_name in(?").append(idx++).append(") ");
        	 params.add(Arrays.asList(StringUtils.split(prjName, ",")));
        }
        
        if(!StringUtil.isEmpty(dcId)){
            sql.append(" and cn.dc_id = ?").append(idx++);
            params.add(dcId);
        }
        if(!StringUtil.isEmpty(cusOrg)){
            sql.append(" and ss.cus_org in(?").append(idx++).append(") ");
            params.add(Arrays.asList(StringUtils.split(cusOrg, ",")));
        }
        
        if(!StringUtil.isEmpty(netName)){
            netName=netName.replaceAll("\\_", "\\\\_");
            sql.append(" and binary cn.net_name like ?").append(idx++);
            params.add("%"+netName+"%");
        }
        sql.append(" group by cn.net_id ");
        sql.append(" order by cn.dc_id , cn.prj_id ,cn.create_time desc");
        
        Page page = cloudNetworkDao.pagedNativeQuery(sql.toString(), queryMap, params.toArray());
        
        @SuppressWarnings("unchecked")
        List<Object> dataList = (List<Object>)page.getResult();
        for (int i = 0; i < dataList.size(); i++) {
        	Object[] objs = (Object[]) dataList.get(i);
            CloudNetWork cloudNetWork = new CloudNetWork();
            cloudNetWork.setNetId(ObjectUtils.toString(objs[0]));
            cloudNetWork.setNetName(ObjectUtils.toString(objs[1]));
            cloudNetWork.setNetStatus(ObjectUtils.toString(objs[2]));
            cloudNetWork.setAdminStateup(ObjectUtils.toString(objs[3]));
            cloudNetWork.setAdminStaName(ObjectUtils.toString(objs[3]).equals("1")?"UP":"DOWN");
            cloudNetWork.setPrjId(ObjectUtils.toString(objs[4]));
            cloudNetWork.setDcId(ObjectUtils.toString(objs[5]));
            cloudNetWork.setSubNetCount(Integer.valueOf(ObjectUtils.toString(objs[6])));
            cloudNetWork.setRate(Integer.valueOf(ObjectUtils.toString(objs[7]==null?0:objs[7])));
            cloudNetWork.setExtNetName(objs[8]==null ? null:ObjectUtils.toString(objs[8]));
            cloudNetWork.setRouteName(ObjectUtils.toString(objs[9]));
            cloudNetWork.setRouteId(objs[10]==null?null:ObjectUtils.toString(objs[10]));
            cloudNetWork.setNetStatusName(DictUtil.getStatusByNodeEn("netWork", cloudNetWork.getNetStatus()));
            cloudNetWork.setCusId(objs[11]==null?null:ObjectUtils.toString(objs[11]));
            cloudNetWork.setCusOrg(objs[12]==null?null:ObjectUtils.toString(objs[12]));
            cloudNetWork.setExtNetId(ObjectUtils.toString(objs[13]));
            cloudNetWork.setDcName(ObjectUtils.toString(objs[14]));
            cloudNetWork.setPrjName(ObjectUtils.toString(objs[15]));
            /* 用户中心改版计费相关 */
            cloudNetWork.setPayType(ObjectUtils.toString(objs[16]));
            cloudNetWork.setEndTime((Date)objs[17]);
            cloudNetWork.setChargeState(ObjectUtils.toString(objs[18]));
            cloudNetWork.setCreateTime((Date)objs[19]);
            cloudNetWork.setPayTypeStr(CloudResourceUtil.escapePayType(cloudNetWork.getPayType()));
            if ("0".equals(cloudNetWork.getChargeState())) {
            	cloudNetWork.setNetStatusName(DictUtil.getStatusByNodeEn("netWork", cloudNetWork.getNetStatus()));
            } else {
            	cloudNetWork.setNetStatusName(CloudResourceUtil.escapseChargeState(cloudNetWork.getChargeState()));
            }
            dataList.set(i, cloudNetWork);
		}
        return page;
	}

	public CloudNetWork addNetWork(CloudNetWork cloudNetWork) {
		// 网络数据
		JSONObject net = new JSONObject();
		net.put("name", cloudNetWork.getNetName());
		net.put("admin_state_up", "true");
		// 用于提交的完整数据
		JSONObject data = new JSONObject();
		data.put("network", net);
		Network netWork = openstackNetworkService.create(cloudNetWork.getDcId(), cloudNetWork.getPrjId(), data);
		if (netWork != null) {
			BaseCloudNetwork baseNetWork = new BaseCloudNetwork();
			cloudNetWork.setNetId(netWork.getId());
			cloudNetWork.setNetName(netWork.getName());
			cloudNetWork.setCreateTime(new Date());
			cloudNetWork.setAdminStateup("1");
			cloudNetWork.setIsShared("1");
			cloudNetWork.setRouterExternal("0");
			cloudNetWork.setNetStatus(netWork.getStatus());
			BeanUtils.copyPropertiesByModel(baseNetWork, cloudNetWork);
			cloudNetworkDao.save(baseNetWork);// 数据库添加
		}
		CloudRoute cloudRoute = new CloudRoute();
		cloudRoute.setRouteName(cloudNetWork.getNetName() + dateToStr(new Date()));
		cloudRoute.setPrjId(cloudNetWork.getPrjId());
		cloudRoute.setDcId(cloudNetWork.getDcId());
		cloudRoute.setCreateName(cloudNetWork.getCreateName());
		cloudRoute.setRate(cloudNetWork.getRate());
		cloudRoute.setNetWorkId(cloudNetWork.getNetId());
		this.addRoute(cloudRoute);
		cloudNetWork.setRouteId(cloudRoute.getRouteId());
		cloudNetWork.setRouteName(cloudRoute.getRouteName());
		return cloudNetWork;
	}

	protected void addRoute(CloudRoute cloudRoute) {
		JSONObject routeJson = new JSONObject();
		routeJson.put("name", cloudRoute.getRouteName());
		JSONObject resultData = new JSONObject();
		resultData.put("router", routeJson);
		Route route = openstackRouterService.create(cloudRoute.getDcId(), cloudRoute.getPrjId(), resultData);
		if (null != route) {
			BaseCloudRoute baseRoute = new BaseCloudRoute();
			BeanUtils.copyPropertiesByModel(baseRoute, cloudRoute);
			baseRoute.setRouteId(route.getId());
			baseRoute.setRouteName(route.getName());
			baseRoute.setRouteStatus(route.getStatus());
			baseRoute.setCreateTime(new Date());
			// 设置路由带宽
			baseRoute.setRate(cloudRoute.getRate());
			// 保存到本地库
			cloudRouteDao.save(baseRoute);
			cloudRoute.setRouteId(baseRoute.getRouteId());
		}

	}

	public boolean checkNetworkName(String datacenterId, String projectId, String netName, String netId)
			throws AppException {
		if (StringUtils.isBlank(netName)) {
			return false;
		}
		return cloudNetworkDao.countBy(projectId, datacenterId, netName, netId).intValue() > 0 ? true : false;
	}

	public CloudNetWork updateNetwork(CloudNetWork cloudNetWork) throws AppException {
        String name = cloudNetWork.getNetName();
        //网络数据
        JSONObject net = new JSONObject();          
        net.put("name", name);
        //用于提交的完整数据 1509101341180
        JSONObject data = new JSONObject();
        data.put("network", net); 
        
        Network network=openstackNetworkService.update(cloudNetWork.getDcId(), cloudNetWork.getPrjId(), data, cloudNetWork.getNetId());
        if(network!=null){
            BaseCloudNetwork beasCloudNetwork =cloudNetworkDao.findOne(cloudNetWork.getNetId());
            beasCloudNetwork.setNetName(network.getName());
            cloudNetworkDao.saveOrUpdate(beasCloudNetwork);
            BeanUtils.copyPropertiesByModel(cloudNetWork, beasCloudNetwork);
            CloudRoute cloudRoute = this.findRouteByNetId(cloudNetWork.getNetId());
            if(cloudRoute!=null && !StringUtils.isEmpty(cloudRoute.getRouteId()) ){
            	this.updateRoute(cloudRoute.getRouteId(), cloudNetWork.getRate());
            }
        }   
        return cloudNetWork;
    }
    private void updateRoute(String routeId,int rate){
		BaseCloudRoute baseRoute = cloudRouteDao.findOne(routeId);
		if(rate==baseRoute.getRate()){
			return;
		}
		baseRoute.setRate(rate);
		if(!StringUtils.isEmpty(baseRoute.getQosId())){
			eayunQosService.updateQos(baseRoute,rate);
		}
		cloudRouteDao.saveOrUpdate(baseRoute);
    }
    
    public CloudNetWork findNetWorkByNetId(String netId) {
        BaseCloudNetwork baseNetWork = cloudNetworkDao.findOne(netId);
        CloudNetWork cloudNetWork = new CloudNetWork();
        BeanUtils.copyPropertiesByModel(cloudNetWork, baseNetWork);
        CloudProject cloudPrj = cloudNetworkDao.findCloudProjectByPrjId(baseNetWork.getPrjId());
        cloudNetWork.setPrjName(cloudPrj.getPrjName());
        cloudNetWork.setPrjQuotaSubNet(cloudPrj.getSubnetCount());
        cloudNetWork.setDcName(cloudPrj.getDcName());
        cloudNetWork.setNetStatusName(DictUtil.getStatusByNodeEn("netWork", cloudNetWork.getNetStatus()));
        CloudRoute cloudRoute = this.findRouteByNetId(netId);
        cloudNetWork.setRate(cloudRoute.getRate());
        cloudNetWork.setExtNetName(cloudRoute.getNetName());//外网名称
        return cloudNetWork;
    }
    
    private CloudRoute findRouteByNetId(String netId){
    	StringBuffer strb = new StringBuffer();
    	strb.append("select cr.rate,cn.net_name,cn.net_id,cr.route_id from cloud_route cr left outer join cloud_network cn on cn.net_id=cr.net_id where cr.network_id=?");
    	
    	javax.persistence.Query query =cloudRouteDao.createSQLNativeQuery(strb.toString(), netId);
    	CloudRoute cloudRoute = new CloudRoute();
    	if(query.getResultList()!=null && query.getResultList().size()>0){//路由跟网络为1对1
    		Object[] objs = (Object[]) query.getResultList().get(0);
    		cloudRoute.setRate(Integer.valueOf(ObjectUtils.toString(objs[0])));
			cloudRoute.setNetName(objs[1]==null ?null:ObjectUtils.toString(objs[1]));
			cloudRoute.setNetId(ObjectUtils.toString(objs[2]));
			cloudRoute.setRouteId(ObjectUtils.toString(objs[3]));
    	}
    	return cloudRoute;
    }

	public EayunResponseJson checkForDel(String networkId) throws AppException {
		EayunResponseJson json = new EayunResponseJson();
		int count = cloudSubNetWorkDao.countByNetId(networkId);
		if (count > 0) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			json.setMessage("该网络下已有子网，请先删除子网");
		}
		else{
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		}
		return json;
	}

	public boolean deleteNetwork(CloudNetWork cloudNetWork) throws AppException {
		/**
    	 * 判断资源是否有未完成的订单 --@author zhouhaitao
    	 */
    	if(checkNetworkOrderExist(cloudNetWork.getNetId())){
    		throw new AppException("该资源有未完成的订单，请取消订单后再进行删除操作！");
    	}
    	
		boolean result = openstackNetworkService.delete(cloudNetWork.getDcId(), cloudNetWork.getPrjId(), cloudNetWork.getNetId());
		if (result) {
			cloudNetworkDao.delete(cloudNetWork.getNetId());
			//删除子网
			cloudSubNetWorkDao.deleteByNetId(cloudNetWork.getNetId());
            //删除资源后更新缓存接口
			tagService.refreshCacheAftDelRes("network", cloudNetWork.getNetId());
			//查询关联的路由
			List<BaseCloudRoute> routeList = cloudRouteDao.findByNetWorkId(cloudNetWork.getNetId());
			if(CollectionUtils.isNotEmpty(routeList)){
				for (BaseCloudRoute baseCloudRoute : routeList) {
					//删除路由底层
					if(openstackRouterService.delete(baseCloudRoute.getDcId(), baseCloudRoute.getPrjId(), baseCloudRoute.getRouteId())){
						cloudRouteDao.delete(baseCloudRoute.getRouteId());
					}
				}
			}
			String cusId = this.getCusIdBuyPrjId(cloudNetWork.getPrjId());
			cloudNetWork.setCusId(cusId);
			this.networkDeleteHandle(cloudNetWork);
		}
		return result;
	}
	/**
     * 删除私有网络后的业务处理--发送消息
     * @author gaoxiang
     * @param cloudNetWork
     * @param deleteStep
     */
    private void networkDeleteHandle(CloudNetWork cloudNetWork) {
        if (PayType.PAYAFTER.equals(cloudNetWork.getPayType())) {
            /* 调用消息队列发送接口 */
            String key = "BILL_RESOURCE_DELETE";
            ChargeRecord chargeRecord = new ChargeRecord();
            chargeRecord.setDatecenterId(cloudNetWork.getDcId());
            chargeRecord.setResourceId(cloudNetWork.getNetId());
            chargeRecord.setResourceName(cloudNetWork.getNetName());
            chargeRecord.setResourceType(ResourceType.NETWORK);
            chargeRecord.setCusId(cloudNetWork.getCusId());
            chargeRecord.setOpTime(new Date());
            rabbitTemplate.send(key, JSONObject.toJSONString(chargeRecord));
        }
    }
	public CloudNetWork getNetworkById(String id) throws AppException {
		BaseCloudNetwork baseNetWork = cloudNetworkDao.findOne(id);
        CloudNetWork cloudNetWork = new CloudNetWork();
        BeanUtils.copyPropertiesByModel(cloudNetWork, baseNetWork);
        CloudProject cloudPrj = cloudNetworkDao.findCloudProjectByPrjId(baseNetWork.getPrjId());
        cloudNetWork.setPrjName(cloudPrj.getPrjName());
        cloudNetWork.setPrjQuotaSubNet(cloudPrj.getSubnetCount());
        cloudNetWork.setDcName(cloudPrj.getDcName());
        if ("0".equals(cloudNetWork.getChargeState())) {
            cloudNetWork.setNetStatusName(DictUtil.getStatusByNodeEn("netWork", cloudNetWork.getNetStatus()));
        } else {
            cloudNetWork.setNetStatusName(CloudResourceUtil.escapseChargeState(cloudNetWork.getChargeState()));
        }
        CloudRoute cloudRoute = this.findRouteByNetId(id);
        cloudNetWork.setRate(cloudRoute.getRate());
        cloudNetWork.setExtNetName(cloudRoute.getNetName());//外网名称
        cloudNetWork.setPayTypeStr(CloudResourceUtil.escapePayType(cloudNetWork.getPayType()));
        return cloudNetWork;
	}

	@Override
	public int getCountByPrjId(String prjId) {
		int netCount =cloudNetworkDao.getCountByPrjId(prjId);
        int orderCount = getNetworkCountInOrder(prjId);
        return netCount + orderCount;
	}
	/**
     * 获取订单状态为待创建或者创建中的资源个数
     * @author gaoxiang
     * @param prjId
     * @return
     */
    private int getNetworkCountInOrder(String prjId) {
        StringBuffer hql = new StringBuffer();
        hql.append("select ");
        hql.append("   count(*) ");
        hql.append("from ");
        hql.append("   order_info ");
        hql.append("left join ");
        hql.append("   cloudorder_network network ");
        hql.append("on ");
        hql.append("   order_info.order_no = network.order_no ");
        hql.append("where ");
        hql.append("   order_info.order_type = 0 ");
        hql.append("   and order_info.resource_type = 3 ");
        hql.append("   and (order_info.order_state = 1 or order_info.order_state = 2)");
        hql.append("   and network.prj_id = ?");
        Query query = cloudNetworkDao.createSQLNativeQuery(hql.toString(), prjId);
        Object result = query.getSingleResult();
        int orderCount = result == null ? 0 : Integer.parseInt(result.toString());
        return orderCount;
    }
	
	public List<CloudNetWork> getNotBindRouteNetworkByPrjId(String prjId) throws AppException {
		if(StringUtils.isBlank(prjId)){
			throw new AppException("请选择项目");
		}
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT cn.net_id as netId ,cn.net_name as netName,cn.prj_id as prjId , cn.dc_id as dcId , net_status as netStatus , cn.admin_stateup as adminStateup");
        sql.append(" FROM cloud_network cn LEFT JOIN cloud_route cr ON cn.net_id = cr.network_id WHERE cr.network_id IS NULL and cn.router_external = '0' ");
        sql.append(" AND cn.prj_id = ?");
        @SuppressWarnings("unchecked")
		List<Object[]> queryList = cloudNetworkDao.createSQLNativeQuery(sql.toString(), new Object[]{prjId}).getResultList();
        List<CloudNetWork> resultList =  new ArrayList<CloudNetWork>(0);
        if(CollectionUtils.isNotEmpty(queryList)){
        	resultList = new ArrayList<CloudNetWork>(queryList.size());
        	for (Object[] objs : queryList) {
        		CloudNetWork network = new CloudNetWork();
        		network.setNetId(ObjectUtils.toString(objs[0]));
        		network.setNetName(ObjectUtils.toString(objs[1]));
        		network.setPrjId(ObjectUtils.toString(objs[2]));
        		network.setDcId(ObjectUtils.toString(objs[3]));
        		network.setNetStatus(ObjectUtils.toString(objs[4]));
        		network.setAdminStateup(ObjectUtils.toString(objs[5]));
        		resultList.add(network);
			}
        }
        return resultList;
    }
	
	@SuppressWarnings("unchecked")
	public List<CloudNetWork> getNetworkListByPrjId(String prjId) throws AppException {
		StringBuffer hql = new StringBuffer();
        hql.append("from  BaseCloudNetwork where prjId= ? and chargeState ='0'");
		List<CloudNetWork> list = cloudNetworkDao.find(hql.toString(), new Object[]{prjId});
        return list;
	}
	 private String dateToStr(Date date){
	    	DateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	    	String dateStr="";
	    	if(date!=null){
	    		dateStr = sdf.format(date);
	    	}
	    	return dateStr;
	    }
	 /**
	  * 根据项目ID获取客户ID
	  * @param prjId
	  * @return
	  */
	private String getCusIdBuyPrjId(String prjId){
		StringBuffer hql = new StringBuffer();
        hql.append("select ");
        hql.append("   customer_id ");
        hql.append("from ");
        hql.append("   cloud_project ");
        hql.append("where ");
        hql.append("   prj_id = ?");
        Query query = cloudNetworkDao.createSQLNativeQuery(hql.toString(), prjId);
        Object result = query.getSingleResult();
        String cusId = result == null ? "" : String.valueOf(result.toString());
		return cusId;
	}
}

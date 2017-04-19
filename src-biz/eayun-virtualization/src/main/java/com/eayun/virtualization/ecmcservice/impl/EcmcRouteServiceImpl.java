/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.virtualization.ecmcservice.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.tools.DictUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.CloudResourceUtil;
import com.eayun.common.util.DateUtil;
import com.eayun.database.instance.service.EcmcCloudRDSInstanceService;
import com.eayun.eayunstack.model.Route;
import com.eayun.eayunstack.service.OpenstackRouterService;
import com.eayun.virtualization.dao.CloudRouteDao;
import com.eayun.virtualization.dao.CloudSubNetWorkDao;
import com.eayun.virtualization.dao.CloudVpnDao;
import com.eayun.virtualization.dao.PortMappingDao;
import com.eayun.virtualization.ecmcservice.EcmcRouteService;
import com.eayun.virtualization.model.BaseCloudRoute;
import com.eayun.virtualization.model.BaseCloudSubNetWork;
import com.eayun.virtualization.model.CloudRoute;
import com.eayun.virtualization.model.CloudSubNetWork;
import com.eayun.virtualization.service.EayunQosService;
import com.eayun.virtualization.service.TagService;

/**
 *                       
 * @Filename: EcmcRouteServiceImpl.java
 * @Description: 
 * @Version: 1.0
 * @Author: fangjun.yang
 * @Email: fangjun.yang@eayun.com
 * @History:<br>
 *<li>Date: 2016年4月6日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Service
@Transactional
public class EcmcRouteServiceImpl implements EcmcRouteService {

    private final static Logger    log = LoggerFactory.getLogger(EcmcRouteServiceImpl.class);

    @Autowired
    private CloudRouteDao          cloudRouteDao;

    @Autowired
    private CloudSubNetWorkDao     cloudSubNetWorkDao;

    @Autowired
    private OpenstackRouterService openstackRouterService;

    @Autowired
    private EayunQosService        eayunQosService;

    @Autowired
    private TagService             tagService;
    
    @Autowired
    private PortMappingDao         portMappingDao;
    
    @Autowired
    private CloudVpnDao            cloudVpnDao;
    
    @Autowired
    private EcmcCloudRDSInstanceService instanceService;

    public boolean checkRouteName(String datacenterId, String projectId, String routeName, String routeId) throws AppException {
        return cloudRouteDao.countByRouteName(datacenterId, projectId, StringUtils.trim(routeName), StringUtils.trim(routeId)) > 0 ? true : false;
    }

    public Map<String, Object> getRouteRateInfo(String prjId) {
        if (StringUtils.isBlank(prjId)) {
            throw new AppException("error.globe.system");
        }
        return cloudRouteDao.findRateInfo(prjId);
    }

    public Page queryRoute(String datacenterId, String prjName, String cusOrg, String name, QueryMap  queryMap) throws AppException {
    	try {
			int index=0;
			Object [] args=new Object[3];
			StringBuffer sql=new StringBuffer();
			sql.append("select cr.route_name as routeName,count(cs.subnet_id) as subnetCount,cr.route_status as routeStatus,cr.net_id as netId,cp.prj_name as prjName,");
			sql.append(" cr.route_id as routeId,cr.prj_id as prjId,cr.dc_id as dcId,dc.dc_name as dcName,cr.create_time as createTime,cr.rate , cn.net_name as networkName , cr.network_id as netWorkId,out_net.net_name as netName,");
			sql.append(" c.cus_id cusId, c.cus_org cusOrg,cr.gateway_ip");
			sql.append(" ,cn.charge_state ");
			sql.append(" from cloud_route cr");
			sql.append(" left join dc_datacenter dc on cr.dc_id=dc.id");
			sql.append(" left join cloud_project cp on cr.prj_id=cp.prj_id");
			sql.append(" left join cloud_network cn on cr.network_id=cn.net_id");    //所属内部网络
			sql.append(" left join cloud_network out_net on cr.net_id=out_net.net_id");    //所属外部网络
			sql.append(" left join cloud_subnetwork cs on cs.route_id=cr.route_id");
			sql.append(" left join sys_selfcustomer c on c.cus_id = cp.customer_id");
			sql.append(" where 1=1");
			
			if (!"null".equals(datacenterId)&&null!=datacenterId&&!"".equals(datacenterId)&&!"undefined".equals(datacenterId)) {
				sql.append(" and cr.dc_id= ?").append(index+1);
				args[index]=datacenterId;
				index++;
			}
			
			if (!"null".equals(prjName)&&null!=prjName&&!"".equals(prjName)&&!"undefined".equals(prjName)) {
				sql.append(" and cp.prj_name in(?").append(index+1).append(")");
				args[index]=  Arrays.asList(StringUtils.split(prjName, ","));
				index++;
			}
			
			if(StringUtils.isNotBlank(cusOrg)){
				sql.append(" and c.cus_org in(?").append(index+1).append(")");
				args[index]=  Arrays.asList(StringUtils.split(cusOrg, ","));
				index++;
			}
			if(!"null".equals(name)&&null!=name&&!"".equals(name)&&!"undefined".equals(name)){
		    	sql.append(" and cr.route_name like ?").append(index+1);
		    	name=name.replaceAll("\\_", "\\\\_");
		    	args[index]="%"+name+"%";
				index++;
			}
			
			sql.append(" group by cr.route_id ");
			sql.append(" order by cr.dc_id , cr.prj_id , cr.create_time desc ");
			Object[] params = new Object[index]; 
			System.arraycopy(args, 0, params, 0, index);
			Page page = cloudRouteDao.pagedNativeQuery(sql.toString(), queryMap, params);
			@SuppressWarnings("unchecked")
			List<Object> resultList = (List<Object>) page.getResult();
			for (int i = 0; i < resultList.size(); i++) {
				Object[] objs = (Object[]) resultList.get(i);
				CloudRoute cloudRoute = new CloudRoute();
				cloudRoute.setRouteName(ObjectUtils.toString(objs[0]));
				cloudRoute.setConnectsubnetnum(ObjectUtils.toString(objs[1]));
				cloudRoute.setRouteStatus(ObjectUtils.toString(objs[2]));
				cloudRoute.setNetId(ObjectUtils.toString(objs[3]));
				cloudRoute.setPrjName(ObjectUtils.toString(objs[4]));
				cloudRoute.setRouteId(ObjectUtils.toString(objs[5]));
				cloudRoute.setPrjId(ObjectUtils.toString(objs[6]));
				cloudRoute.setDcId(ObjectUtils.toString(objs[7]));
				cloudRoute.setDcName(ObjectUtils.toString(objs[8]));
				cloudRoute.setCreateTime(DateUtil.stringToDate(objs[9] == null ? "" : ObjectUtils.toString(objs[9])));
				cloudRoute.setRate((Integer)objs[10]);
				cloudRoute.setNetworkName(ObjectUtils.toString(objs[11]));
				cloudRoute.setNetWorkId(ObjectUtils.toString(objs[12]));
				cloudRoute.setNetName(ObjectUtils.toString(objs[13]));
				cloudRoute.setCusId(ObjectUtils.toString(objs[14]));
				cloudRoute.setCusOrg(ObjectUtils.toString(objs[15]));
				cloudRoute.setGatewayIp(ObjectUtils.toString(objs[16]));
				cloudRoute.setChargeState(ObjectUtils.toString(objs[17]));
				if(cloudRoute.getChargeState().equals("0")){
					cloudRoute.setStatusForRoute(DictUtil.getStatusByNodeEn("route", cloudRoute.getRouteStatus()));
				}else {
					cloudRoute.setStatusForRoute(CloudResourceUtil.escapseChargeState(cloudRoute.getChargeState()));
				}
				cloudRoute.setIsDeleting("not");
				resultList.set(i, cloudRoute);
			}
			return page;
		}catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
    }

    public void addRoute(CloudRoute cloudRoute) throws AppException {
        //网络数据
        JSONObject net = new JSONObject();
        net.put("name", cloudRoute.getRouteName());
        //用于提交的完整数据
        JSONObject resultData = new JSONObject();
        resultData.put("router", net);
        try {
            //创建网络
            Route route = openstackRouterService.create(cloudRoute.getDcId(), cloudRoute.getPrjId(), resultData);
            //openstack平台创建成功后，新建CloudRoute实例对象，并保持到数据库
            if (route != null) {
                BaseCloudRoute baseCoudRoute = new BaseCloudRoute();
                baseCoudRoute.setRouteId(route.getId());
                baseCoudRoute.setRouteName(route.getName());
                baseCoudRoute.setRouteStatus(route.getStatus());
                baseCoudRoute.setCreateName(cloudRoute.getCreateName());
                baseCoudRoute.setPrjId(route.getTenant_id());
                baseCoudRoute.setDcId(cloudRoute.getDcId());
                baseCoudRoute.setNetWorkId(cloudRoute.getNetWorkId());
                baseCoudRoute.setCreateTime(new Date());
                baseCoudRoute.setRate(cloudRoute.getRate());
                cloudRouteDao.save(baseCoudRoute);
                cloudRoute.setRouteId(baseCoudRoute.getRouteId());
            } else {
                throw new AppException("error.globe.system");
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException("error.globe.system", e);
        }
    }

    public void updateRoute(CloudRoute cloudRoute) throws AppException {
        BaseCloudRoute baseCloudRoute = new BaseCloudRoute();
        JSONObject net = new JSONObject();
        net.put("name", cloudRoute.getRouteName());
        //用于提交的完整数据
        JSONObject resultData = new JSONObject();
        resultData.put("router", net);
        try {
            //创建网络
            Route route = openstackRouterService.update(cloudRoute.getDcId(), cloudRoute.getPrjId(), resultData, cloudRoute.getRouteId());
            if (route != null) {
                baseCloudRoute = cloudRouteDao.findOne(cloudRoute.getRouteId());
                if (baseCloudRoute != null) {
                    baseCloudRoute.setRouteName(cloudRoute.getRouteName());
                    int perRate = baseCloudRoute.getRate();
                    baseCloudRoute.setRate(cloudRoute.getRate());
                    if (!StringUtils.isEmpty(baseCloudRoute.getQosId())) {
                        eayunQosService.updateQos(baseCloudRoute, perRate);
                    }
                    cloudRouteDao.saveOrUpdate(baseCloudRoute);
                }
            } else {
                throw new AppException("error.globe.system");
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException("error.globe.system", e);
        }
    }

    public void setGateWay(String routeId, String netId, String dcId) throws AppException {
        try {
            //设置网关
            Route route = openstackRouterService.setGateway(dcId, routeId, netId);
            if (route != null && null != route.getExternal_gateway_info().getNetwork_id()) {
                BaseCloudRoute baseCloudRoute = cloudRouteDao.findOne(routeId);
                baseCloudRoute.setNetId(route.getExternal_gateway_info().getNetwork_id());
                baseCloudRoute.setGatewayIp(route.getIp_address());
                cloudRouteDao.saveOrUpdate(baseCloudRoute);
            } else {
                throw new AppException("error.globe.system");
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException("error.globe.system", e);
        }
    }

    public BaseCloudSubNetWork attachSubnet(String routeId, String datacenterId, String subNetworkId) throws AppException {
        try {
            //底层连接子网成功后执行if
            Route route = openstackRouterService.attachInterface(datacenterId, routeId, subNetworkId);
            if (route != null) {
                //如果路由连接子网未成功，直接throw AppException 不再操作本地DB
                BaseCloudRoute baseCloudRoute = cloudRouteDao.findOne(routeId);
                if (StringUtils.isEmpty(baseCloudRoute.getQosId())) {
                    eayunQosService.createQos(baseCloudRoute);
                    cloudRouteDao.saveOrUpdate(baseCloudRoute);
                }
                BaseCloudSubNetWork subnet = cloudSubNetWorkDao.findOne(subNetworkId);
                subnet.setRouteId(routeId);
                cloudSubNetWorkDao.saveOrUpdate(subnet);
                return subnet;
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public void removeGateway(String routeId, String datacenterId) throws AppException {
        try {
            Route route = openstackRouterService.removeGateway(datacenterId, routeId);
            if (route != null) {
                BaseCloudRoute baseCloudRoute = cloudRouteDao.findOne(routeId);
                baseCloudRoute.setNetId(null);
                baseCloudRoute.setGatewayIp(null);
                cloudRouteDao.saveOrUpdate(baseCloudRoute);
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException("error.globe.system", e);
        }
    }

    public void detachSubnet(String routeId, String subNetworkId, String datacenterId) throws AppException {
        try {
            //底层解绑子网需要用到下面3个参数
            Route route = openstackRouterService.detachInterface(datacenterId, routeId, subNetworkId);
            if (route != null) {
                //根据子网id 查处绑定了改路由的子网，然后置空子网的routeId
                BaseCloudSubNetWork baseCloudSubNetwork = cloudSubNetWorkDao.findOne(subNetworkId);
                if (baseCloudSubNetwork != null && null != baseCloudSubNetwork.getRouteId() && !"".equals(baseCloudSubNetwork.getRouteId())) {
                    baseCloudSubNetwork.setRouteId(null);
                }
                cloudSubNetWorkDao.saveOrUpdate(baseCloudSubNetwork);
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException("error.globe.system", e);
        }
    }

    public boolean deleteRoute(String datacenterId, String id) throws AppException {
        try {
            if (openstackRouterService.delete(datacenterId, null, id)) {
                cloudRouteDao.delete(id);
                //删除资源后更新缓存接口
                tagService.refreshCacheAftDelRes("route", id);
                return true;
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }
    
    public CloudRoute findRouteDetailById(String routeId) throws AppException{
        if(StringUtils.isBlank(routeId)){
            throw new AppException("error.globe.system");
        }
        //本地DB获取指定id的路由信息
        CloudRoute cloudRoute =new CloudRoute();
        Map<String, Object> objMap = cloudRouteDao.findRouteDetail(routeId);
        BeanUtils.mapToBean(cloudRoute, objMap);
        StringBuffer sql = new StringBuffer();
        sql.append(" select charge_state");
        sql.append(" from cloud_network");
        sql.append(" where net_id = ? ");
        Query query = cloudRouteDao.createSQLNativeQuery(sql.toString(), cloudRoute.getNetWorkId());
        Object result = query.getSingleResult();
        String chargeState = result == null ? "" : result.toString();
        cloudRoute.setChargeState(chargeState);
        if(cloudRoute.getChargeState().equals("0")){
			cloudRoute.setStatusForRoute(DictUtil.getStatusByNodeEn("route", cloudRoute.getRouteStatus()));
		}else {
			cloudRoute.setStatusForRoute(CloudResourceUtil.escapseChargeState(cloudRoute.getChargeState()));
		}
        cloudRoute.setConnectsubnetnum(String.valueOf(cloudRouteDao.getSubNetworkCountByRouteId(cloudRoute.getRouteId())));
        return cloudRoute;
    }
    
    public List<Map<String, Object>> getSubnetList(String datacenterId, String routeId) throws AppException{
        return cloudSubNetWorkDao.findRouteSubNetwork(datacenterId, routeId);
    }
    
	@Override
	public int getQosNumByPrjId(String prjId) {
		String sum = cloudRouteDao.getQosNumByPrjId(prjId);
		int created = 0;
		if (sum != null) {
			created = Integer.valueOf(sum);
		}
		int toBeCreated = getQosNumInOrder(prjId) + getQosNumUpgradedInOrder(prjId);
		return created + toBeCreated;
	}
	
	/**
	 * 获取订单状态为待创建或者创建中的资源的带宽总数
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
        Query query = cloudRouteDao.createSQLNativeQuery(sql.toString(), prjId);
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
        Query query = cloudRouteDao.createSQLNativeQuery(sql.toString(), prjId);
        Object result = query.getSingleResult();
        int orderCount = result == null ? 0 : Integer.parseInt(result.toString());
        return orderCount;
	}
	@Override
	public int getCountByPrjId(String prjId) {
		int routeCount = cloudRouteDao.getCountByPrjId(prjId);
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
        Query query = cloudRouteDao.createSQLNativeQuery(sql.toString(), prjId);
        Object result = query.getSingleResult();
        int orderCount = result == null ? 0 : Integer.parseInt(result.toString());
        return orderCount;
	}

	@Override
	public Page getSubnetList(Page page, String dcId, String routeId, QueryMap queryMap) {
		StringBuffer sql = new StringBuffer();
        sql.append(" select ");
        sql.append("	cs.subnet_id");
        sql.append("	,cs.subnet_name");
        sql.append("	,cs.cidr");
        sql.append("	,cs.ip_version");
        sql.append("	,cs.gateway_ip");
        sql.append("	,cn.net_name");
        sql.append("	,cs.route_id");
        sql.append(" FROM ");
        sql.append("        cloud_route cr");
        sql.append(" LEFT OUTER JOIN ");
        sql.append("        cloud_network cn");
        sql.append("        ON cr.network_id = cn.net_id");
        sql.append(" LEFT OUTER JOIN ");
        sql.append("        cloud_subnetwork cs");
        sql.append("        ON cs.net_id = cn.net_id");
        sql.append(" where ");
        sql.append("	1=1 ");
        sql.append("	and cs.subnet_type='1'");
        sql.append("	and cr.dc_id = ?");
        sql.append("	and cr.route_id = ?");
        List<String> params = new ArrayList<String>();
        params.add(dcId);
        params.add(routeId);
        page = cloudRouteDao.pagedNativeQuery(sql.toString(), queryMap,
                params.toArray());
        List result = (List) page.getResult();
        for (int i = 0; i < result.size(); i++) {
            Object[] objs = (Object[]) result.get(i);
            CloudSubNetWork subNetwork = new CloudSubNetWork();
            int index = 0;
            subNetwork.setSubnetId(String.valueOf(objs[index++]));
            subNetwork.setSubnetName(String.valueOf(objs[index++]));
            subNetwork.setCidr(String.valueOf(objs[index++]));
            subNetwork.setIpVersion(String.valueOf(objs[index++]));
            subNetwork.setGatewayIp(String.valueOf(objs[index++]));
            subNetwork.setNetName(String.valueOf(objs[index++]));
            subNetwork.setRouteId(String.valueOf(objs[index++]));
            result.set(i, subNetwork);
        }
        return page;
	}

	@Override
	public EayunResponseJson checkForCle(String routeId) {
		EayunResponseJson json = new EayunResponseJson();
		//创建的端口映射的数量
		int countPortMapping = portMappingDao.getCountByRouteId(routeId);
		//创建的VPN的数量
		int countVpn = cloudVpnDao.getCountByRouteId(routeId);
		//有待创建的云主机或VPN占用了该网关的数量
		int countVmBuyFloatIp = this.getVmCountBuyFloatIpByNetId(routeId);
		//网关已被云主机或负载均衡占用的数量
		int countExistResourceOccupyNet = this.getExistResourceOccupyNetByNetId(routeId);
		
		BaseCloudRoute router = cloudRouteDao.findOne(routeId);
		//网关已被云数据库实例占用的数量
        int countRdsInstance = instanceService.getRdsInstanceCountByNetId(router.getNetWorkId());
        //有待创建的云数据库实例占用的数量
        int countRdsInstanceToBeCreated = instanceService.getRdsInstanceToBeCreatedByNetId(router.getNetWorkId());
		if (countPortMapping + countVpn > 0) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			json.setMessage("网关已被端口映射或VPN占用，请删除后操作!");
		} else if (countVmBuyFloatIp > 0) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			json.setMessage("有待创建的云主机或VPN占用了该网关，无法清除!");
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
	private int getVmCountBuyFloatIpByNetId(String routeId) {
		StringBuffer sqlBuf = new StringBuffer();
		sqlBuf.append("   select  ");
		sqlBuf.append("   vm.vm_count ");
		sqlBuf.append("   ,vpn.vpn_count  ");
		sqlBuf.append("   from  ");
		sqlBuf.append("   ( ");
		sqlBuf.append("   select count(*) as vm_count ");
		sqlBuf.append("   from cloud_route cr  ");
		sqlBuf.append("   left join cloudorder_vm cov on cr.network_id = cov.net_id   ");
		sqlBuf.append("   left join order_info oi on cov.order_no = oi.order_no   ");
		sqlBuf.append("   where oi.order_type = '0'  ");
		sqlBuf.append("   and (oi.order_state = '1' or oi.order_state = '2')   ");
		sqlBuf.append("   and cov.buy_floatip = '1'   ");
		sqlBuf.append("   and cr.route_id = ? ) as vm, ");
		sqlBuf.append("   ( ");
		sqlBuf.append("   select count(*) as vpn_count ");
		sqlBuf.append("   from cloudorder_vpn cov left join order_info oi on cov.order_no = oi.order_no ");
		sqlBuf.append("   where oi.order_type = '0' ");
		sqlBuf.append("   and (oi.order_state = '1' or oi.order_state = '2') ");
		sqlBuf.append("   and cov.route_id = ? ");
		sqlBuf.append("   ) as vpn ");
		Query query = cloudRouteDao.createSQLNativeQuery(sqlBuf.toString(), new Object[]{routeId, routeId});
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
		sqlBuf.append("  select vm.vm_count + ldpool_count as count  ");
		sqlBuf.append("  from   ");
		sqlBuf.append("  (  ");
		sqlBuf.append("  select count(*) as vm_count  ");
		sqlBuf.append("  from cloud_vm vm left join cloud_floatip flo on flo.resource_id = vm.vm_id ");
		sqlBuf.append("  left join cloud_route cr on vm.net_id = cr.network_id ");
		sqlBuf.append("  where flo.flo_ip != '' and flo.flo_ip is not null and vm.is_deleted = '0' and cr.route_id = ?  ");
		sqlBuf.append("  ) vm,  ");
		sqlBuf.append("  (  ");
		sqlBuf.append("  select count(*) as ldpool_count  ");
		sqlBuf.append("  from cloud_ldpool cl left join cloud_subnetwork cs on cs.subnet_id = cl.subnet_id  ");
		sqlBuf.append("  left join cloud_floatip flo on flo.resource_id = cl.pool_id ");
		sqlBuf.append("  where flo.flo_ip != '' and flo.flo_ip is not null and cs.route_id = ? ");
		sqlBuf.append("  ) ldpool  ");
		Query query = cloudRouteDao.createSQLNativeQuery(sqlBuf.toString(), new Object[]{netId, netId});
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
		} else if (pmOrVpnBindSubnetFlag) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			json.setMessage("断开路由失败，断开路由前需删除VPN服务或端口映射!");
		} else if (toBeCreateflag) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			json.setMessage("有待创建的云主机或VPN占用该受管子网，无法解绑!");
		} else if (countRdsInstance > 0) {
		    json.setRespCode(ConstantClazz.ERROR_CODE);
		    json.setMessage("断开路由失败，已有云数据库在使用!");
		} else if (countRdsInstanceToBeCreated > 0) {
		    json.setRespCode(ConstantClazz.ERROR_CODE);
		    json.setMessage("断开路由失败，有待创建的云数据库使用!");
		} else {
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
		Query query = cloudRouteDao.createSQLNativeQuery(sqlBuf.toString(), new Object[]{subnetId, subnetId});
        Object result = query.getSingleResult();
        int count = result == null ? 0 : Integer.parseInt(result.toString());
        if(count > 0)
        	flag = true;
		return flag;
	}
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
		Query query = cloudRouteDao.createSQLNativeQuery(sqlBuf.toString(), new Object[]{subnetId, subnetId});
        Object result = query.getSingleResult();
        int count = result == null ? 0 : Integer.parseInt(result.toString());
        if(count > 0)
        	flag = true;
		return flag;
	}
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
		Query query = cloudRouteDao.createSQLNativeQuery(sqlBuf.toString(), new Object[]{subnetId, subnetId});
        Object result = query.getSingleResult();
        int count = result == null ? 0 : Integer.parseInt(result.toString());
        if(count > 0)
        	flag = true;
		return flag;
	}
}
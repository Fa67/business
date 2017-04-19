/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.virtualization.ecmcservice.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.StringUtil;
import com.eayun.eayunstack.model.LabelRule;
import com.eayun.eayunstack.model.Poolresource;
import com.eayun.eayunstack.model.Route;
import com.eayun.eayunstack.model.SubNetwork;
import com.eayun.eayunstack.service.OpenstackMeterLabelService;
import com.eayun.eayunstack.service.OpenstackRouterService;
import com.eayun.eayunstack.service.OpenstackSubNetworkService;
import com.eayun.project.service.ProjectService;
import com.eayun.virtualization.dao.CloudLdPoolDao;
import com.eayun.virtualization.dao.CloudRouteDao;
import com.eayun.virtualization.dao.CloudSubNetWorkDao;
import com.eayun.virtualization.ecmcservice.EcmcSubNetworkService;
import com.eayun.virtualization.model.BaseCloudProject;
import com.eayun.virtualization.model.BaseCloudRoute;
import com.eayun.virtualization.model.BaseCloudSubNetWork;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.model.CloudRoute;
import com.eayun.virtualization.model.CloudSubNetWork;
import com.eayun.virtualization.model.EcmcCloudSubNetwork;
import com.eayun.virtualization.service.EayunQosService;
import com.eayun.virtualization.service.TagService;

/**
 *                       
 * @Filename: EcmcSubNetworkServiceImpl.java
 * @Description: 
 * @Version: 1.0
 * @Author: fangjun.yang
 * @Email: fangjun.yang@eayun.com
 * @History:<br>
 *<li>Date: 2016年4月1日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Service
@Transactional
public class EcmcSubNetworkServiceImpl implements EcmcSubNetworkService {

    private final static Logger        log = LoggerFactory.getLogger(EcmcSubNetworkServiceImpl.class);

    @Autowired
    private CloudSubNetWorkDao         cloudSubNetWorkDao;

    @Autowired
    private CloudLdPoolDao             cloudLdPoolDao;

    @Autowired
    private OpenstackSubNetworkService openstackSubNetworkService;
    
    @Autowired
	private OpenstackRouterService openstackRouteService;

    @Autowired
    private OpenstackMeterLabelService meterLabelService;

    @Autowired
    private TagService                 tagService;
    
    @Autowired
    private ProjectService             projectService;
    
    @Autowired
	private CloudRouteDao cloudRouteDao;
    
    @Autowired
	private EayunQosService eayunQosService;

    public List<Map<String, Object>> getSubNetworkList(String datacenterId, String networkid) throws AppException {
        try {
            return cloudSubNetWorkDao.findSubNetwork(datacenterId, networkid);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public CloudSubNetWork addSubNetwork(CloudSubNetWork cloudSubNetWork) throws AppException {
    	/*自定义App异常定位哨兵*/
    	int createStep = 0;
    	SubNetwork subNetWork = null;
    	LabelRule inLabelRule = null;
    	LabelRule outLabelRule = null;
    	CloudRoute checkRoute = new CloudRoute();
    	try {
    	    net.sf.json.JSONObject subNet = new net.sf.json.JSONObject();
    	    String dns [] = new String[]{cloudSubNetWork.getDns()};
    	    subNet.put("name", cloudSubNetWork.getSubnetName());
    	    subNet.put("network_id", cloudSubNetWork.getNetId());   
    	    subNet.put("ip_version", "4");
    	    subNet.put("dns_nameservers", dns);
    	    subNet.put("cidr", cloudSubNetWork.getCidr());
    	    subNet.put("tenant_id", cloudSubNetWork.getPrjId());
    	    if ("0".equals(cloudSubNetWork.getSubnetType())) {
    	        subNet.put("gateway_ip", "null");
    	    }
    	    net.sf.json.JSONObject data = new net.sf.json.JSONObject();
    	    data.put("subnet", subNet);
    	    //创建子网
    	    subNetWork = openstackSubNetworkService.create(cloudSubNetWork.getDcId(), cloudSubNetWork.getPrjId(), data);
    	    createStep++;
    	    BaseCloudProject project = projectService.findProject(cloudSubNetWork.getPrjId());
    	    /*判断项目是否含有labelId，如果两个都有，则创建对应的labelRule，否则不创建*/
    	    if (project.getLabelInId() != null && project.getLabelOutId() != null) {
    	        inLabelRule = createLabelRule(cloudSubNetWork.getCidr(), "ingress", project.getLabelInId(), project.getDcId());
    	        createStep++;
    	        outLabelRule = createLabelRule(cloudSubNetWork.getCidr(), "egress", project.getLabelOutId(), project.getDcId());
    	    } else {
    	        createStep++;
    	    }
    	    createStep++;
    	    
    	    if(subNetWork!=null){
    	        BaseCloudSubNetWork baseSubNet=new BaseCloudSubNetWork();
    	        cloudSubNetWork.setSubnetId(subNetWork.getId());
    	        cloudSubNetWork.setCreateTime(new Date());
    	        cloudSubNetWork.setGatewayIp(subNetWork.getGateway_ip());
    	        cloudSubNetWork.setIpVersion("4");
    	        cloudSubNetWork.setDns(cloudSubNetWork.getDns());
    	        if(subNetWork.getEnable_dhcp()==null){
    	            cloudSubNetWork.setIsForbiddengw("0");
    	        }else{
    	            cloudSubNetWork.setIsForbiddengw("1");
    	        }
    	        if(inLabelRule != null && outLabelRule != null) {
    	            cloudSubNetWork.setInLabelRuleId(inLabelRule.getId());
    	            cloudSubNetWork.setOutLabelRuleId(outLabelRule.getId());
    	        }
    	        Poolresource poolresource =subNetWork.getAllocation_pools()[0];
    	        cloudSubNetWork.setPooldata(poolresource.getStart()+","+poolresource.getEnd());
    	        BeanUtils.copyPropertiesByModel(baseSubNet, cloudSubNetWork);
    	        //创建子网
    	        cloudSubNetWorkDao.save(baseSubNet);
    	        //判断子网类型决定是否要连接路由
    	        if("1".equals(cloudSubNetWork.getSubnetType())) {
    	            //子网连接路由
    	            CloudRoute cloudRoute = this.findRouteByNetId(cloudSubNetWork.getNetId());
    	            if(null!=cloudRoute&&!StringUtils.isEmpty(cloudRoute.getRouteId())){
    	                baseSubNet= this.connectSubnet(cloudSubNetWork.getDcId(), cloudRoute.getRouteId(), cloudSubNetWork.getSubnetId(), checkRoute);
    	            }
    	        }
    	    }
    	} catch (AppException e) {
    	    log.error(e.getMessage(), e);
    	    exceptionHandleForCreateSub(createStep, cloudSubNetWork.getDcId(), cloudSubNetWork.getPrjId(), inLabelRule, outLabelRule, subNetWork, checkRoute);
    	    throw e;
    	}
        return cloudSubNetWork;
    }
    /**
     * 创建label-rule的私有接口
     * @param cidr
     * @param gress
     * @param labelId
     * @param dcId
     * @return
     * @throws AppException
     */
    private LabelRule createLabelRule (String cidr, String gress, String labelId, String dcId) throws AppException {
        JSONObject labelRuleData = new JSONObject();
        JSONObject labelRule = new JSONObject();
        labelRule.put("remote_ip_prefix", cidr);
        labelRule.put("direction", gress);
        labelRule.put("metering_label_id", labelId);
        labelRuleData.put("metering_label_rule", labelRule);
        LabelRule labelRuleResult = meterLabelService.create(dcId, labelRuleData);
        return labelRuleResult;
    }
    /**
     * 根据网络id查询路由
     * @param netId
     * @return
     */
    private CloudRoute findRouteByNetId(String netId){
    	StringBuffer strb = new StringBuffer();
    	strb.append("select cr.rate,cn.net_name,cn.net_id,cr.route_id from cloud_route cr left outer join cloud_network cn on cn.net_id=cr.network_id where cr.network_id=?");
    	javax.persistence.Query query = cloudRouteDao.createSQLNativeQuery(strb.toString(), netId);
    	CloudRoute cloudRoute = new CloudRoute();
    	if(query.getResultList()!=null && query.getResultList().size()>0){//路由跟网络为1对1
    		Object[] objs = (Object[]) query.getResultList().get(0);
    		cloudRoute.setRate(Integer.valueOf(String.valueOf(objs[0])));
			cloudRoute.setNetName(objs[1]==null ?null:String.valueOf(objs[1]));
			cloudRoute.setNetId(String.valueOf(objs[2]));
			cloudRoute.setRouteId(String.valueOf(objs[3]));
    	}
    	return cloudRoute;
    }
    /**
     * 子网绑定路由私有方法
     * @param dcId
     * @param routeId
     * @param subnetworkId
     * @return
     */
    private BaseCloudSubNetWork connectSubnet(String dcId,String routeId,String subnetworkId, CloudRoute check){		
		Route route = openstackRouteService.attachInterface(dcId, routeId, subnetworkId);
		if(route!=null){
		    check.setRouteId(routeId);
			BaseCloudRoute cloudRoute = cloudRouteDao.findOne(routeId);
			if(StringUtils.isEmpty(cloudRoute.getQosId())){
				eayunQosService.createQos(cloudRoute);
			}
			//如果路由连接子网未成功，直接throw AppException 不再操作本地DB
			BaseCloudSubNetWork subnet = this.getBaseSubNetworkById(subnetworkId);
			subnet.setRouteId(routeId);
			this.saveOrUpdate(subnet);
			cloudRouteDao.saveOrUpdate(cloudRoute);
			return subnet;
		}else{
			return null;
		}
    }
    /**
     * 处理创建子网流程当中的异常
     * @param createStep
     * @param dcId
     * @param prjId
     * @param in
     * @param out
     * @param subnet
     * @param checkRoute
     */
    private void exceptionHandleForCreateSub (int createStep, String dcId, String prjId, LabelRule in, LabelRule out, SubNetwork subnet, CloudRoute checkRoute) {
        /*异常点在创建子网的代码之后*/
        if (createStep > 0) {
            /*异常点在创建label-rule-in的代码之后*/
            if (createStep > 1) {
                /*异常点在创建label-rule-out的代码之后*/
                if (createStep > 2) {
                    /*判断是否底层绑定了路由*/
                    if (!StringUtil.isEmpty(checkRoute.getRouteId())) {
                        openstackRouteService.detachInterface(dcId, checkRoute.getRouteId(), subnet.getId());
                    }
                    /*判断是否创建了label-rule-out*/
                    if (null != out) {
                        meterLabelService.delete(dcId, out.getId());
                    }
                }
                /*判断是否创建了label-rule-in*/
                if (null != in) {
                    meterLabelService.delete(dcId, in.getId());
                }
            }
            /*判断是否创建了子网*/
            if (null != subnet) {
                openstackSubNetworkService.delete(dcId, prjId, subnet.getId());
            }
        }
    }
    
    public BaseCloudSubNetWork getBaseSubNetworkById(String subNetId){
		return cloudSubNetWorkDao.findOne(subNetId);
	}
    public void saveOrUpdate(BaseCloudSubNetWork subNetWork){
    	cloudSubNetWorkDao.saveOrUpdate(subNetWork);
	}
    public SubNetwork updateSubNetwork(CloudSubNetWork subNetWork) throws AppException {
        //网络数据
        JSONObject subnet = new JSONObject();
        subnet.put("name", subNetWork.getSubnetName());
        String dns [] = new String[]{};
        if(!StringUtils.isEmpty(subNetWork.getDns())){
        	dns = subNetWork.getDns().split(";");
        }
        subnet.put("dns_nameservers", dns);
        //用于提交的完整数据
        JSONObject data = new JSONObject();
        data.put("subnet", subnet);
        try {
            //修改子网络
            SubNetwork subnetwork = openstackSubNetworkService.update(subNetWork.getDcId(), subNetWork.getPrjId(), data, subNetWork.getSubnetId());
            if (subnetwork != null) {
                BaseCloudSubNetWork cloudsubNetwork = cloudSubNetWorkDao.findOne(subNetWork.getSubnetId());
                cloudsubNetwork.setSubnetName(subnetwork.getName());
                cloudsubNetwork.setGatewayIp(subnetwork.getGateway_ip());
                cloudsubNetwork.setDns(subNetWork.getDns());
                cloudSubNetWorkDao.saveOrUpdate(cloudsubNetwork);
                //设置networkvoe对象属性
                return subnetwork;
            } else {
                return null;
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            //出现未知异常时，返回空
            return null;
        }
    }

    public boolean deleteSubNetwork(String datacenterId, String id) throws AppException {
        BaseCloudSubNetWork cloudSubNetwork = cloudSubNetWorkDao.findOne(id);
        if (cloudLdPoolDao.countByNetId(cloudSubNetwork.getNetId()) > 0) {
            return false;
        }
        // XXX 标签删除成功与否的判断，如果删除失败了，是否需要对之后的子网删除做限制？
        boolean flag = openstackSubNetworkService.delete(datacenterId, null, id);
        if (flag) {
            if (cloudSubNetwork.getInLabelRuleId() != null && cloudSubNetwork.getOutLabelRuleId() != null) {
                meterLabelService.delete(datacenterId, cloudSubNetwork.getInLabelRuleId());
                meterLabelService.delete(datacenterId, cloudSubNetwork.getOutLabelRuleId());
            }
            tagService.refreshCacheAftDelRes("subNetwork", id);
            cloudSubNetWorkDao.delete(id);
        }
        return flag;
    }

    public EcmcCloudSubNetwork getSubNetworkById(String id) throws AppException {
        try {
            List<EcmcCloudSubNetwork> list = cloudSubNetWorkDao.findEcmcCloudSubNetwork(id);
            return CollectionUtils.isNotEmpty(list) ? list.get(0) : null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            //当执行出现未知异常时，打印异常信息，并返回空
            return null;
        }
    }

    public boolean checkSameSubNetIP(String subnetIP, String netId) throws AppException{
        return cloudSubNetWorkDao.countByNetIdAndCidr(netId, subnetIP) > 0 ? true : false;
    }

    public boolean checkSubNetWorkName(String datacenterId,String projectId,String subnetName,String subnetId) throws AppException{
        return cloudSubNetWorkDao.countBy(datacenterId, projectId, subnetName, subnetId) > 0 ? true : false;
    }

	@Override
	public int getCountByPrjId(String prjId) {
		return cloudSubNetWorkDao.getCountByPrjId(prjId);
	}

	@Override
	public List<EcmcCloudSubNetwork> getSubNetListByNetId(String netId) {
		log.info("查询某一网络下的子网集合");
		List<EcmcCloudSubNetwork> ecmcSubList = new ArrayList<EcmcCloudSubNetwork>();
		String hql = "select bcs from BaseCloudSubNetWork bcs,BaseCloudNetwork bcn where bcn.netId = bcs.netId and bcs.subnetType is not null and bcn.routerExternal ='0' and bcs.netId = ? ";
		@SuppressWarnings("unchecked")
		List<BaseCloudSubNetWork> subList = cloudSubNetWorkDao.find(hql, netId);
		for(BaseCloudSubNetWork baseSubNet : subList){
			EcmcCloudSubNetwork ecmcSubNet = new EcmcCloudSubNetwork();
			BeanUtils.copyPropertiesByModel(ecmcSubNet, baseSubNet);
			ecmcSubList.add(ecmcSubNet);
		}
		return ecmcSubList;
	}
	
	public List<CloudSubNetWork> getNotBindRouteSubnetList(String datacenterId,String projectId,String netWorkId) throws AppException {
		try {
			int index=0;
			Object [] args=new Object[3];
			StringBuffer sql=new StringBuffer();
			sql.append("select cs.subnet_name as subnetName,cs.cidr as cidr,cn.net_name as netName,");
			sql.append(" cs.subnet_id as subnetId");
			sql.append(" ,cs.subnet_type");
			sql.append(" from cloud_subnetwork cs");
			sql.append(" left join cloud_network cn on cs.net_id=cn.net_id");
			sql.append(" where cn.router_external= '0' ");
			sql.append(" and (cs.route_id is null or cs.route_id ='') ");
			
			if (StringUtils.isNotBlank(projectId)) {
				sql.append(" and cs.prj_id= ? ");
	            args[index]=projectId;
	            index++;
			}
			if (StringUtils.isNotBlank(datacenterId)) {
				sql.append(" and cs.dc_id= ? ");
	            args[index]=datacenterId;
	            index++;
			}
			if(StringUtils.isNotBlank(netWorkId)){
				sql.append(" and cs.net_id= ? ");
	            args[index]=netWorkId;
	            index++;
			}
			sql.append(" order by cs.create_time desc ");
			Object[] params = new Object[index]; 
			System.arraycopy(args, 0, params, 0, index);
			@SuppressWarnings("unchecked")
			List<Object[]> queryList = cloudSubNetWorkDao.createSQLNativeQuery(sql.toString(), params).getResultList();
			 List<CloudSubNetWork> resultList = new ArrayList<CloudSubNetWork>();
			 if(CollectionUtils.isNotEmpty(queryList)){
				 for (Object[] objs : queryList) {
					 CloudSubNetWork subnet = new CloudSubNetWork();
					 subnet.setSubnetName(ObjectUtils.toString(objs[0]));
					 subnet.setCidr(ObjectUtils.toString(objs[1]));
					 subnet.setNetName(ObjectUtils.toString(objs[2]));
					 subnet.setSubnetId(ObjectUtils.toString(objs[3]));
					 subnet.setSubnetType(ObjectUtils.toString(objs[4]));
					 resultList.add(subnet);
				}
			 }
			 return resultList;
		}catch (Exception e) {
			log.error(e.getMessage(),e);
			//当执行出现未知异常时，打印异常信息，并返回空
			return null;
		}
	}

	@Override
	public EayunResponseJson checkForDel(Map<String, String> map) {
		EayunResponseJson json = new EayunResponseJson();
		String subnetId = map.get("subnetId") == null ? "" : map.get("subnetId").toString();
		String subnetType = map.get("subnetType") == null ? "" : map.get("subnetType").toString();
		int subBindRouteCount = 0;
		int vmOccupySubnetCount = 0;
		int vmInOrderOccupySubnetCount = 0;
		int poolOccupySubnetCount = this.getPoolOccupySubnetCount(subnetId);
		if(subnetType != "" && subnetType.equals("1")){
			subBindRouteCount = this.getSubBindRouteCount(subnetId);
		}
		vmOccupySubnetCount = this.getVmOccupySubnetCount(subnetId, subnetType);
		vmInOrderOccupySubnetCount = this.getVmInOrderOccupySubnetCount(subnetId);
		if (subBindRouteCount > 0) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			json.setMessage("子网连接了路由，请断开后操作!");
		}else if(poolOccupySubnetCount > 0){
			json.setRespCode(ConstantClazz.ERROR_CODE);
			json.setMessage("有待创建的负载均衡占用该子网，不能删除!");
		}else if(vmOccupySubnetCount > 0){
			json.setRespCode(ConstantClazz.ERROR_CODE);
			json.setMessage("子网IP已被云主机或负载均衡占用，不能删除!");
		}else if (vmInOrderOccupySubnetCount > 0) {
		    json.setRespCode(ConstantClazz.ERROR_CODE);
		    json.setMessage("您有待创建的云主机占用该子网，无法删除!");
		}
		else{
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		}
		return json;
	}
	/**
	 * 判断受管子网是否绑定路由。count > 0 :已绑定，count = 0 : 未绑定。
	 * @param subnetId
	 * @return
	 */
	private int getSubBindRouteCount(String subnetId){
		StringBuffer sqlBuf = new StringBuffer();
		sqlBuf.append(" select count(*) ");
		sqlBuf.append(" from cloud_subnetwork ");
		sqlBuf.append(" where subnet_type = '1'  ");
		sqlBuf.append(" and (route_id is not null or route_id != '') ");
		sqlBuf.append(" and subnet_id = ? ");
		Query query = cloudSubNetWorkDao.createSQLNativeQuery(sqlBuf.toString(), subnetId);
        Object result = query.getSingleResult();
        int count = result == null ? 0 : Integer.parseInt(result.toString());
        return count;
	}
	/**
	 * 待创建的云主机占用自管
	 * @author gaoxiang
	 * @param subnetId
	 * @return
	 */
	private int getVmInOrderOccupySubnetCount(String subnetId) {
	    StringBuffer sqlBuf = new StringBuffer();
	    sqlBuf.append(" select count(*) as count ");
	    sqlBuf.append(" from cloudorder_vm vm left join order_info oi on vm.order_no = oi.order_no ");
	    sqlBuf.append(" where oi.order_type = '0' ");
	    sqlBuf.append(" and (oi.order_state = '1' or oi.order_state = '2') ");
	    sqlBuf.append(" and vm.self_subnetid = ? ");
	    Query query = cloudSubNetWorkDao.createSQLNativeQuery(sqlBuf.toString(), new Object[]{subnetId});
	    Object result = query.getSingleResult();
	    int count = result == null ? 0 : Integer.parseInt(result.toString());
	    return count;
	}
	/**
	 * 判断云主机或负载均衡是否占用该自管子网。count > 0 :占用，count = 0 : 未占用。
	 * @param subnetId
	 * @return
	 */
	private int getVmOccupySubnetCount(String subnetId, String subnetType) {
		StringBuffer sqlBuf = new StringBuffer();
		String filed = null;
		if("1".equals(subnetType)){
			filed = "subnet_id";
		} else {
			filed = "self_subnetid";
		}
		sqlBuf.append(" select vm.vm_count + ldpool_count as count ");
		sqlBuf.append(" from  ");
		sqlBuf.append(" ( ");
		sqlBuf.append(" select count(*) as vm_count ");
		sqlBuf.append(" from cloud_vm vm ");
		sqlBuf.append(" where vm.is_deleted != '1' and "+ filed +" = ? ");
		sqlBuf.append(" ) vm, ");
		sqlBuf.append(" ( ");
		sqlBuf.append(" select count(*) as ldpool_count ");
		sqlBuf.append(" from cloud_ldpool ");
		sqlBuf.append(" where subnet_id = ? ");
		sqlBuf.append(" ) ldpool ");
		Query query = cloudSubNetWorkDao.createSQLNativeQuery(sqlBuf.toString(), new Object[]{subnetId, subnetId});
        Object result = query.getSingleResult();
        int count = result == null ? 0 : Integer.parseInt(result.toString());
        return count;
	}
	/**
	 * 带创建的负载均衡占用该子网的数量
	 * @param subnetId
	 * @return
	 */
	private int getPoolOccupySubnetCount(String subnetId) {
		StringBuffer sqlBuf = new StringBuffer();
		sqlBuf.append("  select count(*) as count ");
		sqlBuf.append("  from cloudorder_ldpool col left join order_info oi on col.order_no = oi.order_no ");
		sqlBuf.append("  where oi.order_type = '0' ");
		sqlBuf.append("  and (oi.order_state = '1' or oi.order_state = '2') ");
		sqlBuf.append("  and col.subnet_id = ? ");
		Query query = cloudSubNetWorkDao.createSQLNativeQuery(sqlBuf.toString(), new Object[]{subnetId});
        Object result = query.getSingleResult();
        int count = result == null ? 0 : Integer.parseInt(result.toString());
        return count;
	}
}

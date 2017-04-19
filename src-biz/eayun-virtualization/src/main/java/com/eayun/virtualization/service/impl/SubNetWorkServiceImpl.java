package com.eayun.virtualization.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.eayun.virtualization.dao.CloudRouteDao;
import com.eayun.virtualization.dao.CloudSubNetWorkDao;
import com.eayun.virtualization.model.BaseCloudNetwork;
import com.eayun.virtualization.model.BaseCloudProject;
import com.eayun.virtualization.model.BaseCloudRoute;
import com.eayun.virtualization.model.BaseCloudSubNetWork;
import com.eayun.virtualization.model.CloudRoute;
import com.eayun.virtualization.model.CloudSubNetWork;
import com.eayun.virtualization.service.EayunQosService;
import com.eayun.virtualization.service.RouteService;
import com.eayun.virtualization.service.SubNetWorkService;
import com.eayun.virtualization.service.TagService;
/**
 * SubNetWorkServiceImpl
 * 
 * @Filename: SubNetWorkServiceImpl.java
 * @Description:
 * @Version: 1.0
 * @Author: liyanchao
 * @Email: yanchao.li@eayun.com
 * @History:<br> <li>Date: 2015年11月9日</li> <li>Version: 1.0</li> <li>Content:
 *               create</li>
 * 
 */
@Service
@Transactional
public class SubNetWorkServiceImpl implements SubNetWorkService{
	private static final Logger log = LoggerFactory.getLogger(SubNetWorkServiceImpl.class);
	
	@Autowired
	private CloudSubNetWorkDao subNetDao;
	@Autowired
	private OpenstackRouterService openstackRouteService;
	
	@Autowired
	private OpenstackSubNetworkService openSubNetService;
	@Autowired
	private TagService tagService;
	@Autowired
	private OpenstackMeterLabelService meterLabelService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private CloudRouteDao routeDao;
	@Autowired
	private EayunQosService eayunQosService;
	/**
	 * 获取相同项目下所有内部网络的子网络
	 * @param datacenterId  数据中心id
	 * @param projectId  项目id
	 * @return
	 */
	public List<CloudSubNetWork> getInnerNetList(String dcId,String prjId){
		
			List<CloudSubNetWork> subNetList =new ArrayList<CloudSubNetWork>();
			List<Object> listParams = new ArrayList<Object>();
			
			StringBuffer sql=new StringBuffer();
			sql.append("select cs.subnet_name as subnetName,cs.cidr as cidr,cn.net_name as netName,");
			sql.append(" cs.subnet_id as subnetId");
			sql.append(" from cloud_subnetwork cs");
			sql.append(" left join cloud_network cn on cs.net_id=cn.net_id");
			sql.append(" where cn.router_external= '0' ");
			sql.append(" and (cs.route_id is null or cs.route_id ='') ");
			
			if (!"null".equals(dcId)&&null!=dcId&&!"".equals(dcId)&&!"undefined".equals(dcId)) {
				sql.append(" and cs.dc_id= ? ");
				listParams.add(dcId);
			}
			if (!"null".equals(prjId)&&null!=prjId&&!"".equals(prjId)&&!"undefined".equals(prjId)) {
				sql.append(" and cs.prj_id= ? ");
				listParams.add(prjId);
			}
			
			sql.append(" order by cs.create_time desc ");
			
			javax.persistence.Query query =subNetDao.createSQLNativeQuery(sql.toString(), listParams.toArray());
			List listResult = query.getResultList();
			for (int i = 0; i < listResult.size(); i++) {
				Object[] objs = (Object[]) listResult.get(i);
				CloudSubNetWork subNet = new CloudSubNetWork();
				subNet.setSubnetName(String.valueOf(objs[0]));
				subNet.setCidr(String.valueOf(objs[1]));
				subNet.setNetName(String.valueOf(objs[2]));
				subNet.setSubnetId(String.valueOf(objs[3]));

				subNetList.add(subNet);
			}
			return subNetList;
		
	} 
	public BaseCloudSubNetWork getSubNetworkById(String subNetId){
		return subNetDao.findOne(subNetId);
	}
	public void saveOrUpdate(BaseCloudSubNetWork subNetWork){
		subNetDao.saveOrUpdate(subNetWork);
	}
	/**
	 * 获取路由绑定的子网
	 * @param datacenterId
	 * @param routeid
	 * @return
	 */
	public List<CloudSubNetWork> getSubnetList(String dcId,String prjId,String routeId){
		/*查询子网列表，根据route_id查询，左连杰网络表展示网络名称  */
		List<CloudSubNetWork> subnetList =new ArrayList<CloudSubNetWork>();
		List<Object> listParams = new ArrayList<Object>();
		
		StringBuffer sql=new StringBuffer();
		sql.append("select cs.subnet_name as subnetName,cs.cidr as cidr,cs.ip_version as ipVersion,cs.gateway_ip as gatewayIP,");
		sql.append(" cn.net_name as netName,cs.subnet_id as subnetId,cs.dc_id as dcId,cs.route_id as routeId,r.route_name as routeName,cs.prj_id as prjId ");
		/*子网类型，区分受管子网和自管子网*/
		sql.append(" ,cs.subnet_type as subnetType ");
		sql.append(" from cloud_subnetwork cs");
		sql.append(" left join cloud_network cn on cs.net_id=cn.net_id");
		sql.append(" left join cloud_route r on cs.route_id = r.route_id ");
		sql.append(" where 1=1");
		
		if (!"null".equals(dcId)&&null!=dcId&&!"".equals(dcId)&&!"undefined".equals(dcId)) {
			sql.append(" and cs.dc_id= ? ");
			listParams.add(dcId);
		}
		
		if (!"null".equals(routeId)&&null!=routeId&&!"".equals(routeId)&&!"undefined".equals(routeId)) {
			sql.append(" and cs.route_id= ? ");
			listParams.add(routeId);
		}
		
		sql.append(" order by cs.create_time desc ");
		javax.persistence.Query query =subNetDao.createSQLNativeQuery(sql.toString(), listParams.toArray());
		List listResult = query.getResultList();
		for (int i = 0; i < listResult.size(); i++) {
			Object[] objs = (Object[]) listResult.get(i);
			CloudSubNetWork subNet = new CloudSubNetWork();
			subNet.setSubnetName(String.valueOf(objs[0]));
			subNet.setCidr(String.valueOf(objs[1]));
			subNet.setIpVersion(String.valueOf(objs[2]));
			subNet.setGatewayIp(String.valueOf(objs[3]));
			subNet.setNetName(String.valueOf(objs[4]));
			subNet.setSubnetId(String.valueOf(objs[5]));
			subNet.setDcId(String.valueOf(objs[6]));
			subNet.setRouteId(null!=String.valueOf(objs[7])&&!"".equals(String.valueOf(objs[7]))?String.valueOf(objs[7]):"");
			subNet.setRouteName(String.valueOf(objs[8])==null&&!"".equals(String.valueOf(objs[8])) ? "":String.valueOf(objs[8]));
			subNet.setPrjId(String.valueOf(objs[9]));
			subNet.setSubnetType(String.valueOf(objs[10]));
			subnetList.add(subNet);
		}
		for(int i=0;i<subnetList.size();i++){
			String tagName = tagService.getResourceTagForShowcase("subNetwork", subnetList.get(i).getSubnetId());
			subnetList.get(i).setTagName(tagName);
		}
		return subnetList;
	}
	/**
	 * 获取项目下所有内网的子网列表信息,用于资源池创建中的下拉框
	 * @param datacenterId  数据中心id
	 * @param projectId  项目id
	 * 
	 * @return
	 * @throws AppException
	 */
	public List<BaseCloudSubNetWork> querySubnetList(String datacenterId,String projectId){
			
			List<BaseCloudSubNetWork> listSubNet = new ArrayList<BaseCloudSubNetWork>();
			
			StringBuffer sql=new StringBuffer();
			List<Object> list = new ArrayList<Object>();
			sql.append("select cs.subnet_id as subnetId,cs.subnet_name as subnetName,cs.net_id as netId");
			sql.append(" from cloud_subnetwork cs");
			sql.append(" left outer join cloud_network cn on cn.net_id=cs.net_id");
			sql.append(" where cn.router_external= '0' ");
		    if (!"null".equals(datacenterId)&&null!=datacenterId&&!"".equals(datacenterId)&&!"undefined".equals(datacenterId)) {
				sql.append(" and cs.dc_id = ?");
				list.add(datacenterId);
			}
		    if(!"null".equals(projectId)&&null!=projectId&&!"".equals(projectId)&&!"undefined".equals(projectId)){
		    	sql.append(" and cs.prj_id = ?");
		    	list.add(projectId);
		    }
		    sql.append(" order by cs.create_time desc ");
		    javax.persistence.Query query = subNetDao.createSQLNativeQuery(sql.toString(), list.toArray());
		    List listResult = query.getResultList();
		    for (int i = 0; i < listResult.size(); i++) {
				Object[] objs = (Object[]) listResult.get(i);
				BaseCloudSubNetWork subNet = new BaseCloudSubNetWork();
				subNet.setSubnetId(String.valueOf(objs[0]));
				subNet.setSubnetName(String.valueOf(objs[1]));
				subNet.setNetId(String.valueOf(objs[2]));
				
				listSubNet.add(subNet);
			}
		    
		    
			return listSubNet;
		
	}
	/*
	 * 陈鹏飞--ecsc网络模块屏蔽外网
	 */
    @SuppressWarnings("unchecked")
    @Override
    public boolean checkSubNetName(String subnetId,String subNetName,String prjId) {
        List<String> values = new ArrayList<String>();
        StringBuffer hql = new StringBuffer();
        hql.append("from BaseCloudSubNetWork where binary(subnetName) =? and prjId=?");
        values.add(subNetName);
        values.add(prjId);
        if(!StringUtil.isEmpty(subnetId)){
            hql.append(" and subnetId <> ?");
            values.add(subnetId);
        }
        List<BaseCloudNetwork> netWorkList = subNetDao.find(hql.toString(), values.toArray());
        return netWorkList.size()==0?true:false;
    }
    @Override
    public CloudSubNetWork addSubNetWork(CloudSubNetWork cloudSubNetWork) {
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
            subNetWork=openSubNetService.create(cloudSubNetWork.getDcId(), cloudSubNetWork.getPrjId(), data);
            
            createStep++;
            
            BaseCloudProject project = projectService.findProject(cloudSubNetWork.getPrjId());
            /*判断项目是否含有labelId，如果两个都有，则创建对应的labelRule，否则不创建*/
            if(project.getLabelInId() != null && project.getLabelOutId() != null) {
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
                subNetDao.save(baseSubNet);
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
    /*创建label-rule的私有接口*/
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
    //根据网络id查询路由
    private CloudRoute findRouteByNetId(String netId){
    	StringBuffer strb = new StringBuffer();
    	strb.append("select cr.rate,cn.net_name,cn.net_id,cr.route_id from cloud_route cr left outer join cloud_network cn on cn.net_id=cr.network_id where cr.network_id=?");
    	javax.persistence.Query query =routeDao.createSQLNativeQuery(strb.toString(), netId);
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
    //子网绑定路由私有方法
    private BaseCloudSubNetWork connectSubnet(String dcId,String routeId,String subnetworkId, CloudRoute check) {		
		Route route = openstackRouteService.attachInterface(dcId, routeId, subnetworkId);
		if(route!=null){
		    check.setRouteId(routeId);
			BaseCloudRoute cloudRoute =  routeDao.findOne(routeId);
			try {
    			if(StringUtils.isEmpty(cloudRoute.getQosId())){
			        eayunQosService.createQos(cloudRoute);
    			}
		    } catch (AppException e) {
		        if ("NeutronError-->A Qos of the same direction is already set with the target".equals(e.getArgsMessage()[0])) {
		            log.error(e.getArgsMessage()[0], e);
		        } else {
		            throw e;
		        }
		    }
			//如果路由连接子网未成功，直接throw AppException 不再操作本地DB
			BaseCloudSubNetWork subnet=this.getSubNetworkById(subnetworkId);
			subnet.setRouteId(routeId);
			this.saveOrUpdate(subnet);
			routeDao.saveOrUpdate(cloudRoute);
			return subnet;
		}else{
			return null;
		}
    }
    //路由解绑子网私有方法
    private BaseCloudSubNetWork detachSubnet(String dcId,String routeId,String subnetworkId){		
		BaseCloudSubNetWork subNet=new BaseCloudSubNetWork();
		//底层解绑子网需要用到下面3个参数
		Route route = openstackRouteService.detachInterface(dcId, routeId, subnetworkId);
		if(route!=null){
			//根据子网id 查处绑定了改路由的子网，然后置空子网的routeId
			subNet = this.getSubNetworkById(subnetworkId);
			 if(null!=subNet.getRouteId()&&!"".equals(subNet.getRouteId())){
				 subNet.setRouteId(null);
			 }
			 this.saveOrUpdate(subNet);
		}
		return subNet;
    }
    /*处理创建子网流程当中的异常*/
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
                openSubNetService.delete(dcId, prjId, subnet.getId());
            }
        }
    }
    @SuppressWarnings("unchecked")
    @Override
    public List<CloudSubNetWork> getSubNetListById(String netId,String prjId,String dcId) {
        List<String> value= new ArrayList<String>();
        StringBuffer sql = new StringBuffer();
        sql.append("select bcsn from BaseCloudSubNetWork bcsn,BaseCloudNetwork bcn");
        sql.append(" where bcn.netId = bcsn.netId and bcn.routerExternal ='0'");
        sql.append("    and bcsn.subnetType is not null ");
        if(!StringUtil.isEmpty(netId)){
            sql.append(" and bcsn.netId=?");
            value.add(netId);
        }
        if(!StringUtil.isEmpty(prjId)){
            sql.append(" and bcsn.prjId=?");
            value.add(prjId);
        }
        if(!StringUtil.isEmpty(dcId)){
            sql.append(" and bcsn.dcId=?");
            value.add(dcId);
        }
        org.hibernate.Query query =  subNetDao.createQuery(sql.toString(), value.toArray());
        Iterator<BaseCloudSubNetWork> iter =query.iterate();   
        List<CloudSubNetWork> subList = new ArrayList<CloudSubNetWork>();
        while(iter.hasNext()){
            CloudSubNetWork cloudSubNetWork = new CloudSubNetWork();
            BeanUtils.copyPropertiesByModel(cloudSubNetWork, iter.next());
            cloudSubNetWork.setSubnetTypeStr("0".equals(cloudSubNetWork.getSubnetType()) ? "自管子网" : "受管子网");
            subList.add(cloudSubNetWork);
        }
        for(int i=0;i<subList.size();i++){
			String tagName = tagService.getResourceTagForShowcase("subNetwork", subList.get(i).getSubnetId());
			subList.get(i).setTagName(tagName);
		}
        return subList;
    }
    @Override
    public CloudSubNetWork updateSubNetWork(CloudSubNetWork cloudSubNetWork) {
        String subnetname = cloudSubNetWork.getSubnetName();
        JSONObject subNet = new JSONObject();
        String dns [] = new String[]{};
        subNet.put("name", subnetname);
        if(!StringUtils.isEmpty(cloudSubNetWork.getDns())){
        	dns = cloudSubNetWork.getDns().split(";");
        }
        subNet.put("dns_nameservers", dns);
        JSONObject data = new JSONObject();
        data.put("subnet", subNet);
        //创建网络
        SubNetwork subNetWork=openSubNetService.update(cloudSubNetWork.getDcId(), cloudSubNetWork.getPrjId(), data, cloudSubNetWork.getSubnetId());
        if(subNetWork!=null){
            BaseCloudSubNetWork baseSubNet = new BaseCloudSubNetWork();
            BeanUtils.copyPropertiesByModel(baseSubNet, cloudSubNetWork);
            subNetDao.merge(baseSubNet);
        }
        return cloudSubNetWork;
    }
    @Override
    public boolean daleteCloudSubNet(CloudSubNetWork cloudSubNetWork) {
    	BaseCloudSubNetWork baseSubNet =null;
    	BaseCloudSubNetWork sub =null;
    	String routeId = null;
    	boolean bool =false;
    	try{
    		StringBuffer hql = new StringBuffer();
        	hql.append("from BaseCloudLdPool where subnetId=?");
        	org.hibernate.Query query = subNetDao.createQuery(hql.toString(), cloudSubNetWork.getSubnetId());
        	if(query.iterate().hasNext()){
        		return false;
        	}
        	//解绑子网
//        	CloudRoute cloudRoute = this.findRouteByNetId(cloudSubNetWork.getNetId());
        	sub = subNetDao.findOne(cloudSubNetWork.getSubnetId());
        	routeId = sub.getRouteId();
        	if(!StringUtils.isEmpty(sub.getRouteId())){
        		baseSubNet=this.detachSubnet(sub.getDcId(), sub.getRouteId(), sub.getSubnetId());
        	}
            bool=openSubNetService.delete(cloudSubNetWork.getDcId(), cloudSubNetWork.getPrjId(), cloudSubNetWork.getSubnetId());
            if(bool){
            	if(cloudSubNetWork.getInLabelRuleId() != null && cloudSubNetWork.getOutLabelRuleId() != null) {
            		boolean inLabelRuleFlag = meterLabelService.delete(cloudSubNetWork.getDcId(),cloudSubNetWork.getInLabelRuleId());
            		boolean outLabelRuleFlag = meterLabelService.delete(cloudSubNetWork.getDcId(),cloudSubNetWork.getOutLabelRuleId());
            	}
                subNetDao.delete(cloudSubNetWork.getSubnetId());
              //删除资源后更新缓存接口
    			tagService.refreshCacheAftDelRes("subNetwork", cloudSubNetWork.getSubnetId());
            }
            return bool;
    	}catch(Exception e){
    		throw e;
    	}
    	finally{
    		if((!bool)&&null!=baseSubNet&&!StringUtils.isEmpty(baseSubNet.getSubnetId())){
    			connectSubnet(cloudSubNetWork.getDcId(), routeId, baseSubNet.getSubnetId(), new CloudRoute());
    		}
    	}
    	
    }
    @Override
    public int findSubNetCountByPrjId(String prjId) {
        StringBuffer hql = new StringBuffer();
        hql.append("select count(*) from BaseCloudSubNetWork where prjId=?");
        int count = subNetDao.getCountByPrjId(prjId);
        return count;
    }
	@SuppressWarnings("unchecked")
	@Override
	public boolean checkCidr(String netId, String cidr) {
		StringBuffer hql = new StringBuffer();
		hql.append(" from BaseCloudSubNetWork where netId=? and cidr=?");
		List<String> values = new ArrayList<String>();
		values.add(netId);
		values.add(cidr);
		List<BaseCloudSubNetWork> list =subNetDao.find(hql.toString(), values.toArray());
		if(list!=null && list.size()>0){
			return false;
		}
		return true;
	}
	
	/**
     * 获取子网DNS
     * @param subnetId
     * @return
     */
    public String getSubnetDNS(String subnetId){
    	String dns = "";
    	BaseCloudSubNetWork subnet = subNetDao.findOne(subnetId);
    	if(null!=subnet){
    		dns = subnet.getDns();
    	}
    	return dns;
    }
    
    @Override
    public List<BaseCloudSubNetWork> getSubNetListByType(String netId, String subnetType) {
    	StringBuffer hql = new StringBuffer();
    	hql.append(" from BaseCloudSubNetWork where netId= ? and subnetType = ? ");
    	hql.append("   and ((subnetType = '1' and routeId is not null) or subnetType = '0')");
    	List<String> values = new ArrayList<String>();
    	values.add(netId);
    	values.add(subnetType);
    	List<BaseCloudSubNetWork> list = subNetDao.find(hql.toString(), values.toArray());
		return list;
    }
    
    @Override
    public List<BaseCloudSubNetWork> getManagedSubnetList(String netId) {
        StringBuffer hql = new StringBuffer();
        hql.append(" from BaseCloudSubNetWork where netId = ?");
        hql.append("   and subnetType = '1'");
        List<String> values = new ArrayList<String>();
        values.add(netId);
        List<BaseCloudSubNetWork> list = subNetDao.find(hql.toString(), values.toArray());
        return list;
    }
    
    @Override
    public boolean checkSubnetExistForVmInOrder(String subnetId) {
        StringBuffer sql = new StringBuffer();
        sql.append("select                              ");
        sql.append("    count(*)                        ");
        sql.append("from                                ");
        sql.append("    cloudorder_vm vm                ");
        sql.append("left join                           ");
        sql.append("    order_info oi                   ");
        sql.append("on                                  ");
        sql.append("    vm.order_no = oi.order_no       ");
        sql.append("where                               ");
        sql.append("    oi.order_type = 0               ");
        sql.append("and                                 ");
        sql.append("    oi.resource_type = 0            ");
        sql.append("and                                 ");
        sql.append("    oi.order_state in ('1', '2')    ");
        sql.append("and                                 ");
        sql.append("    vm.subnet_id = ?                ");
        Query query = subNetDao.createSQLNativeQuery(sql.toString(), new Object[]{subnetId});
        int count =  query.getSingleResult() == null ? 0 : Integer.parseInt(query.getSingleResult().toString());
        return count != 0;
    }
    
    @Override
    public BaseCloudSubNetWork getNetIdBySubnetId(String subnetId) {
        return subNetDao.findOne(subnetId);
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
		} else if (vmInOrderOccupySubnetCount > 0) {
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
		Query query = subNetDao.createSQLNativeQuery(sqlBuf.toString(), subnetId);
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
		Query query = subNetDao.createSQLNativeQuery(sqlBuf.toString(), new Object[]{subnetId, subnetId});
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
	    Query query = subNetDao.createSQLNativeQuery(sqlBuf.toString(), new Object[]{subnetId});
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
		Query query = subNetDao.createSQLNativeQuery(sqlBuf.toString(), new Object[]{subnetId});
        Object result = query.getSingleResult();
        int count = result == null ? 0 : Integer.parseInt(result.toString());
        return count;
	}
}

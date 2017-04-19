/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.virtualization.ecmcservice.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.eayun.common.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.eayunstack.model.Poolresource;
import com.eayun.eayunstack.model.SubNetwork;
import com.eayun.eayunstack.service.OpenstackSubNetworkService;
import com.eayun.virtualization.dao.CloudSubNetWorkDao;
import com.eayun.virtualization.ecmcservice.EcmcCloudOutIpService;
import com.eayun.virtualization.ecmcservice.EcmcOutSubNetworService;
import com.eayun.virtualization.model.BaseCloudSubNetWork;
import com.eayun.virtualization.model.EcmcCloudSubNetwork;

/**
 *                       
 * @Filename: EcmcOutSubNetworkServiceImpl.java
 * @Description: 
 * @Version: 1.0
 * @Author: fangjun.yang
 * @Email: fangjun.yang@eayun.com
 * @History:<br>
 *<li>Date: 2016年4月5日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Service
@Transactional
public class EcmcOutSubNetworkServiceImpl implements EcmcOutSubNetworService {

    private final static Logger log = LoggerFactory.getLogger(EcmcOutSubNetworkServiceImpl.class);

    @Autowired
    private CloudSubNetWorkDao  cloudSubNetWorkDao;
    
    @Autowired
    private OpenstackSubNetworkService openstackSubNetworkService;
    
    @Autowired
	private EcmcCloudOutIpService outIpService;

	@Override
	public List<EcmcCloudSubNetwork> getOutSubNetworkList( Map<String, String> map) throws AppException {
		String dcId = map.get("dcId");
		String netId = map.get("netId");
		List<EcmcCloudSubNetwork> list=cloudSubNetWorkDao.findEcmcCloudSubNetwork(dcId, netId);
		return list;
	}

	@Override
	public EcmcCloudSubNetwork addSubNetwork(EcmcCloudSubNetwork ecmcCloudSubNetwork) throws AppException {
		//网络数据
        JSONObject subnet = new JSONObject();           
        subnet.put("name", ecmcCloudSubNetwork.getSubnetName());
        subnet.put("network_id", ecmcCloudSubNetwork.getNetId());   
        subnet.put("ip_version", ecmcCloudSubNetwork.getIpVersion().substring(3, 4));
        subnet.put("cidr", ecmcCloudSubNetwork.getCidr());
        subnet.put("gateway_ip", ecmcCloudSubNetwork.getGatewayIp());
        String pooldata = ecmcCloudSubNetwork.getPooldata();
        //网络地址池拼装
        if (pooldata !=null &&  pooldata.indexOf(",")!=-1){
            String startip = pooldata.substring(0, pooldata.indexOf(","));
            String endip = pooldata.substring(pooldata.indexOf(",")+1, pooldata.length());
            JSONObject pool = new JSONObject(); 
            pool.put("start", startip);
            pool.put("end", endip); 
            JSONArray pools = new JSONArray();
            pools.add(pool);        
            subnet.put("allocation_pools", pools);   
        }else{
        	pooldata="";
        }
        //用于提交的完整数据
        JSONObject resultData = new JSONObject();
        resultData.put("subnet", subnet);

        //创建网络
        SubNetwork subnetwork = openstackSubNetworkService.create(ecmcCloudSubNetwork.getDcId(), ecmcCloudSubNetwork.getPrjId(), resultData);
        //SubNetwork平台创建成功后，保持到数据库
        if(subnetwork!=null){
            BaseCloudSubNetWork cloudsubNetwork=new BaseCloudSubNetWork();
            cloudsubNetwork.setSubnetId(subnetwork.getId());
            cloudsubNetwork.setSubnetName(subnetwork.getName());
            cloudsubNetwork.setCreateName(ecmcCloudSubNetwork.getCreateName());
            cloudsubNetwork.setCreateTime(new Date());      
            //cloudsubNetwork.setPrjId(projectId); //此处旧版ecmc如此
            cloudsubNetwork.setDcId(ecmcCloudSubNetwork.getDcId());
            cloudsubNetwork.setNetId(subnetwork.getNetwork_id());
            cloudsubNetwork.setIpVersion(subnetwork.getIp_version());
            cloudsubNetwork.setCidr(subnetwork.getCidr());
            cloudsubNetwork.setGatewayIp(subnetwork.getGateway_ip());
            pooldata = "";
            for (Poolresource poolresource : subnetwork.getAllocation_pools()) {
            	pooldata+=poolresource.getStart()+","+poolresource.getEnd()+";";
			}
            pooldata=pooldata.substring(0,pooldata.lastIndexOf(";"));
            cloudsubNetwork.setPooldata(pooldata);
            cloudsubNetwork.setIsForbiddengw("true".equals(ecmcCloudSubNetwork.getIsForbiddengw())?"1":"0");//如果前端传递是true(确定以禁止)则数据库设置为1，否则为0
            cloudSubNetWorkDao.saveOrUpdate(cloudsubNetwork);
            //20160417 start liujingang outip add
            outIpService.createOutIp(cloudsubNetwork.getCidr(), cloudsubNetwork.getPooldata(), cloudsubNetwork.getDcId(), cloudsubNetwork.getNetId(), cloudsubNetwork.getSubnetId(), cloudsubNetwork.getRouteId(), cloudsubNetwork.getIpVersion());
            //end liujingang
            return ecmcCloudSubNetwork;
        }else{
            return null;
        }
	}

	@Override
	public EcmcCloudSubNetwork updateSubNetwork(Map<String, String> map) throws AppException {
		String subNetName = map.get("subnetName");
		String gatewayIp = map.get("gatewayIp");
//		String isForbidGateway = map.get("isForbidGateway");
		String dcId = map.get("dcId");
		String subNetId = map.get("subnetId");
		EcmcCloudSubNetwork voe = new EcmcCloudSubNetwork();
        //1、调用底层更新子网数据
        JSONObject subnet = new JSONObject();           
        subnet.put("name", subNetName); 
        //外网子网修改网关
        if(!StringUtil.isEmpty(gatewayIp)){
            subnet.put("gateway_ip", gatewayIp);    
        }
//        if("true".equals(isForbidGateway)){
//            subnet.put("gateway_ip", "null");
//        }
        
        JSONObject resultData = new JSONObject();
        resultData.put("subnet", subnet);
        SubNetwork subnetwork = openstackSubNetworkService.update(dcId, null, resultData, subNetId);
        //2、调用DAO更新上层业务数据库
        if(subnetwork!=null){
            BaseCloudSubNetWork cloudsubNetwork= cloudSubNetWorkDao.findOne(subNetId);
            cloudsubNetwork.setSubnetName(subNetName);
            cloudsubNetwork.setGatewayIp(subnetwork.getGateway_ip()==null?"":subnetwork.getGateway_ip());
            cloudsubNetwork.setIsForbiddengw("".equals(cloudsubNetwork.getGatewayIp())?"1":"0");
            cloudSubNetWorkDao.saveOrUpdate(cloudsubNetwork);
            return voe;
        }else{
            return null;
        }
	}

	@Override
	public boolean deleteSubNetwork(Map<String, String> map) throws AppException {
		String dcId = map.get("dcId");
		String subNetId = map.get("subNetId");
		if (openstackSubNetworkService.delete(dcId, null, subNetId)) {
            cloudSubNetWorkDao.delete(subNetId);
            //20160417 start liujingang outip 
            outIpService.deleteOutIp(subNetId);
            return true;
        }
        return false;
	}

	@Override
	public boolean checkOutSubnetName(Map<String, String> map) throws AppException {
		String netId = map.get("netId");
		String subNetName = map.get("subnetName");
		String subNetId = map.get("subnetId");
		
		boolean bool=cloudSubNetWorkDao.countMultiSubnetName(netId, subNetName, subNetId) > 0 ? true : false;
		return bool;
	}
    
    
}

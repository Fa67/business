/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.virtualization.ecmcservice.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.util.BeanUtils;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.datacenter.service.DataCenterService;
import com.eayun.eayunstack.model.Network;
import com.eayun.eayunstack.service.OpenstackNetworkService;
import com.eayun.virtualization.dao.CloudNetWorkDao;
import com.eayun.virtualization.ecmcservice.EcmcOutNetworkService;
import com.eayun.virtualization.model.BaseCloudNetwork;
import com.eayun.virtualization.model.EcmcCloudNetwork;

/**
 *                       
 * @Filename: EcmcOutNetworkServiceImpl.java
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
public class EcmcOutNetworkServiceImpl implements EcmcOutNetworkService {
    
    private final static Logger log = LoggerFactory.getLogger(EcmcOutNetworkServiceImpl.class);

    @Autowired
    private CloudNetWorkDao cloudNetWorkDao;
    @Autowired
    private DataCenterService dcService;
    
    @Autowired
    private OpenstackNetworkService openstackNetworkService;

	@Override
	public Page getOutNetworkList(Page page, ParamsMap paramsMap)throws AppException {
		StringBuffer hql = new StringBuffer();
		hql.append("select net.net_id,net.net_name,net.net_status,net.admin_stateup,net.create_time,net.is_shared,net.dc_id,");
		hql.append(" count(subNet.subnet_id) as subNetNum,dc.dc_name,net.prj_id");
		hql.append(" from cloud_network net");
		hql.append(" left join cloud_subnetwork subNet on net.net_id = subNet.net_id");
		hql.append(" left join dc_datacenter dc on dc.id = net.dc_id");
		hql.append(" where net.router_external='1'");
		Object dcIdObj =paramsMap.getParams().get("dcId");
		Object netNameObj =paramsMap.getParams().get("netName");
		String dcId = dcIdObj==null?null:String.valueOf(dcIdObj);
		String netName = netNameObj==null?null:String.valueOf(netNameObj);
		List<String> values = new ArrayList<String>();
		if(!StringUtils.isEmpty(dcId)){
			hql.append(" and net.dc_id=?");
			values.add(dcId);
		}
		if(!StringUtils.isEmpty(netName)){
			hql.append(" and binary(net.net_name) like ?");
			netName = netName.replaceAll("\\_", "\\\\_").replaceAll("\\%", "\\\\%");
			values.add("%"+netName+"%" );
		}
		hql.append(" group by net.net_id ");
		hql.append(" order by net.dc_id,net.prj_id,net.create_time desc ");
		QueryMap queryMap = new QueryMap();
		int pageSize = paramsMap.getPageSize();
		int pageNumber = paramsMap.getPageNumber();
		queryMap.setPageNum(pageNumber);
		queryMap.setCURRENT_ROWS_SIZE(pageSize);
		page=cloudNetWorkDao.pagedNativeQuery(hql.toString(), queryMap, values.toArray());
		List<Object> objList = (List<Object>) page.getResult();
		if(objList!=null && objList.size()>0){
			int i=0;
			for (Object object : objList) {
				Object[] obj = (Object[]) object;
				EcmcCloudNetwork ecmcNetWork = new EcmcCloudNetwork();
				ecmcNetWork.setNetId(String.valueOf(obj[0]));
				ecmcNetWork.setNetName(String.valueOf(obj[1]));
				ecmcNetWork.setNetStatus(String.valueOf(obj[2]));
				ecmcNetWork.setAdminStateup(String.valueOf(obj[3]));
				ecmcNetWork.setCreateTime((Date)obj[4]);
				ecmcNetWork.setIsShared(String.valueOf(obj[5]));
				ecmcNetWork.setDcId(String.valueOf(obj[6]));
				ecmcNetWork.setSubnetnum(String.valueOf(obj[7]));
				ecmcNetWork.setDcName(String.valueOf(obj[8]));
				ecmcNetWork.setPrjId(String.valueOf(obj[9]));
				objList.set(i, ecmcNetWork);
				i++;
			}
		}
		return page;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<EcmcCloudNetwork> getAllOutNetworkList(Map<String, String> map) {
		StringBuffer hql = new StringBuffer();
		hql.append(" from BaseCloudNetwork net");
		hql.append(" where net.routerExternal='1'");
		String dcId =map.get("dcId");
		String netName=map.get("netName");
		String netId = map.get("netId");
		List<String> values = new ArrayList<String>();
		if(!StringUtils.isEmpty(dcId)){
			hql.append(" and net.dcId=?");
			values.add(dcId);
		}
		if(!StringUtils.isEmpty(netName)){
			hql.append(" and binary(net.netName)=?");
			values.add(netName);
		}
		if(!StringUtils.isEmpty(netId)){
			hql.append(" and net.netId <> ?");
			values.add(netId);
		}
		hql.append(" group by net.netId order by net.netName desc");
		List<BaseCloudNetwork> baseList =cloudNetWorkDao.find(hql.toString(), values.toArray());
		List<EcmcCloudNetwork> list = new ArrayList<EcmcCloudNetwork>();
		for (BaseCloudNetwork baseCloudNetwork : baseList) {
			EcmcCloudNetwork ecmcCloudNetwork = new EcmcCloudNetwork();
			BeanUtils.copyPropertiesByModel(ecmcCloudNetwork, baseCloudNetwork);
			list.add(ecmcCloudNetwork);
		}
		return list;
	}


	@Override
	public EcmcCloudNetwork getCloudNetworkById(Map<String, String> map) throws AppException {
		EcmcCloudNetwork cloudNetwork=null;
		BaseCloudNetwork network = cloudNetWorkDao.findOne(map.get("netId"));
		if (network!=null) {
		    BaseDcDataCenter dc = dcService.getById(network.getDcId());
			cloudNetwork = new EcmcCloudNetwork();
			BeanUtils.copyPropertiesByModel(cloudNetwork, network);
			cloudNetwork.setAdminStateup(cloudNetwork.getAdminStateup()!=null&&"1".equals(cloudNetwork.getAdminStateup())?"是":"否");
			cloudNetwork.setIsShared(cloudNetwork.getIsShared()!=null&&"1".equals(cloudNetwork.getIsShared())?"是":"否");
			cloudNetwork.setDcName(dc.getName());
			if(cloudNetwork.getPrjId()!=null&&!"".equals(cloudNetwork.getPrjId().trim())){
				String prjName = cloudNetWorkDao.getProjectName(cloudNetwork.getPrjId());
				if(StringUtils.isNotBlank(prjName)){
					cloudNetwork.setPrjName(prjName);
				}else{
					cloudNetwork.setPrjName("无");
				}
			}else
				cloudNetwork.setPrjName("无");
		}
		return cloudNetwork;
	}

	@Override
	public EcmcCloudNetwork modifyOutNetwork(Map<String, String> map) throws AppException {
		EcmcCloudNetwork cloudNetwork = null;
        //1、调用底层保存网络数据
		String netId = map.get("netId");
		String netName = map.get("netName");
		String admStateup = map.get("admStateup");
		String dcId = map.get("dcId");
        JSONObject net = new JSONObject();          
        net.put("name",netName);
        net.put("admin_state_up", admStateup);        
        net.put("router:external", "true");
        JSONObject data = new JSONObject();
        data.put("network", net); 
        
        Network network = openstackNetworkService.update(dcId, null, data, netId);
        //2、底层保存成功后，更新业务数据库信息
        BaseCloudNetwork baseCloudNetwork = null;
        if(network!=null){
            baseCloudNetwork = cloudNetWorkDao.findOne(netId);
            baseCloudNetwork.setNetName(netName);
            baseCloudNetwork.setAdminStateup("true".equals(admStateup)?"1":"0");
            cloudNetWorkDao.saveOrUpdate(baseCloudNetwork);
        }
        if(baseCloudNetwork!=null){
            cloudNetwork = new EcmcCloudNetwork();
            BeanUtils.copyPropertiesByModel(cloudNetwork, baseCloudNetwork);
        }
        return cloudNetwork;
	}

	@Override
	public boolean checkNetName(Map<String, String> map) {
		List<EcmcCloudNetwork> list =this.getAllOutNetworkList(map);
		if(list!=null && list.size()>0){
			return true;
		}
		return false;
	}
}

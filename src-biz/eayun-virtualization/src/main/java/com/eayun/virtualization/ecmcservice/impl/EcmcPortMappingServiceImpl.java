package com.eayun.virtualization.ecmcservice.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.eayunstack.model.PortMapping;
import com.eayun.eayunstack.service.OpenstackPortMappingService;
import com.eayun.virtualization.dao.PortMappingDao;
import com.eayun.virtualization.ecmcservice.EcmcPortMappingService;
import com.eayun.virtualization.model.BaseCloudPortMapping;
import com.eayun.virtualization.model.CloudPortMapping;

@Service
@Transactional
public class EcmcPortMappingServiceImpl implements EcmcPortMappingService {

    @Autowired
    private PortMappingDao portMappingDao;
    @Autowired
    private OpenstackPortMappingService openstackPortMappingService;

    @Override
    public Page getPortMappingList(Page page, String dcId, String prjId,
            String routeId, QueryMap queryMap) throws Exception {
        StringBuffer sql = new StringBuffer();
        sql.append(" select ");
        sql.append("	pm.pm_id");
        sql.append("	,pm.dc_id");
        sql.append("	,pm.prj_id");
        sql.append("	,pm.protocol");
        sql.append("	,pm.resource_id");
        sql.append("	,pm.resource_ip");
        sql.append("	,pm.resource_port");
        sql.append("	,pm.destiny_id");
        sql.append("	,pm.destiny_ip");
        sql.append("	,pm.destiny_port");
        sql.append("	,pm.create_name");
        sql.append("	,pm.create_time");
        sql.append("	,cv.subnet_id");
        sql.append(" from ");
        sql.append("	cloud_portmapping pm");
        sql.append(" LEFT JOIN cloud_vm cv ");
        sql.append(" on pm.destiny_id = cv.vm_id");
        sql.append(" where ");
        sql.append("	1=1 ");
        sql.append("	and pm.resource_id = ?");
        sql.append(" order by pm.create_time desc ");
        List<String> params = new ArrayList<String>();
        params.add(routeId);
        page = portMappingDao.pagedNativeQuery(sql.toString(), queryMap,
                params.toArray());
        List result = (List) page.getResult();
        for (int i = 0; i < result.size(); i++) {
            Object[] objs = (Object[]) result.get(i);
            CloudPortMapping portMapping = new CloudPortMapping();
            portMapping.setPmId(String.valueOf(objs[0]));
            portMapping.setDcId(String.valueOf(objs[1]));
            portMapping.setPrjId(String.valueOf(objs[2]));
            portMapping.setProtocol(String.valueOf(objs[3]));
            portMapping.setResourceId(String.valueOf(objs[4]));
            portMapping.setResourceIp(String.valueOf(objs[5]));
            portMapping.setResourcePort(String.valueOf(objs[6]));
            portMapping.setDestinyId(String.valueOf(objs[7]));
            portMapping.setDestinyIp(String.valueOf(objs[8]));
            portMapping.setDestinyPort(String.valueOf(objs[9]));
            portMapping.setCreateName(String.valueOf(objs[10]));
            portMapping.setCreateTime((Date) objs[11]);
            portMapping.setSubnetId(String.valueOf(objs[12]));
            result.set(i, portMapping);
        }
        return page;
    }
    @Override
    public CloudPortMapping addPortMapping(CloudPortMapping cloudPortMapping) {
        JSONObject portMapping = new JSONObject();
        portMapping.put("tenant_id", cloudPortMapping.getPrjId());
        portMapping.put("router_id", cloudPortMapping.getResourceId());
        portMapping.put("router_port", cloudPortMapping.getResourcePort());
        portMapping.put("destination_ip", cloudPortMapping.getDestinyIp());
        portMapping.put("destination_port", cloudPortMapping.getDestinyPort());
        portMapping.put("protocol", cloudPortMapping.getProtocol());
        portMapping.put("name", "");
        portMapping.put("admin_state_up", "true");
        JSONObject data = new JSONObject();
        data.put("portmapping", portMapping);
        PortMapping result = openstackPortMappingService.create(
                cloudPortMapping.getDcId(), cloudPortMapping.getPrjId(), data);
        if (result != null) {
            BaseCloudPortMapping pm = new BaseCloudPortMapping();
            pm.setPmId(result.getId());
            pm.setDcId(cloudPortMapping.getDcId());
            pm.setPrjId(cloudPortMapping.getPrjId());
            pm.setProtocol(result.getProtocol());
            pm.setResourceId(result.getRouter_id());
            pm.setResourceIp(cloudPortMapping.getResourceIp());
            pm.setResourcePort(String.valueOf(result.getRouter_port()));
            pm.setDestinyId(cloudPortMapping.getDestinyId());
            pm.setDestinyIp(result.getDestination_ip());
            pm.setDestinyPort(String.valueOf(result.getDestination_port()));
            pm.setCreateName("");
            pm.setCreateTime(new Date());
            portMappingDao.save(pm);
            cloudPortMapping.setPmId(result.getId());
        }
        return cloudPortMapping;
    }

    @Override
    public CloudPortMapping updatePortMapping(CloudPortMapping cloudPortMapping) {
        boolean deleteFlag = deletePortMapping(cloudPortMapping.getDcId(), cloudPortMapping.getPrjId(), cloudPortMapping.getPmId());
        if (deleteFlag) {
            cloudPortMapping = addPortMapping(cloudPortMapping);
        }
        return cloudPortMapping;
    }

    @Override
    public boolean deletePortMapping(String dcId, String prjId, String portMappingId) {
        boolean deleteFlag = openstackPortMappingService.delete(dcId, prjId, portMappingId);
        if (deleteFlag) {
            portMappingDao.delete(portMappingId);
        }
        return deleteFlag;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<BaseCloudPortMapping> queryPortMappingListByDestinyId(
            String destinyId) {
        StringBuffer hql = new StringBuffer();
        hql.append(" from ");
        hql.append("    BaseCloudPortMapping");
        hql.append(" where ");
        hql.append("    destinyId = ?");
		List<BaseCloudPortMapping> list = portMappingDao.find(hql.toString(),
                new Object[] { destinyId });
        ;
        return list;
    }

    @Override
    public int getCountByPrjId(String prjId) {
        return portMappingDao.getCountByPrjId(prjId);
    }
	@Override
	public boolean checkResourcePort(Map<String, String> params) {
		String routeId = params.get("routeId");//路由ID
		String protocol = params.get("protocol");//协议
		String resourcePort = params.get("resourcePort");//前台输入的源端口
		String pmId = params.get("pmId");//端口映射ID，新建为null
		boolean flag = false;
		int count = portMappingDao.countMultiResourcePort(pmId, routeId, protocol, resourcePort);
		if(count > 0)
			flag = true;
		return flag;
	}
	@Override
    public boolean deletePortMappingListByDestinyId(String dcId, String prjId, String destinyId) {
        List<BaseCloudPortMapping> list = queryPortMappingListByDestinyId(destinyId);
        for (int i = 0; i < list.size(); i++) {
            deletePortMapping(dcId, prjId, list.get(i).getPmId());
        }
        return false;
    }
}

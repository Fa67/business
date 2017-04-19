package com.eayun.virtualization.dao;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.eayun.common.dao.IRepository;
import com.eayun.virtualization.model.BaseCloudVpn;
@Repository
public interface CloudVpnDao extends IRepository<BaseCloudVpn, String> {

    @Query("select new map (cn.netId as netId,cn.netName as netName,cr.routeId as routeId,cr.gatewayIp as gatewayIp)"
            + " from BaseCloudNetwork cn, BaseCloudRoute cr"
            + " where cn.netId = cr.netWorkId and cn.routerExternal = '0' and cr.gatewayIp is not null and cn.prjId =?")
    public List<Map<String, Object>> findNetworkByPrjId(String prjId);
    
    @Query("select count(*) from BaseCloudVpnConn where prjId=? and isVisible = '1'")
    public int getCountByPrjId(String prjId);
    
    @Query("select count(*) from BaseCloudVpnConn cvc,BaseCloudVpn cv where cvc.vpnserviceId = cv.vpnServiceId and cv.routeId = ?")
    public int getCountByRouteId(String routeId);
}

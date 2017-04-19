package com.eayun.virtualization.dao;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eayun.common.dao.IRepository;
import com.eayun.virtualization.model.BaseCloudRoute;

public interface CloudRouteDao extends IRepository<BaseCloudRoute, String> {
	 
    @Query("select count(*) from BaseCloudRoute t where t.prjId= ? ")
    public int getCountByPrjId(String prjId);

    @Query("select sum(r.rate) from BaseCloudRoute r, BaseCloudNetwork n where r.netWorkId = n.netId and r.prjId= ? and n.isVisible = '1' ")
    public String getQosNumByPrjId(String prjId);

    public int countByNetWorkId(String netWorkId);

    @Query("select count(t.routeId) from BaseCloudRoute t"
            + " where 1=1"
            + " and (:dcId = null or t.dcId = :dcId)"
            + " and (:prjId = null or t.prjId = :prjId)"
            + " and (:routeName = null or binary(t.routeName) = :routeName)"
            + " and (:routeId = null or :routeId = '' or t.routeId <> :routeId)")
    public int countByRouteName(@Param("dcId") String datacenterId, @Param("prjId") String projectId, @Param("routeName") String routeName, @Param("routeId") String routeId);
    
    /**
	 * 获取带宽使用量
	 * @author zengbo
	 * @return
	 */
	@Query("select sum(rate) from BaseCloudRoute")
	public int getQosNum();
	
    @Query("select new map(p.countBand as countBand,sum(cr.rate) as usedRate)"
        
            + " from BaseCloudProject p, BaseCloudRoute cr"
            + " where p.projectId = cr.prjId and p.projectId = ?")
	public Map<String, Object> findRateInfo(String prjId);
    
    @Query("select new map(cr.routeName as routeName,cr.routeStatus as routeStatus,cr.netId as netId,cp.prjName as prjName,"
            + " cr.routeId as routeId,cr.prjId as prjId,cr.dcId as dcId,cr.rate,cn.netName as netName , cr.netWorkId as netWorkId,dc.name as dcName,cr.rate as rate,cr.gatewayIp as gatewayIp)"
            + " from BaseCloudRoute cr, BaseCloudProject cp, BaseCloudNetwork cn ,BaseDcDataCenter dc"
            + " where cr.prjId = cp.projectId and cr.netWorkId = cn.netId"
            + " and cr.dcId = dc.id"
            + " and cr.routeId = ?")
    public Map<String, Object> findRouteDetail(String routeId);
    
    public List<BaseCloudRoute> findByNetWorkId(String netWorkId);
    
    @Query("from BaseCloudRoute where netWorkId = :networkId ")
    public BaseCloudRoute queryByNetworkId(@Param("networkId")String networkId);
    
    @Query("update BaseCloudRoute set rateOld = :rateOld where routeId = :routeId")
    public void updateRateOldByRouteId(@Param("rateOld")int rateOld, @Param("routeId") String routeId);
    
    /**
     * 根据路由ID获取路由绑定的子网（受管子网）数量
     * @param routeId
     * @return
     */
    @Query("select count(*) from BaseCloudRoute cr,BaseCloudSubNetWork cn where cr.routeId = cn.routeId and cn.subnetType = '1' and cr.routeId= ? ")
    public int getSubNetworkCountByRouteId(String routeId);
}

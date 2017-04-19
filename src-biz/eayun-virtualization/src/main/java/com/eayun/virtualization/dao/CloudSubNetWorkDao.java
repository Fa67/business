package com.eayun.virtualization.dao;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eayun.common.dao.IRepository;
import com.eayun.virtualization.model.BaseCloudSubNetWork;
import com.eayun.virtualization.model.EcmcCloudSubNetwork;

public interface CloudSubNetWorkDao extends IRepository<BaseCloudSubNetWork, String> {
    @Query("select count(*) from BaseCloudSubNetWork where prjId=?")
    public int getCountByPrjId(String prjId);
    
    @Query("select new map (cs.subnetId as subnetId,cs.subnetName as subnetName,cs.cidr as cidr"
            + ",cs.ipVersion as ipVersion,cs.pooldata as pooldata,cs.gatewayIp as gatewapIp"
            + ",cs.isForbiddengw as isForbiddengw,cn.netId as netId,cn.netName as netName)"
            + " from BaseCloudSubNetWork cs, BaseCloudNetwork cn"
            + " where cn.netId = cs.netId and (cs.dcId = :dcId or :dcId = null) and (cs.netId = :netId or :netId = null)")
    public List<Map<String, Object>> findSubNetwork(@Param("dcId") String dcId, @Param("netId") String netId);
    
    public int countByNetIdAndCidr(String netId, String cidr);
    
    @Query("select new com.eayun.virtualization.model.EcmcCloudSubNetwork(cs.subnetId,cs.subnetName,cs.ipVersion,cn.netName,"
            + "cs.cidr, cs.gatewayIp, cs.pooldata,cs.isForbiddengw)"
            + " from BaseCloudSubNetWork cs, BaseCloudNetwork cn"
            + " where cs.netId = cn.netId and (cs.subnetId = :subnetId or :subnetId = null)")
    public List<EcmcCloudSubNetwork> findEcmcCloudSubNetwork(@Param("subnetId") String subnetId);
    
    @Query("select new com.eayun.virtualization.model.EcmcCloudSubNetwork(cs.subnetId,cs.subnetName,cs.ipVersion,cs.netId,cn.netName,"
            + "cs.cidr, cs.gatewayIp, cs.pooldata,cs.isForbiddengw)"
            + " from BaseCloudSubNetWork cs, BaseCloudNetwork cn"
            + " where cs.netId = cn.netId "
            + " and (cs.dcId = :dcId or :dcId = null)"
            + " and (cs.netId = :netId or :netId = null)"
            + " group by cs.subnetId"
            + " order by cs.createTime desc")
    public List<EcmcCloudSubNetwork> findEcmcCloudSubNetwork(@Param("dcId") String dcId, @Param("netId") String netId);
    
    @Query("select count(*) from BaseCloudSubNetWork  as cs"
            + " where (:datacenterId = null or cs.dcId = :datacenterId)"
            + " and (:projectId = null or cs.prjId = :projectId)"
            + " and (:subnetName = null or binary(cs.subnetName) = :subnetName)"
            + " and (:subnetId = null or cs.subnetId <> :subnetId)")
    public int countBy(@Param("datacenterId") String datacenterId,@Param("projectId") String projectId,@Param("subnetName") String subnetName,@Param("subnetId")String subnetId);
    
    @Query("select new Map(cs.subnetName as subnetName,cs.cidr as cidr,cs.ipVersion as ipVersion,cs.gatewayIp as gatewayIp,"
            + " cn.netName as netName,cs.subnetId as subnetId)"
            + " from BaseCloudSubNetWork cs, BaseCloudNetwork cn"
            + " where cs.netId = cn.netId"
            + " and (:dcId = null or cs.dcId = :dcId)"
            + " and (:routeId = null or cs.routeId = :routeId)")
    public List<Map<String, Object>> findRouteSubNetwork(@Param("dcId") String dcId, @Param("routeId") String routeId);
    
    public int deleteByNetId(String netId);
    
    public int countByNetId(String netId);
    
    @Query("select count(sn.subnetId) from BaseCloudSubNetWork sn where sn.netId = ?1 and binary(sn.subnetName) = ?2"
    		+ " and (?3 = null or ?3 = '' or sn.subnetId <> ?3)")
    public int countMultiSubnetName(String netId, String subnetName, String subnetId);
}

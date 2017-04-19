package com.eayun.virtualization.dao;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.eayun.common.dao.IRepository;
import com.eayun.virtualization.model.BaseCloudNetwork;
import com.eayun.virtualization.model.CloudNetWork;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.model.EcmcCloudNetwork;

@Repository
public interface CloudNetWorkDao extends IRepository<BaseCloudNetwork, String> {
    @Query("select count(*) from BaseCloudNetwork where prjId = ? and isVisible = '1'")
    public int getCountByPrjId(String prjId);
    
    /**
     * 查询全部私有网络数量
     * @author zengbo
     * @return
     */
    @Query("select count(*) from BaseCloudNetwork t where t.prjId is not null")
    public int getAllCount();
    
    @Query("select count(*) from BaseCloudNetwork cn where (cn.prjId = :prjId or :prjId = null) and (cn.dcId = :dcId or :dcId = null) and binary(cn.netName) = :netName and (cn.netId<>:netId or :netId = null)")
    public Long countBy(@Param("prjId")String prjId, @Param("dcId")String dcId, @Param("netName")String netName, @Param("netId")String netId);
    
    public int countByNetId(String netId);
    
    @Query("select new map (cn.netId as netId,cn.netName as netName,cp.prjName as prjName,cp.projectId as prjId,cn.netStatus as netStatus,cn.adminStateup as adminStateup,cn.isShared as isShared)"
            + " from BaseCloudNetwork cn, BaseCloudProject cp"
            + " where cn.prjId = cp.projectId and cn.netId =?")
    public List<Map<String, Object>> findNetworkDetailById(String netId);
    
    @Query("select new com.eayun.virtualization.model.EcmcCloudNetwork(cn.netId,cn.netName,count(cs.subnetId) as subNum,cn.netStatus,cn.adminStateup,cn.createTime,cn.isShared,cn.dcId,dc.name)"
            + " from BaseCloudNetwork cn, BaseCloudSubNetWork cs, BaseDcDataCenter dc"
            + " where cn.routerExternal='1' and cs.netId = cn.netId and cn.dcId = dc.id"//表明是外网
            + " and (:dcId = null or :dcId = '' or cn.dcId = :dcId)"
            + " group by cn.netId order by cn.createTime desc")
    public List<EcmcCloudNetwork> findOutNetwork(@Param("dcId") String dcId, Pageable pageRequest);
    
    @Query("select count(*) from BaseCloudNetwork cn, BaseCloudSubNetWork cs, BaseDcDataCenter dc"
            + " where cn.routerExternal='1' and cs.netId = cn.netId and cn.dcId = dc.id"
            + " and (:dcId = null or cn.dcId = :dcId)"
            + " group by cn.netId ")
    public Integer countByDcIdAndGroupByNetId(@Param("dcId") String dcId);
    
    @Query("from BaseCloudNetwork cn where cn.routerExternal = 1 and (cn.dcId = :dcId or :dcId = null)")
    public List<BaseCloudNetwork> findOutNetwork(@Param("dcId") String dcId);
    
    @Query(value = "select prj_name from cloud_project where prj_id = ? limit 1", nativeQuery = true)
	public String getProjectName(String prjId);
    
    @Query("select new com.eayun.virtualization.model.CloudNetWork(t) from BaseCloudNetwork t where t.routerExternal = '0' and t.prjId = ?")
    public List<CloudNetWork> findInnerCloudNetworkByPrjId(String prjId);
    
    @Query("select new com.eayun.virtualization.model.CloudProject(cp, dc.name) from BaseCloudProject cp, BaseDcDataCenter dc"
    		+ " where cp.dcId = dc.id and cp.projectId = ?")
    public CloudProject findCloudProjectByPrjId(String prjId);
}

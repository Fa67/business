package com.eayun.virtualization.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eayun.common.dao.IRepository;
import com.eayun.virtualization.ecmcvo.CloudLdpoolVoe;
import com.eayun.virtualization.model.BaseCloudFloatIp;
import com.eayun.virtualization.model.BaseCloudLdPool;

public interface CloudLdPoolDao extends IRepository<BaseCloudLdPool, String> {
	 @Query("select count(*) from BaseCloudLdPool t where t.prjId= ? and t.isVisible = '1' ")
	 public int getCountByPrjId(String prjId);
	 
	 @Query("select count(*) from BaseCloudLdPool ldPool,BaseCloudSubNetWork subNet where subNet.netId = ? and ldPool.subnetId=subNet.subnetId")
	 public int countByNetId(String netId);
	 
	/**
	 * 查询所有资源池个数
	 * @author zengbo
	 * @return
	 */
	@Query("select count(*) from BaseCloudLdPool where prjId is not null")
	public int getAllCount();

	@Query("select count(*) from BaseCloudLdPool p where p.prjId = ?1 and binary(p.poolName) = ?2 and (?3 = null or ?3 = '' or p.poolId <> ?3)")
	public int countPoolName(String prjId, String poolName, String poolId);
	
	@Query(value = "select prj_name from cloud_project where prj_id = ? limit 1", nativeQuery = true)
	public String getProjectName(String prjId);
	
	@Query(" select new com.eayun.virtualization.ecmcvo.CloudLdpoolVoe(t,dc.name, cp.prjName, c.cusId,c.cusName)"
			+ " from BaseCloudLdPool t, BaseCustomer c, BaseCloudProject cp, BaseDcDataCenter dc"
			+ " where t.prjId = cp.projectId and cp.customerId = c.cusId and t.dcId = dc.id"
			+ " and (t.dcId = :dcId or :dcId = null)"
			+ " and (CONCAT(:prjName) = null or cp.prjName in(:prjName))"
			+ " and (CONCAT(:cusName) = null or c.cusName in(:cusName))"
			+ " and (:poolName = null or t.poolName like %:poolName%)"
			+ " order by if ( isnull(t.createTime), 1, 0 ), t.createTime desc")
	public Page<CloudLdpoolVoe> findBy(@Param("dcId") String dcId, @Param("prjName") List<String> prjName, @Param("cusName") List<String> cusName, @Param("poolName") String poolName, Pageable pageable);
	/**
	 * 获取当前负载均衡器已经绑定的弹性公网ip
	 * @param resourceId
	 * @return
	 */
	@Query("from BaseCloudFloatIp where resourceId = ? and isDeleted = '0'")
    public BaseCloudFloatIp getFloatIpByPoolId(String resourceId);
}

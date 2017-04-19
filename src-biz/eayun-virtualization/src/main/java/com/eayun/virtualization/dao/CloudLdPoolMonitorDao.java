package com.eayun.virtualization.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eayun.common.dao.IRepository;
import com.eayun.virtualization.model.BaseCloudLdPoolMonitor;

public interface CloudLdPoolMonitorDao extends IRepository<BaseCloudLdPoolMonitor, String> {

    @Query("select count(m) from BaseCloudLdPoolMonitor m where m.poolId = ?1")
    int countByPool(String poolId);

    @Query("select new java.lang.String(pool.poolName as poolName)" + " from BaseCloudLdPoolMonitor ldmp, BaseCloudLdPool pool " + " where ldmp.poolId=pool.poolId" + " and pool.dcId = :dcId" + " and (:prjId = null or pool.prjId = :prjId)" + " and (:ldmId = null or ldmp.ldmId = :ldmId)")
    public List<String> findMonitorBindPoolNames(@Param("dcId") String dcId, @Param("prjId") String prjId, @Param("ldmId") String ldmId);

    public void deleteByLdmIdAndPoolId(String ldmId, String poolId);

}

package com.eayun.virtualization.dao;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eayun.common.dao.IRepository;
import com.eayun.virtualization.model.BaseCloudLdVip;

public interface CloudLdVipDao extends IRepository<BaseCloudLdVip, String> {
    @Query("select count(*) from BaseCloudLdVip t where t.prjId= ? ")
    public int getCountByPrjId(String prjId);

    @Query("select count(*) from BaseCloudLdVip cv where 1=1"
            + " and (:dcId = null or :dcId = '' or cv.dcId = :dcId)"
            + " and (:prjId = null or :prjId = '' or cv.prjId = :prjId)"
            + " and (:vipName = null or binary(cv.vipName) = :vipName)"
            + " and (:vipId = null or :vipId = '' or cv.vipId <> :vipId)")
    public int countVipNameRepeat(@Param("dcId") String dcId, @Param("prjId") String prjId, @Param("vipName") String vipName, @Param("vipId") String vipId);
    
    @Query("select new map(cs.subnetId as subnetId,cs.subnetName as subnetName,cs.netId as netId)"
            + " from BaseCloudSubNetWork cs, BaseCloudNetwork cn"
            + " where cs.netId = cn.netId and cn.routerExternal= '0'"
            + " and (:dcId = null or cs.dcId = :dcId)"
            + " and (:prjId = null or cs.prjId = :prjId)")
    public Page<Map<String, Object>> findSubNetwork(@Param("dcId") String dcId, @Param("prjId") String prjId, Pageable pageable);
}

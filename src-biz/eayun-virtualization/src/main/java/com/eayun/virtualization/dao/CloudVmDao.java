package com.eayun.virtualization.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eayun.common.dao.IRepository;
import com.eayun.virtualization.model.BaseCloudVm;

public interface CloudVmDao extends IRepository<BaseCloudVm, String> {

    @Query("from BaseCloudVm t where prjId=:prjId and isDeleted = '0' ")
    public List<BaseCloudVm> getUnDeletedListByProject(@Param("prjId") String prjId);
    
    @Query("select count(vmId) from BaseCloudVm t where t.prjId = ? and t.isDeleted = '0' ")
    public int getUnDeletedVmCountByPrjId(String prjId);
    
    /**
     * 获得状态为未删除的云主机总数
     * @author zengbo
     * @return
     */
    @Query("select count(*) from BaseCloudVm t where t.prjId is not null and t.isDeleted = '0'")
    public int getUnDeletedCount();
    
    @Query("from BaseCloudVm t where t.prjId = ? and t.vmStatus = ? and isDeleted = '0' ")
    public List<BaseCloudVm> getVmListByPrjIdAndVmStatus(String prjId,String vmStatus);
    
    /**
     * 获得状态为未删除且非active的云主机list
     * @author liyanchao
     * @return vmList
     */
    @Query("from BaseCloudVm t where prjId= ? and vmStatus != ? and isDeleted = '0' ")
    public List<BaseCloudVm> getNoActiveUnDeletedVmByPrjId(String prjId , String vmStatus);

    @Query("select count(vmId) from BaseCloudVm t where t.fromImageId = ? and (t.isDeleted = '0' or t.isDeleted = '2') ")
	public int countVmByImageId(String imageId);
}

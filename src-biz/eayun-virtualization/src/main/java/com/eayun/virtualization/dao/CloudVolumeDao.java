package com.eayun.virtualization.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eayun.common.dao.IRepository;
import com.eayun.virtualization.model.BaseCloudVolume;
import com.eayun.virtualization.model.CloudVolume;

public interface CloudVolumeDao extends IRepository<BaseCloudVolume, String> {

    @Query("from BaseCloudVolume t where prjId=:prjId and isDeleted = '0' ")
    public List<BaseCloudVolume> getUnDeletedListByProject(@Param("prjId") String prjId);
    
    @Query("from BaseCloudVolume t where dcId=:dcId and volName=:volName and isDeleted = '0' ")
	public BaseCloudVolume findVolumeByName(@Param("dcId") String dcId, @Param("volName")String volName);
    
    @Query("select count(*) from BaseCloudVolume bcv where bcv.vmId = ? and bcv.isDeleted = '0' ")
    public int getVmCount(String vmId);
    
    @Query("from BaseCloudVolume vol where vol.vmId = ? and vol.isDeleted = '0' ")
    public List<CloudVolume> getVolumesByVm(String vmId);
    
    @Query("select count(*) from BaseCloudVolume vol where vol.prjId = ? and vol.isDeleted='0'")
	public int getCountByPrjId(String prjId);
    
    
    @Query("from BaseCloudVolume t where volId=:volId  and isDeleted = '0' and isVisable='1' ")
   	public BaseCloudVolume isExistsByResourceId( @Param("volId")String volId);
    
    /**
     * 查询所有未删除状态的云硬盘数量
     * @author zengbo
     * @return
     */
    @Query("select count(*) from BaseCloudVolume vol where vol.prjId is not null and vol.isDeleted='0'")
	public int getUnDeletedCount();
}

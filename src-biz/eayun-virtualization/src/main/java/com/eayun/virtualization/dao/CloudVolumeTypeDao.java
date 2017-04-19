package com.eayun.virtualization.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eayun.common.dao.IRepository;
import com.eayun.virtualization.model.BaseCloudVolumeType;

public interface CloudVolumeTypeDao extends IRepository<BaseCloudVolumeType, String> {

    
    
    @Query("from BaseCloudVolumeType t where  dcId=:dcId  and volumeType=:volumeType")
	public BaseCloudVolumeType getTypeByDcId(@Param("dcId") String dcId, @Param("volumeType")String volumeType);

    @Query("from BaseCloudVolumeType t where  dcId=:dcId  and typeId=:typeId")
	public BaseCloudVolumeType getVolumeTypeByTypeId(@Param("dcId") String dcId, @Param("typeId")String typeId);

    @Query("from BaseCloudVolumeType t where  dcId=:dcId  and typeName=:typeName")
	public BaseCloudVolumeType getVolumeTypeByName(@Param("dcId")String dcId, @Param("typeName")String typeName);
    
 
}

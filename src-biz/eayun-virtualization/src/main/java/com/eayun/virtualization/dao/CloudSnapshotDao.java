package com.eayun.virtualization.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eayun.common.dao.IRepository;
import com.eayun.virtualization.model.BaseCloudSnapshot;
import com.eayun.virtualization.model.BaseCloudVolume;

public interface CloudSnapshotDao extends IRepository<BaseCloudSnapshot, String> {

    @Query("from BaseCloudSnapshot t where volId=:volId order by t.createTime desc")
	public List<BaseCloudSnapshot> getSnapListByVolId(@Param("volId") String volId);
    
    @Query("from BaseCloudSnapshot t where snapId=:snapId")
	public BaseCloudSnapshot getSnapshotById(@Param("snapId") String snapId);
    
    @Query("select count(*) from BaseCloudSnapshot snap where snap.prjId = ?")
	public int countSnapshotByPrjId(String prjId);
    
    @Query("from BaseCloudSnapshot t where snapId=:snapId  and isDeleted = '0' and isVisable='1' ")
   	public BaseCloudSnapshot isExistsByResourceId( @Param("snapId")String snapId);

    @Query("from BaseCloudSnapshot t where volId=:volId and isDeleted='0' and isVisable='1' order by t.createTime desc")
	public List<BaseCloudSnapshot> getUnDelSnapListByVolId(@Param("volId") String volId);
}

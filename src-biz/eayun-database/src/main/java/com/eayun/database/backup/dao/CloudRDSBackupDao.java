package com.eayun.database.backup.dao;

import com.eayun.common.dao.IRepository;
import com.eayun.database.backup.model.BaseCloudRDSBackup;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface CloudRDSBackupDao extends IRepository<BaseCloudRDSBackup, String> {
    @Modifying
    @Query(" update BaseCloudRDSBackup set name=? where backupId=? ")
    void updateBackup(String name, String backupId);

    @Modifying
    @Query(" update BaseCloudRDSBackup set instanceExist=? , instanceDeleteTime=? where instanceId=?")
    void updateBackupStatus(String instanceExist, Date instanceDeletTime, String instanceId);

    @Query(" from BaseCloudRDSBackup where instanceId=? and category=? and isVisible = 1 order by createTime asc")
    List<BaseCloudRDSBackup> getBackupListByInstanceId(String instanceId, String category);

    @Query(" select count(b) from BaseCloudRDSBackup b where b.instanceId=? and b.category=?")
    int getCurrentManualBackupCount(String instanceId, String category);

    @Query(" select count(b) from BaseCloudRDSBackup b where b.instanceId=? and b.createTime>=? and b.createTime<? and b.category=?")
    int getBackupsCountByTimeRange(String instanceId, Date zeroToday, Date currentTime, String category);

    @Query(" from BaseCloudRDSBackup where instanceId=? and category=? order by createTime asc")
    List<BaseCloudRDSBackup> getBackups(String instanceId, String category);

    @Query(value = " select * from cloud_rdsbackup where instance_id=? and category=? and parent_id is null order by create_time asc limit 1", nativeQuery = true)
    BaseCloudRDSBackup getEarliestFullBackup(String instanceId, String category);

    @Query(" from BaseCloudRDSBackup where parentId=? and category=? ")
    List<BaseCloudRDSBackup> getChildrenBackups(String backupId, String category);

    @Query(" from BaseCloudRDSBackup where parentId is null and instanceExist=?")
    List<BaseCloudRDSBackup> getOrphanedBackups( String instanceExist);
}

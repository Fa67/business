package com.eayun.database.backup.dao;

import com.eayun.common.dao.IRepository;
import com.eayun.database.backup.model.BaseCloudRDSBackupSchedule;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CloudRDSBackupScheduleDao extends IRepository<BaseCloudRDSBackupSchedule, String> {
    @Modifying
    @Query("update BaseCloudRDSBackupSchedule set isEnabled=? where instanceId=? ")
    int updateBackupScheduleStatus(String isEnabled, String instanceId);

    @Modifying
    @Query("delete BaseCloudRDSBackupSchedule where instanceId=? ")
    void deleteBackupScheduleByInstanceId(String instanceId);

    @Query(" from BaseCloudRDSBackupSchedule where instanceId=?")
    BaseCloudRDSBackupSchedule getBackupScheduleByInstanceId(String instanceId);

    @Query(" from BaseCloudRDSBackupSchedule where isEnabled=?")
    List<BaseCloudRDSBackupSchedule> getEnabledBackupSchedules(String isEnabled);

    @Modifying
    @Query("update BaseCloudRDSBackupSchedule set scheduleTime=? where instanceId=? ")
    int modifyScheduleTime(String scheduleTime, String instanceId);
}

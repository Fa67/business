package com.eayun.database.backup.service;

import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.database.backup.model.BaseCloudRDSBackup;
import com.eayun.database.backup.model.CloudRDSBackup;

import java.util.Map;

/**
 * ECMC云数据库备份Service接口
 * @author fan.zhang
 */
public interface EcmcRDSBackupService{
    Page getBackups(Page page, ParamsMap map, SessionUserInfo sessionUser, QueryMap queryMap) throws Exception;

    Page getBackupsByInstanceId(Page page, ParamsMap map, QueryMap queryMap) throws Exception;

    CloudRDSBackup createBackup(String datacenterId, String projectId, String name, String instanceId, String parentId, String category) throws Exception;

    void updateBackup(String backupId, String name) throws Exception;

    Map<String, Object> deleteBackup(String backupId);

    int enableAutomaticBackup(String instanceId) throws Exception;

    int disableAutomaticBackup(String instanceId) throws Exception;

    boolean verifyBackupName(String datacenterId, String projectId, String backupName) throws Exception;

    void addBackupSchedule(String datacenterId, String projectId, String customerId, String instanceId) throws Exception;

    CloudRDSBackup syncBackupStatus(String datacenterId, String projectId, String backupId, String category) throws Exception;

    int modifyScheduleTime(String instanceId, String scheduleTime) throws Exception;

    long getCurrentManualBackupCount(String instanceId) throws Exception;

    long getMaxManualBackupCount(String projectId) throws Exception;

    Map<String,String> getAutoBackupEnableStatus(String instanceId) throws Exception;

    Map<String,String> getInfoForLog(String instanceId);

    String getInstanceNameById(String instanceId);
}

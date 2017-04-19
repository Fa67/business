package com.eayun.database.backup.service;

import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.database.backup.model.BaseCloudRDSBackup;
import com.eayun.database.backup.model.CloudRDSBackup;
import com.eayun.database.backup.model.CloudRDSBackupSchedule;
import com.eayun.eayunstack.model.RDSBackup;

import java.util.List;
import java.util.Map;

/**
 * ECSC云数据库备份Service接口
 * @author fan.zhang
 */
public interface RDSBackupService {
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

    List<BaseCloudRDSBackup> getBackupListByInstanceId(String instanceId, String category) throws Exception;

    void updateBackup(BaseCloudRDSBackup backup) throws Exception;

    long getMaxManualBackupCount(String projectId) throws Exception;

    long getCurrentManualBackupCount(String instanceId) throws Exception;

    void handleBackupsOfDeletedInstance(String instanceId) throws Exception;

    Map<String, String> getAutoBackupEnableStatus(String instanceId) throws Exception;

    List<CloudRDSBackupSchedule> getEnabledBackupSchedules() throws Exception;

    boolean isAutoBackupedToday(String instanceId) throws Exception;

    List<BaseCloudRDSBackup> getBackups(String instanceId, String category) throws Exception;

    BaseCloudRDSBackup getEarliestAutoFullBackup(String instanceId) throws Exception;

    List<BaseCloudRDSBackup> getChildrenBackups(String rootBackupId) throws Exception;

    Map<String, String> getConfigurationInfo(String backupId) throws Exception;

    Map<String,String> getInfoForLog(String instanceId);

    List<BaseCloudRDSBackup> getOrphanedBackups();

    Map<String, Object> clearChildrenBackupsRecursively(BaseCloudRDSBackup parentBackup, List<BaseCloudRDSBackup> backupList);

    void notifyDevOps(CloudRDSBackup cloudBackup);

    void notifyDevOps(BaseCloudRDSBackup backup, List<BaseCloudRDSBackup> backupsList);

    void findAllParentBackups(List<BaseCloudRDSBackup> parentsList, BaseCloudRDSBackup bak, List<BaseCloudRDSBackup> backupsList);

    void deleteBackup(BaseCloudRDSBackup backup);

    void notifyDevOps(String prjId, List<RDSBackup> failDeletingBackups);

    String getInstanceNameById(String instanceId);
}

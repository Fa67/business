package com.eayun.database.backup.service.impl;

import com.eayun.common.exception.AppException;
import com.eayun.common.util.BeanUtils;
import com.eayun.database.backup.bean.BackupCategory;
import com.eayun.database.backup.dao.CloudRDSBackupDao;
import com.eayun.database.backup.dao.CloudRDSBackupScheduleDao;
import com.eayun.database.backup.model.BaseCloudRDSBackup;
import com.eayun.database.backup.model.BaseCloudRDSBackupSchedule;
import com.eayun.database.backup.model.CloudRDSBackupSchedule;
import com.eayun.database.backup.service.RDSBackupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.Query;
import java.util.*;

/**
 * ECSC RDS 备份Service实现类
 *
 * @author fan.zhang
 */
@Service
@Transactional
public class RDSBackupServiceImpl extends BaseRDSBackupService implements RDSBackupService {

    private static final Logger log = LoggerFactory.getLogger(RDSBackupServiceImpl.class);

    @Autowired
    private CloudRDSBackupDao rdsBackupDao;
    @Autowired
    private CloudRDSBackupScheduleDao rdsBackupScheduleDao;

    @Override
    public List<CloudRDSBackupSchedule> getEnabledBackupSchedules() throws Exception {
        List<CloudRDSBackupSchedule> schedules = new ArrayList<>();
        List<BaseCloudRDSBackupSchedule> baseSchedules = rdsBackupScheduleDao.getEnabledBackupSchedules("1");
        for (int i = 0; i < baseSchedules.size(); i++) {
            CloudRDSBackupSchedule schedule = new CloudRDSBackupSchedule();
            BeanUtils.copyProperties(schedule, baseSchedules.get(i));
            schedules.add(schedule);
        }
        return schedules;
    }

    @Override
    public boolean isAutoBackupedToday(String instanceId) throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date zeroToday = cal.getTime();
        Date currentTime = new Date();
//        int count = 0;//fixme 为了测试自动备份数量达到指定数量时的处理，强行当天自动备份为0，不去校验当天是否已经做过自动备份 不过该判断已经经过了测试
        int count = rdsBackupDao.getBackupsCountByTimeRange(instanceId, zeroToday, currentTime, "AUTO");
        return count == 0 ? false : true;
    }


    @Override
    public List<BaseCloudRDSBackup> getBackups(String instanceId, String category) throws Exception {
        List<BaseCloudRDSBackup> baseBackups = rdsBackupDao.getBackups(instanceId, category);
        return baseBackups;
    }

    @Override
    public BaseCloudRDSBackup getEarliestAutoFullBackup(String instanceId) throws Exception {
        BaseCloudRDSBackup baseBackup = rdsBackupDao.getEarliestFullBackup(instanceId, BackupCategory.AUTO);
        return baseBackup;
    }

    @Override
    public List<BaseCloudRDSBackup> getChildrenBackups(String backupId) throws Exception {
        List<BaseCloudRDSBackup> childrenBackups = rdsBackupDao.getChildrenBackups(backupId, BackupCategory.AUTO);
        return childrenBackups;
    }

    /*@Override
    public Map<String, String> getConfigurationInfo(String backupId) throws Exception {
        //根据backupId联查cloud_rdsconfigfile、cloud_datastore、cloud_datastoreversion，取得configId，configName，versionId，versionName
        StringBuilder sb = new StringBuilder();
        sb.append(" SELECT                                                        ");
        sb.append("    conf.config_id as config_id,                               ");
        sb.append("    config_name as config_name,                                ");
        sb.append("    ds.name as datastore_name,                                 ");
        sb.append("    bak.version_id as version_id,                              ");
        sb.append("    ver.name as version_name,                                  ");
        sb.append("    bak.size as size                                           ");
        sb.append(" FROM                                                          ");
        sb.append("    cloud_rdsbackup bak                                        ");
        sb.append("        LEFT JOIN                                              ");
        sb.append("    cloud_rdsconfigfile conf ON bak.config_id = conf.config_id ");
        sb.append("        LEFT JOIN                                              ");
        sb.append("    cloud_datastoreversion ver ON bak.version_id = ver.id      ");
        sb.append("        LEFT JOIN                                              ");
        sb.append("    cloud_datastore ds ON ver.datastore_id = ds.id             ");
        sb.append(" WHERE                                                         ");
        sb.append("    bak.backup_id = ?                                          ");
        Query query = rdsBackupDao.createSQLNativeQuery(sb.toString(), backupId);
        Object[] objs = (Object[]) query.getResultList().get(0);
        String configId = String.valueOf(objs[0]);
        String configName = String.valueOf(objs[1]);
        String datastoreName = beautify(String.valueOf(objs[2]));
        String versionId = String.valueOf(objs[3]);
        String versionName = String.valueOf(objs[4]);
        String size = String.valueOf(objs[5]);
        Map<String, String> info = new HashMap<>();
        info.put("configurationId", configId);
        info.put("configurationName", configName);
        info.put("versionInfo", datastoreName + " " + versionName);
        info.put("versionId", versionId);
        info.put("size", size);
        return info;
    }*/
    @Override
    public Map<String, String> getConfigurationInfo(String backupId) throws Exception {
        //根据backupId联查cloud_rdsconfigfile、cloud_datastore、cloud_datastoreversion，取得configId，configName，versionId，versionName
        StringBuilder sb = new StringBuilder();
        sb.append(" SELECT                                                                                 ");
        sb.append("    conf.config_id as config_id,                                                        ");
        sb.append("    config_name as config_name,                                                         ");
        sb.append("    ds.name as datastore_name,                                                          ");
        sb.append("    bak.version_id as version_id,                                                       ");
        sb.append("    ver.name as version_name,                                                           ");
        sb.append("    bak.size as size                                                                    ");
        sb.append(" FROM                                                                                   ");
        sb.append("    cloud_rdsbackup bak                                                                 ");
        sb.append("        LEFT JOIN                                                                       ");
        sb.append("    cloud_datastoreversion ver ON bak.version_id = ver.id                               ");
        sb.append("        LEFT JOIN                                                                       ");
        sb.append("    cloud_rdsconfigfile conf ON ver.id = conf.config_version AND conf.config_type = '1' ");
        sb.append("        LEFT JOIN                                                                       ");
        sb.append("    cloud_datastore ds ON ver.datastore_id = ds.id                                      ");
        sb.append(" WHERE                                                                                  ");
        sb.append("    bak.backup_id = ?                                                                   ");
        Query query = rdsBackupDao.createSQLNativeQuery(sb.toString(), backupId);
        Object[] objs = (Object[]) query.getResultList().get(0);
        String configId = String.valueOf(objs[0]);
        String configName = String.valueOf(objs[1]);
        String datastoreName = beautify(String.valueOf(objs[2]));
        String versionId = String.valueOf(objs[3]);
        String versionName = String.valueOf(objs[4]);
        String size = String.valueOf(objs[5]);
        Map<String, String> info = new HashMap<>();
        info.put("configurationId", configId);
        info.put("configurationName", configName);
        info.put("versionInfo", datastoreName + " " + versionName);
        info.put("versionId", versionId);
        info.put("size", size);
        return info;
    }

    @Override
    public List<BaseCloudRDSBackup> getOrphanedBackups() {
        //获取备份列表中来源实例已删除的全量备份
        List<BaseCloudRDSBackup> backups = rdsBackupDao.getOrphanedBackups("0");
        return backups;
    }

    private String beautify(String type) {
        String result = "";
        switch (type) {
            case "mysql":
                result = "MySQL";
                break;
        }
        return result;
    }

    @Transactional(noRollbackFor = {AppException.class})
    @Override
    public Map<String, Object> clearChildrenBackupsRecursively(BaseCloudRDSBackup parentBackup, List<BaseCloudRDSBackup> backupList) {
        Map<String, Object> map = null;
        for (int i = 0; i < backupList.size(); i++) {
            BaseCloudRDSBackup backup = backupList.get(i);
            String parentBackupId = backup.getParentId();
            String backupId = backup.getBackupId();
            if (!StringUtils.isEmpty(parentBackupId)) {
                //仅针对孩子备份进行处理
                if (parentBackup.getBackupId().equals(parentBackupId)) {
                    //如果当前备份是parentBackup的孩子，则继续找它的孩子，否则不处理
                    map = clearChildrenBackupsRecursively(backup, backupList);
                    if(map!=null){
                        boolean isSuccess = (boolean) map.get("isSuccess");
                        if (!isSuccess) {
                            log.info("删除子备份[" + backupId + "]失败，递归删除中断退出");
                            break;
                        }
                    }
                    log.info("开始删除子备份[" + backupId + "]，父备份为[" + parentBackupId + "]");
                    map = deleteBackup(backupId);
                }
            }
        }
        return map;
    }

    @Override
    public void findAllParentBackups(List<BaseCloudRDSBackup> parentsList, BaseCloudRDSBackup bak, List<BaseCloudRDSBackup> backupsList) {
        for(int i=0; i<backupsList.size(); i++){
            BaseCloudRDSBackup b =  backupsList.get(i);
            if(b.getBackupId().equals(bak.getParentId())){
                //如果当前备份是删除失败备份的父备份，则继续找它的父备份
                findAllParentBackups(parentsList, b, backupsList);
                //找不到父备份之后，将这个备份添加到parentsList中
                parentsList.add(b);
            }
        }
    }
}

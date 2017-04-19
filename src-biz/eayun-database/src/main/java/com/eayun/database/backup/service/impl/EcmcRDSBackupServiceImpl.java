package com.eayun.database.backup.service.impl;

import com.eayun.database.backup.dao.CloudRDSBackupDao;
import com.eayun.database.backup.dao.CloudRDSBackupScheduleDao;
import com.eayun.database.backup.model.CloudRDSBackupSchedule;
import com.eayun.database.backup.service.*;
import com.eayun.eayunstack.model.RDSBackup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * ECMC云数据库备份Service抽象类，实现ECMC备份Service接口
 * @author fan.zhang
 */
@Service
@Transactional
public class EcmcRDSBackupServiceImpl extends BaseRDSBackupService implements EcmcRDSBackupService {

    @Autowired
    private CloudRDSBackupScheduleDao backupScheduleDao;
    @Override
    public int modifyScheduleTime(String instanceId, String scheduleTime) throws Exception {
        int count = backupScheduleDao.modifyScheduleTime(scheduleTime, instanceId);
        return count;
    }
}

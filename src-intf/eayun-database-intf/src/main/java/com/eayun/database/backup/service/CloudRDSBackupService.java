package com.eayun.database.backup.service;

import com.eayun.datacenter.model.BaseDcDataCenter;

/**
 * 云数据库备份底层数据同步接口类
 * @author fan.zhang
 */
public interface CloudRDSBackupService {
    void syncData(BaseDcDataCenter dataCenter, String prjId) throws Exception;
}

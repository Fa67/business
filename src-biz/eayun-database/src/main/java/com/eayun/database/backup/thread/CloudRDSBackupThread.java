package com.eayun.database.backup.thread;

import com.eayun.common.exception.AppException;
import com.eayun.database.backup.service.CloudRDSBackupService;
import com.eayun.datacenter.model.BaseDcDataCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * 云数据库备份底层数据同步线程
 * @author fan.zhang
 */
public class CloudRDSBackupThread implements Callable<String> {
    private static final Logger log = LoggerFactory.getLogger(CloudRDSBackupThread.class);
    private BaseDcDataCenter dataCenter;
    private String projectId;
    private CloudRDSBackupService service;

    public CloudRDSBackupThread(CloudRDSBackupService service) {
        this.service = service;
    }

    public BaseDcDataCenter getDataCenter() {
        return dataCenter;
    }

    public void setDataCenter(BaseDcDataCenter dataCenter) {
        this.dataCenter = dataCenter;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    @Override
    public String call() throws Exception {
        log.info("执行项目ID:"+projectId+"下云数据库备份同步---开始");
        try {
            service.syncData(dataCenter, projectId);
            log.info("执行项目ID:"+projectId+"下云数据库备份同步---结束");
            return "success";
        } catch (AppException e) {
            log.error("执行项目ID:"+projectId+"下云数据库备份同步出错:" + e.getMessage(),e);
            return "failed";
        }
    }
}

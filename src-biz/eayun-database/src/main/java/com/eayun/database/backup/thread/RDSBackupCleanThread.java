package com.eayun.database.backup.thread;

import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.database.backup.bean.BackupCategory;
import com.eayun.database.backup.model.BaseCloudRDSBackup;
import com.eayun.database.backup.model.CloudRDSBackup;
import com.eayun.database.backup.service.RDSBackupService;
import com.eayun.notice.service.MessageCenterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 来源实例已被删除的备份的自动清理线程
 *
 * @author fan.zhang
 */
public class RDSBackupCleanThread implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(RDSBackupCleanThread.class);
    private RDSBackupService rdsBackupService;
    private MessageCenterService messageCenterService;
    private BaseCloudRDSBackup backup;
    private Date currentTime;

    public RDSBackupCleanThread(RDSBackupService rdsBackupService, MessageCenterService messageCenterService, BaseCloudRDSBackup backup, Date currentTime) {
        this.rdsBackupService = rdsBackupService;
        this.messageCenterService = messageCenterService;
        this.backup = backup;
        this.currentTime = currentTime;
    }

    @Override
    public void run() {
        //首先判断备份来源实例删除时间是否距当前时间大于三天
        Date instanceDeleteTime = backup.getInstanceDeleteTime();
        boolean isMoreThan3Days = isMoreThan3Days(instanceDeleteTime, currentTime);
        if (isMoreThan3Days) {
            //判断备份类型，手动或自动区别处理
            String category = backup.getCategory();
            if (category.equals(BackupCategory.MANUAL)) {
                //手动备份直接删除即可
                cleanBackup(backup);
            } else if (category.equals(BackupCategory.AUTO)) {
                cleanAutoBackup();
            }
        } else {
            log.info("备份[" + backup.getBackupId() + "]的实例删除时间为[" + backup.getInstanceDeleteTime() + "]，距当前时间小于三天，不处理。");
        }
    }

    private void cleanAutoBackup() {
        //递归删除该全量自动备份的孩子备份，为了减少查询数据库的次数，这里统一查出所有的来源实例被删除的备份列表，递归在内存中查询
        log.info("全量自动备份[" + backup.getBackupId() + "]的实例删除时间超过三天，处理备份开始");
        try {
            log.info("获取实例[" + backup.getInstanceId() + "]的全部备份列表");
            List<BaseCloudRDSBackup> backupsList = rdsBackupService.getBackups(backup.getInstanceId(), BackupCategory.AUTO);
            log.info("开始处理全量自动备份[" + backup.getBackupId() + "]的子备份");
            Map<String, Object> map = rdsBackupService.clearChildrenBackupsRecursively(this.backup, backupsList);
            if (map != null) {
                //map不为空表示该全量备份有孩子备份且孩子备份被处理
                boolean isSuccess = (boolean) map.get("isSuccess");
                if (isSuccess) {
                    log.info("全量自动备份[" + backup.getBackupId() + "]的孩子备份处理成功，开始处理该全量备份");
                    cleanBackup(backup);
                } else {
                    //根据返回map中最早失败的备份，根据实例的backupsList，找到它的父备份们(肯定也是删除失败的，因为子备份删除失败)，然后组织内容，发邮件。
                    log.info("全量自动备份[" + backup.getBackupId() + "]的孩子备份处理失败，开始组织邮件通知运维和管理员");
                    CloudRDSBackup bak = (CloudRDSBackup) map.get("rdsBackup");
                    List<BaseCloudRDSBackup> parentsList = new ArrayList<>();
                    rdsBackupService.findAllParentBackups(parentsList, bak, backupsList);
                    //找到这个删除失败的所有父畚份之后，也要记得把这个删除失败的备份传递过去
                    rdsBackupService.notifyDevOps(bak, parentsList);
                    //无论底层删除成功还是失败，都要把数据库记录删除
                    this.deleteDBRecords(bak, parentsList);
                }
            } else {
                //map为空，则表示递归清除孩子备份时，该全量备份无孩子备份
                log.info("全量自动备份[" + backup.getBackupId() + "]无孩子备份，开始处理该全量备份");
                cleanBackup(backup);
            }
        } catch (Exception e) {
            log.error("全量自动备份[" + backup.getBackupId() + "]的实例删除时间超过三天，处理备份异常", e);
        }
    }

    private void deleteDBRecords(CloudRDSBackup bak, List<BaseCloudRDSBackup> parentsList) {
        BaseCloudRDSBackup baseBak = new BaseCloudRDSBackup();
        try {
            BeanUtils.copyProperties(baseBak, bak);
            rdsBackupService.deleteBackup(baseBak);
        } catch (IllegalAccessException e) {
            log.error(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            log.error(e.getMessage(), e);
        }
        for (int i = 0; i < parentsList.size(); i++) {
            BaseCloudRDSBackup b = parentsList.get(i);
            rdsBackupService.deleteBackup(b);
        }
    }

    private void cleanBackup(BaseCloudRDSBackup backup) {
        try {
            Map<String, Object> result = rdsBackupService.deleteBackup(backup.getBackupId());
            boolean isDone = (boolean) result.get("isSuccess");
            if (isDone) {
                log.info("备份[" + backup.getBackupId() + "]的实例删除时间超过三天，已成功清理。");
            } else {
                log.info("备份[" + backup.getBackupId() + "]的实例删除时间超过三天，清理失败。");
                //组织内容发邮件
                CloudRDSBackup cloudBackup = (CloudRDSBackup) result.get("rdsBackup");
                rdsBackupService.notifyDevOps(cloudBackup);

            }
        } catch (Exception e) {
            log.error("备份[" + backup.getBackupId() + "]的实例删除时间超过三天，清理异常。", e);
        }
    }

    private boolean isMoreThan3Days(Date instanceDeleteTime, Date currentTime) {
        Date threeDaysAgo = DateUtil.addDay(currentTime, new int[]{0, 0, -3});
        return instanceDeleteTime.before(threeDaysAgo);
    }
}

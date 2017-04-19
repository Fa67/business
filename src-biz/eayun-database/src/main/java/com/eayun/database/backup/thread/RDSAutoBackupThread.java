package com.eayun.database.backup.thread;

import com.eayun.common.ConstantClazz;
import com.eayun.database.backup.bean.BackupCategory;
import com.eayun.database.backup.bean.BackupStatus;
import com.eayun.database.backup.model.BaseCloudRDSBackup;
import com.eayun.database.backup.model.CloudRDSBackupSchedule;
import com.eayun.database.backup.service.RDSBackupService;
import com.eayun.database.instance.model.CloudRDSInstance;
import com.eayun.database.instance.service.RDSInstanceService;
import com.eayun.notice.service.MessageCenterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * RDS自动备份线程
 *
 * @author fan.zhang
 */
public class RDSAutoBackupThread implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(RDSAutoBackupThread.class);
    private RDSBackupService rdsBackupService;
    private RDSInstanceService rdsInstanceService;
    private MessageCenterService messageCenterService;
    private CloudRDSBackupSchedule schedule;
    private Date currentHour;

    public RDSAutoBackupThread(RDSBackupService rdsBackupService, RDSInstanceService rdsInstanceService, MessageCenterService messageCenterService, CloudRDSBackupSchedule schedule, Date currentHour) {
        this.rdsBackupService = rdsBackupService;
        this.rdsInstanceService = rdsInstanceService;
        this.messageCenterService = messageCenterService;
        this.schedule = schedule;
        this.currentHour = currentHour;
    }

    @Override
    public void run() {
        String instanceId = schedule.getInstanceId();
        String scheduleTime = schedule.getScheduleTime();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String currentHourStr = sdf.format(currentHour);
        log.info("实例[" + instanceId + "]备份计划执行开始");
        try {
            if (!StringUtils.isEmpty(scheduleTime) && scheduleTime.equals(currentHourStr)) {
                log.info("实例[" + instanceId + "]当前时间符合备份计划时间，自动备份开始");
                boolean isAutoBackupedToday = rdsBackupService.isAutoBackupedToday(instanceId);
                CloudRDSInstance instance = rdsInstanceService.findRDSInstanceByRdsId(instanceId);
                String instanceStatus = instance.getRdsStatus();
                if (isAutoBackupedToday) {
                    log.info("实例[" + instanceId + "]当天已有自动备份[" + isAutoBackupedToday + "]，自动备份任务结束。");
                }else if(!instanceStatus.equals("ACTIVE")){
                    log.info("实例[" + instanceId + "]自动备份任务执行时实例状态为[" + instanceStatus + "]，自动备份任务结束。");
                    log.info("向客户发送站内信通知...");
                    //发邮件通知客户实例自动备份执行失败，因为当前实例状态不满足备份条件
                    messageCenterService.cloudDataBaseBackupNoStart(instance.getPrjId(), instance.getRdsName(), new Date());
                } else {
                    //获取当前实例所有的自动备份列表，含可见和不可见的，并且以创建时间升序排列
                    List<BaseCloudRDSBackup> autoBackups = rdsBackupService.getBackups(instanceId, BackupCategory.AUTO);

                    Date date = new Date();
                    SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
                    String timestamp = format.format(date);
                    String backupName = instance.getRdsName() + "_autobackup_" + timestamp;

                    BaseCloudRDSBackup previousBackup = null;
                    int currentAutoBackupsCount = autoBackups.size();
                    if(currentAutoBackupsCount>=1){
                        previousBackup = autoBackups.get(currentAutoBackupsCount-1);
                    }
                    if(previousBackup!=null && !previousBackup.getStatus().equals(BackupStatus.COMPLETED)){
                        //如果之前有自动备份，且自动备份状态不是完成状态，则本次自动备份需要做一个全量备份
                        rdsBackupService.createBackup(schedule.getDatacenterId(), schedule.getProjectId(), backupName, instanceId, null, BackupCategory.AUTO);
                    }else{
                        //如果之前没有备份或者前一个自动备份状态是完成状态，则本次自动备份按照正常逻辑走
                        if (currentAutoBackupsCount % ConstantClazz.RDS_MAX_AUTO_BACKUP == 0) {
                            //基于实例做悬梁备份
                            rdsBackupService.createBackup(schedule.getDatacenterId(), schedule.getProjectId(), backupName, instanceId, null, BackupCategory.AUTO);
                        } else {
                            //找到最新的可用备份，做增量备份
                            for (int i = autoBackups.size() - 1; i >= 0; i--) {
                                BaseCloudRDSBackup backup = autoBackups.get(i);
                                if (BackupStatus.COMPLETED.equals(backup.getStatus())) {
                                    String parentId = backup.getBackupId();
                                    rdsBackupService.createBackup(schedule.getDatacenterId(), schedule.getProjectId(), backupName, instanceId, parentId, BackupCategory.AUTO);
                                    break;
                                }
                            }
                        }
                    }
                }
            } else {
                log.info("实例[" + instanceId + "]当前时间不符合备份计划时间，结束");
            }
        } catch (Exception e) {
            log.error("实例[" + instanceId + "]备份计划执行失败", e);
        }
    }
}

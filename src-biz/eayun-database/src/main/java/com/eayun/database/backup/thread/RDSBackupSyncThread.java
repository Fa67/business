package com.eayun.database.backup.thread;


import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.database.backup.bean.BackupCategory;
import com.eayun.database.backup.bean.BackupStatus;
import com.eayun.database.backup.model.BaseCloudRDSBackup;
import com.eayun.database.backup.model.CloudRDSBackup;
import com.eayun.database.backup.service.RDSBackupService;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.project.service.ProjectService;
import com.eayun.virtualization.model.CloudProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * RDS备份状态同步线程
 *
 * @author fan.zhang
 */
public class RDSBackupSyncThread implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(RDSBackupSyncThread.class);
    private RDSBackupService rdsBackupService;
    private ProjectService projectService;
    private MessageCenterService messageCenterService;
    private JedisUtil jedisUtil;
    private static final int MAX_RETRY_COUNT = 720;

    public RDSBackupSyncThread(RDSBackupService rdsBackupService, ProjectService projectService, MessageCenterService messageCenterService, JedisUtil jedisUtil) {
        this.rdsBackupService = rdsBackupService;
        this.projectService = projectService;
        this.messageCenterService = messageCenterService;
        this.jedisUtil = jedisUtil;
    }

    @Override
    public void run() {
        //1.在队列中取出每个待状态同步的备份任务
        //2.检查备份状态是否为COMPLETED或者FAILED，即是否备份结束
        //3.如果没有备份结束，需要检查次数，超过720次移除调度队列
        try {
            String taskStr = jedisUtil.pop(RedisKey.rdsBackupSyncKey);
            log.info("在云数据库备份状态同步队列中取出：" + taskStr);
            if (taskStr != null && !taskStr.equals("")) {
                JSONObject task = JSONObject.parseObject(taskStr);
                String datacenterId = task.getString("datacenter_id");
                String projectId = task.getString("project_id");
                String backupId = task.getString("backup_id");
                String category = task.getString("category");
                int retryCount = task.getIntValue("retry_count");

                CloudRDSBackup cloudRDSBackup = rdsBackupService.syncBackupStatus(datacenterId, projectId, backupId, category);
                retryCount++;

                CloudProject project = projectService.findProject(projectId);

                if (BackupStatus.COMPLETED.equals(cloudRDSBackup.getStatus())
                        || BackupStatus.FAILED.equals(cloudRDSBackup.getStatus())
                        || BackupStatus.DELETE_FAILED.equals(cloudRDSBackup.getStatus())) {
                    log.info("云数据库备份状态刷新完成，备份[" + backupId + "]状态为:" + cloudRDSBackup.getStatus());
                    //保存状态刷新完成的备份信息——status、size、locationRef
                    BaseCloudRDSBackup baseBackup = new BaseCloudRDSBackup();
                    BeanUtils.copyProperties(baseBackup, cloudRDSBackup);
                    rdsBackupService.updateBackup(baseBackup);
                    //备份内容更新完毕，根据当前备份类型，获取项目表中的配额，处理当前实例已有的备份
                    postProcess(project, cloudRDSBackup);

                } else if (BackupStatus.NEW.equals(cloudRDSBackup.getStatus()) || BackupStatus.BUILDING.equals(cloudRDSBackup.getStatus())) {
                    //备份还在备份中，检查当前重试次数
                    if (retryCount < MAX_RETRY_COUNT) {
                        task.put("retry_count", retryCount);
                        log.info("备份状态未刷新，已尝试同步状态[" + retryCount + "]次，等待下次调度。");
                        jedisUtil.push(RedisKey.rdsBackupSyncKey, task.toJSONString());
                    } else {
                        log.info("备份状态为刷新，已尝试同步状态[" + retryCount + "]次，移除队列，结束刷新！");
                    }
                }
            }
        } catch (Exception e) {
            log.error("云数据库备份状态刷新失败", e);
        }
    }

    private void postProcess(CloudProject project, CloudRDSBackup cloudRDSBackup) {
        String category = cloudRDSBackup.getCategory();
        int maxManualCount = project.getMaxBackupByHand();
        int maxAutoCount = project.getMaxBackupByAuto();
        String instanceId = cloudRDSBackup.getInstanceId();
        if (BackupCategory.MANUAL.equals(category)) {
            processManualBackups(maxManualCount, instanceId);
        } else if (BackupCategory.AUTO.equals(category)) {
            processAutoBackups(maxAutoCount, instanceId);
        }

    }

    private void processAutoBackups(int maxAutoCount, String instanceId) {
        log.info("备份成功后处理自动备份");
        try {
            List<BaseCloudRDSBackup> currentAutoBackupList = rdsBackupService.getBackups(instanceId, BackupCategory.AUTO);
            int currentAutoBackupCount = currentAutoBackupList.size();
            if (maxAutoCount < currentAutoBackupCount && currentAutoBackupCount < (2 * ConstantClazz.RDS_MAX_AUTO_BACKUP + 1)) {
                //将超过配额数量的最早的可见的备份置为不可见
                int count = currentAutoBackupCount - maxAutoCount;
                setExtraBackupInvisible(currentAutoBackupList, count);
            } else if (currentAutoBackupCount >= (2 * ConstantClazz.RDS_MAX_AUTO_BACKUP + 1)) {
                //因为第八个自动备份一定是全量备份，所以当达到15个自动备份时，要删掉前起个自动备份，保证当前有一个全量+六个增量+一个全量
                List<BaseCloudRDSBackup> subBackups = currentAutoBackupList.subList(0, 7);
                deleteExtraBackups(subBackups);
                //删除掉多余的备份后，需要在子处理一下自动备份，把超过配额的置为不可见
                processAutoBackups(maxAutoCount, instanceId);
            }
        } catch (Exception e) {
            log.error("备份成功后处理自动备份异常", e);
        }
    }

    @Transactional(noRollbackFor = {AppException.class})
    private void deleteExtraBackups(List<BaseCloudRDSBackup> subBackups) {
        List<BaseCloudRDSBackup> parentBackups = new ArrayList<>();
        for (int i = subBackups.size() - 1; i >= 0; i--) {
            BaseCloudRDSBackup backup = subBackups.get(i);
            Map<String, Object> result = rdsBackupService.deleteBackup(backup.getBackupId());
            boolean isSuccess = (boolean) result.get("isSuccess");
            if (!isSuccess) {
                //其实如果这里删除某一个孩子失败，后面的删除也可以不用进行了，它的父备份一定也是删除失败的
                BaseCloudRDSBackup rdsBackup = (BaseCloudRDSBackup) result.get("rdsBackup");
                //首先找到所有的父备份，然后将这个子备份一并传递给邮件通知接口
                rdsBackupService.findAllParentBackups(parentBackups, rdsBackup, subBackups);
                //取得底层删除失败的列表，组织邮件，发送给运维和管理员 调用消息中心接口
                rdsBackupService.notifyDevOps(rdsBackup, parentBackups);
                //删除失败也要讲这个备份和失败的父备份数据库记录删掉
                this.deleteDBRecords(rdsBackup, parentBackups);

                break;
            }
        }
    }

    private void deleteDBRecords(BaseCloudRDSBackup rdsBackup, List<BaseCloudRDSBackup> parentBackups) {
        BaseCloudRDSBackup baseBak = new BaseCloudRDSBackup();
        try {
            BeanUtils.copyProperties(baseBak, rdsBackup);
            rdsBackupService.deleteBackup(baseBak);
        } catch (IllegalAccessException e) {
            log.error(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            log.error(e.getMessage(), e);
        }
        for (int i = 0; i < parentBackups.size(); i++) {
            BaseCloudRDSBackup b = parentBackups.get(i);
            rdsBackupService.deleteBackup(b);
        }
    }


    private void setExtraBackupInvisible(List<BaseCloudRDSBackup> currentAutoBackupList, int count) throws Exception {
        List<BaseCloudRDSBackup> sublist = currentAutoBackupList.subList(0, count);
        for (int i = 0; i < sublist.size(); i++) {
            BaseCloudRDSBackup backup = sublist.get(i);
            String visible = backup.getIsVisible();
            if (visible.equals("1")) {
                //如果备份状态是可见，则应该置为不可见，否则不用重复更新
                backup.setIsVisible("0");
                rdsBackupService.updateBackup(backup);
            }
        }
    }

    private void processManualBackups(int maxManualCount, String instanceId) {
        log.info("备份成功后处理手动备份");
        try {
            List<BaseCloudRDSBackup> currentManualBackupList = rdsBackupService.getBackupListByInstanceId(instanceId, BackupCategory.MANUAL);
            int currentManuelBackupCount = currentManualBackupList.size();
            if (currentManuelBackupCount > maxManualCount) {
                //当前手动备份数量大于项目配额，则需要删除多出来的最早的一个备份
                BaseCloudRDSBackup backup = currentManualBackupList.get(0);
                Map<String, Object> map = rdsBackupService.deleteBackup(backup.getBackupId());
                boolean isDone = (boolean) map.get("isSuccess");
                if (isDone) {
                    log.info("超过配额的手动备份[" + backup.getBackupId() + "]删除成功");
                } else {
                    log.error("超过配额的手动备份[" + backup.getBackupId() + "]删除失败");
                    CloudRDSBackup cloudBackup = (CloudRDSBackup) map.get("rdsBackup");
                    rdsBackupService.notifyDevOps(cloudBackup);
                }
            }
        } catch (Exception e) {
            log.error("超过配额的手动备份删除异常", e);
        }

    }
}

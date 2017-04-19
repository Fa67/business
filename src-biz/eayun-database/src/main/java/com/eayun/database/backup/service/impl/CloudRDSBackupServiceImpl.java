package com.eayun.database.backup.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.constant.ResourceSyncConstant;
import com.eayun.common.constant.SyncProgress.SyncByProjectTypes;
import com.eayun.common.constant.SyncProgress.SyncType;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.sync.SyncProgressUtil;
import com.eayun.common.util.DateUtil;
import com.eayun.database.backup.bean.BackupStatus;
import com.eayun.database.backup.dao.CloudRDSBackupDao;
import com.eayun.database.backup.model.BaseCloudRDSBackup;
import com.eayun.database.backup.service.CloudRDSBackupService;
import com.eayun.database.backup.service.RDSBackupService;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.RDSBackup;
import com.eayun.eayunstack.service.OpenstackTroveBackupService;
import com.eayun.log.ecmcsevice.EcmcLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 云数据库备份底层数据同步Service实现类
 *
 * @author fan.zhang
 */
@Service
@Transactional
public class CloudRDSBackupServiceImpl implements CloudRDSBackupService {
    private static final Logger log = LoggerFactory.getLogger(CloudRDSBackupServiceImpl.class);
    private static final String NAME_PREFIX = "Replication snapshot for";

    @Autowired
    private RDSBackupService rdsBackupService;
    @Autowired
    private CloudRDSBackupDao cloudRDSBackupDao;
    @Autowired
    private OpenstackTroveBackupService openstackTroveBackupService;
    @Autowired
    private JedisUtil jedisUtil;
    @Autowired
    private EcmcLogService ecmcLogService;
    @Autowired
    private SyncProgressUtil syncProgressUtil;

    @Override
    public void syncData(BaseDcDataCenter dataCenter, String prjId) throws Exception {
        Map<String, BaseCloudRDSBackup> dbMap = new HashMap<>();
        Map<String, RDSBackup> stackMap = new HashMap<>();

        String dataCenterId = dataCenter.getId();
        //1.根据数据中心ID和项目ID，查询数据库中的备份列表
        List<BaseCloudRDSBackup> dbList = this.getBackupListByDcIdAndPrjId(dataCenterId, prjId);
        //2.查询底层中的备份列表
        List<RDSBackup> stackList = openstackTroveBackupService.list(dataCenterId, prjId);

        if (dbList != null) {
            for (BaseCloudRDSBackup b : dbList) {
                dbMap.put(b.getBackupId(), b);
            }
        }
        long total = stackList == null ? 0L : stackList.size();
        syncProgressUtil.initResourceTotal(dataCenter.getId(), SyncType.PROJECT, SyncByProjectTypes.RDS_BAK, total);
        if (stackList != null) {
            List<RDSBackup> failDeletingBackups = new ArrayList<>();
            for (RDSBackup b : stackList) {
                //3.对于底层存在、上层存在的数据——更新上层数据库
                if (dbMap.containsKey(b.getId())) {
                    updateCloudRDSBackup(b);
                } else {
                    //4.对于底层存在、上层不存在的数据——如果名字是Replication snapshot for xxx，则删除（这是由于创建从库产生的）；如果是xxx_autobackup_xx，有可能是删除失败的，需要发邮件给运维和管理员提醒他们处理
                    handleNonexistentCloudRDSBackup(failDeletingBackups, dataCenterId, prjId, b);
                }
                stackMap.put(b.getId(), b);
                syncProgressUtil.incrResourceDone(dataCenter.getId(), SyncType.PROJECT, SyncByProjectTypes.RDS_BAK);
            }
            if (failDeletingBackups.size() > 0) {
                rdsBackupService.notifyDevOps(prjId, failDeletingBackups);
            }
        }
        if (dbList != null) {
            //5.对于底层不存在、上层存在的数据——删除数据库记录，注意组织内容push到DATACENTER_SYNC_DELETED_RESOURCE中，用于资源同步发邮件通知
            for (BaseCloudRDSBackup b : dbList) {
                if (!stackMap.containsKey(b.getBackupId())) {
                    rdsBackupService.deleteBackup(b);
                    ecmcLogService.addLog("同步资源清除数据", ResourceSyncConstant.RDS, b.getName(), b.getProjectId(), 1, b.getBackupId(), null);

                    JSONObject json = new JSONObject();
                    json.put("resourceType", ResourceSyncConstant.RDS);
                    json.put("resourceId", b.getBackupId());
                    json.put("resourceName", b.getName());
                    json.put("synTime", new Date());
                    jedisUtil.push(RedisKey.DATACENTER_SYNC_DELETED_RESOURCE, json.toJSONString());
                }
            }

        }
        //6.对于底层不存在、上层不存在的数据——我TM怎么知道

    }

    /**
     * 对于底层存在、上层不存在的数据<br/>
     * 如果名字是Replication snapshot for xxx，则删除（这是由于创建从库产生的）,删除失败，则添加到删除失败的备份列表；<br/>
     * 如果名字不带有Replication snapshot for xxx，有可能是删除失败的，则添加到删除失败的备份列表<br/>
     * 最终发邮件给运维和管理员提醒他们处理
     *
     * @param failDeletingBackups
     * @param dataCenterId
     * @param prjId
     * @param backup
     */
    private void handleNonexistentCloudRDSBackup(List<RDSBackup> failDeletingBackups, String dataCenterId, String prjId, RDSBackup backup) {
        String backupName = backup.getName();
        String backupId = backup.getId();
        String status = backup.getStatus();
        if (backupName.contains(NAME_PREFIX)) {
            boolean isSuccess = false;
            //如果底层备份状态是完成、备份失败，则先调用删除操作
            if(BackupStatus.COMPLETED.equals(status) ||
                    BackupStatus.FAILED.equals(status)){
                isSuccess = openstackTroveBackupService.delete(dataCenterId, prjId, backupId);
            }
            if (!isSuccess) {
                failDeletingBackups.add(backup);
            }
        } else {
            failDeletingBackups.add(backup);
        }
    }

    /**
     * 对于底层存在、上层存在的数据<br/>
     * 更新上层数据库
     *
     * @param backup
     */
    private boolean updateCloudRDSBackup(RDSBackup backup) throws Exception {
        boolean isDone = false;
        try {
            StringBuffer sb = new StringBuffer();
            sb.append(" UPDATE BaseCloudRDSBackup                ");
            sb.append(" SET                                   ");
            sb.append("     locationRef = ?,                 ");
            sb.append("     updateTime = ?,                  ");
            sb.append("     status = ?,                       ");
            sb.append("     name = ?,                         ");
            sb.append("     instanceId = ?,                  ");
            sb.append("     size = ?                          ");
            sb.append(" WHERE                                 ");
            sb.append("     backupId = ?                     ");
            cloudRDSBackupDao.executeUpdate(sb.toString(), new Object[]{
                    backup.getLocationRef(),
                    formatDate(backup.getUpdated()),
                    backup.getStatus(),
                    backup.getName(),
                    backup.getInstance_id(),
                    backup.getSize(),
                    backup.getId()
            });
            isDone = true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            isDone = false;
            throw e;
        }
        return isDone;
    }

    private Date formatDate(String updated) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date updateTimeUTC = sdf.parse(updated);
        Date updateTime = DateUtil.addDay(updateTimeUTC, new int[]{0, 0, 0, +8});
        return updateTime;
    }

    private List<BaseCloudRDSBackup> getBackupListByDcIdAndPrjId(String dcId, String prjId) {
        StringBuilder sb = new StringBuilder();
        sb.append(" from BaseCloudRDSBackup where datacenterId=? and projectId=?");
        return cloudRDSBackupDao.find(sb.toString(), new Object[]{dcId, prjId});
    }
}

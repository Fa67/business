package com.eayun.database.backup.job;

import com.eayun.common.job.BaseQuartzJobBean;
import com.eayun.database.backup.model.BaseCloudRDSBackup;
import com.eayun.database.backup.model.CloudRDSBackupSchedule;
import com.eayun.database.backup.pool.RDSAutoBackupThreadPool;
import com.eayun.database.backup.service.RDSBackupService;
import com.eayun.database.backup.thread.RDSAutoBackupThread;
import com.eayun.database.backup.thread.RDSBackupCleanThread;
import com.eayun.database.instance.service.RDSInstanceService;
import com.eayun.notice.service.MessageCenterService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 云数据库来源实例已删除的备份定时删除计划任务
 *
 * @author fan.zhang
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class RDSBackupCleanJob extends BaseQuartzJobBean {
    private static final Logger log = LoggerFactory.getLogger(RDSBackupCleanJob.class);
    private RDSBackupService rdsBackupService;
    private MessageCenterService messageCenterService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("开始执行来源实例已删除的备份定时删除计划任务");
        ApplicationContext applicationContext = getApplicationContext(context);
        rdsBackupService = applicationContext.getBean(RDSBackupService.class);
        messageCenterService = applicationContext.getBean(MessageCenterService.class);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date currentTime = format.parse(format.format(new Date()));
            //1.获取来源实例已被删除的备份列表，过滤出来源是里删除时间大于等于3天的备份（手动备份和自动备份的全量备份），每个备份提交一个线程
            //2.备份中，根据备份的类型，分别处理手动备份和自动备份——手动备份直接删、自动备份先删孩子节点，那么应该取出的自动备份至少应该是全量备份
            /* 这里可以不需要找parentId为null，instanceExist为0的全量备份，而是逆序查询instanceExist为0的所有备份，依次删除即可
             * 但是，交由线程去删除，无法保证顺序，所以还是递归的删除比较稳妥。
             */
            List<BaseCloudRDSBackup> backups = rdsBackupService.getOrphanedBackups();
            for (int i = 0; i < backups.size(); i++) {
                BaseCloudRDSBackup backup = backups.get(i);
                RDSBackupCleanThread t = new RDSBackupCleanThread(rdsBackupService, messageCenterService, backup, currentTime);
                RDSAutoBackupThreadPool.pool.submit(t);
            }
        } catch (Exception e) {
            log.error("执行来源实例已删除的备份定时删除计划任务异常", e);
        }

    }
}

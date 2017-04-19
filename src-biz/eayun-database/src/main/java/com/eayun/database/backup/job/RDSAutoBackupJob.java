package com.eayun.database.backup.job;

import com.eayun.common.constant.RedisKey;
import com.eayun.common.job.BaseQuartzJobBean;
import com.eayun.database.backup.model.CloudRDSBackupSchedule;
import com.eayun.database.backup.pool.RDSAutoBackupThreadPool;
import com.eayun.database.backup.service.RDSBackupService;
import com.eayun.database.backup.thread.RDSAutoBackupThread;
import com.eayun.database.instance.service.RDSInstanceService;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.project.service.ProjectService;
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
 * 云数据库自动备份计划任务
 *
 * @author fan.zhang
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class RDSAutoBackupJob extends BaseQuartzJobBean {
    private static final Logger log = LoggerFactory.getLogger(RDSAutoBackupJob.class);
    private RDSBackupService rdsBackupService;
    private RDSInstanceService rdsInstanceService;
    private MessageCenterService messageCenterService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("开始执行云数据库自动备份任务");
        ApplicationContext applicationContext = getApplicationContext(context);
        rdsBackupService = applicationContext.getBean(RDSBackupService.class);
        rdsInstanceService = applicationContext.getBean(RDSInstanceService.class);
        messageCenterService = applicationContext.getBean(MessageCenterService.class);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH");
        try {
            //获取启用的备份计划，每个备份计划提交线程池处理
            Date currentHour = format.parse(format.format(new Date()));
            List<CloudRDSBackupSchedule> scheduleList = rdsBackupService.getEnabledBackupSchedules();
            log.info("当前启用的自动备份计划数量为[" + scheduleList.size() + "]");
            for (int i = 0; i < scheduleList.size(); i++) {
                CloudRDSBackupSchedule schedule = scheduleList.get(i);
                RDSAutoBackupThread thread = new RDSAutoBackupThread(rdsBackupService, rdsInstanceService, messageCenterService, schedule, currentHour);
                RDSAutoBackupThreadPool.pool.submit(thread);
            }
        } catch (Exception e) {
            log.error("云数据库自动备份任务执行异常", e);
        }

    }
}

package com.eayun.database.backup.job;

import com.eayun.common.constant.RedisKey;
import com.eayun.common.job.BaseQuartzJobBean;
import com.eayun.common.redis.JedisUtil;
import com.eayun.database.backup.service.RDSBackupService;
import com.eayun.database.backup.thread.RDSBackupSyncThread;
import com.eayun.database.resourcepool.SyncResourceStatutPool;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.project.service.ProjectService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 云数据库备份状态同步计划任务
 *
 * @author fan.zhang
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class RDSBackupSyncJob extends BaseQuartzJobBean {
    private static final Logger log = LoggerFactory.getLogger(RDSBackupSyncJob.class);

    private RDSBackupService rdsBackupService;
    private ProjectService projectService;
    private MessageCenterService messageCenterService;
    private JedisUtil jedisUtil;
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("开始执行云数据库备份状态同步任务");
        ApplicationContext applicationContext = getApplicationContext(context);
        rdsBackupService = applicationContext.getBean(RDSBackupService.class);
        projectService = applicationContext.getBean(ProjectService.class);
        messageCenterService = applicationContext.getBean(MessageCenterService.class);
        jedisUtil = applicationContext.getBean(JedisUtil.class);

        ThreadPoolExecutor pool = SyncResourceStatutPool.pool;
        int maxPoolSize = 100;
        long availableSize = maxPoolSize-pool.getActiveCount();
        long queueLength = jedisUtil.sizeOfList(RedisKey.rdsBackupSyncKey);
        availableSize = availableSize>queueLength?queueLength:availableSize;
        for (int i=0; i<availableSize; i++){
            RDSBackupSyncThread thread = new RDSBackupSyncThread(rdsBackupService, projectService, messageCenterService, jedisUtil);
            pool.submit(thread);
        }
    }
}

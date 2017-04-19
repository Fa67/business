package com.eayun.schedule.job;

import java.util.concurrent.ThreadPoolExecutor;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.context.ApplicationContext;

import com.eayun.common.job.BaseQuartzJobBean;
import com.eayun.schedule.pool.SyncResourceStatutPool;
import com.eayun.schedule.service.CloudVpnService;
import com.eayun.schedule.thread.status.CloudVpnStatusThread;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class CloudVpnJob extends BaseQuartzJobBean{

    private CloudVpnService cloudVpnService;
    
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        ThreadPoolExecutor pool = SyncResourceStatutPool.pool;
        ApplicationContext applicationContext = getApplicationContext(context);
        cloudVpnService = applicationContext.getBean(CloudVpnService.class);
        
        int maxPoolSize = 100;
        long size = maxPoolSize-pool.getActiveCount();
        
        for (int i = 0; i<size; i++) {
            cloudVpnService = applicationContext.getBean(CloudVpnService.class);
            CloudVpnStatusThread vpnThread = new CloudVpnStatusThread(cloudVpnService);
            pool.submit(vpnThread);
        }
    }
}

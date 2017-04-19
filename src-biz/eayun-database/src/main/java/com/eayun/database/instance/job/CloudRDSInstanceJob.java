package com.eayun.database.instance.job;

import java.util.concurrent.ThreadPoolExecutor;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.context.ApplicationContext;

import com.eayun.common.constant.RedisKey;
import com.eayun.common.job.BaseQuartzJobBean;
import com.eayun.database.instance.service.CloudRDSInstanceService;
import com.eayun.database.instance.thread.status.CloudRDSInstanceStatusThread;
import com.eayun.database.resourcepool.SyncResourceStatutPool;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class CloudRDSInstanceJob extends BaseQuartzJobBean{

	private CloudRDSInstanceService cloudRDSInstanceService;
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		ThreadPoolExecutor pool = SyncResourceStatutPool.pool;
		int maxPoolSize = 100;
		long size = maxPoolSize-pool.getActiveCount();
		ApplicationContext applicationContext = getApplicationContext(context);
		cloudRDSInstanceService = applicationContext.getBean(CloudRDSInstanceService.class);
		long quenceSize = cloudRDSInstanceService.size(RedisKey.rdsKey);
		
		if(size>quenceSize){
			size = quenceSize;
		}
		for(int i= 0; i<size ;i++){
			cloudRDSInstanceService = applicationContext.getBean(CloudRDSInstanceService.class);
			CloudRDSInstanceStatusThread rdsInstanceThread = new CloudRDSInstanceStatusThread(cloudRDSInstanceService);
			pool.submit(rdsInstanceThread);
		}
	}

}

package com.eayun.database.log.thread;

import com.eayun.database.instance.model.CloudRDSInstance;
import com.eayun.database.log.model.CloudRDSLog;
import com.eayun.database.log.service.RDSLogService;

public class CloudRDSLogSyncThread implements Runnable {
	private RDSLogService rdsLogService;
	private CloudRDSInstance rdsInstance;
	
	public CloudRDSLogSyncThread(){}
	
	public CloudRDSLogSyncThread(RDSLogService rdsLogService,CloudRDSInstance rdsInstance){
		this.rdsLogService = rdsLogService;
		this.rdsInstance = rdsInstance;
	}
	@Override
	public void run() {
		CloudRDSLog rdsLog = new CloudRDSLog();
		rdsLog.setDcId(rdsInstance.getDcId());
		rdsLog.setPrjId(rdsInstance.getPrjId());
		rdsLog.setRdsInstanceId(rdsInstance.getRdsId());
		rdsLog.setLogType(rdsInstance.getLogPublishing());
		
		rdsLogService.syncLog(rdsLog);
	}
	
}

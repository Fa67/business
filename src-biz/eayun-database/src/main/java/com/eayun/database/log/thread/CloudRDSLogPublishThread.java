package com.eayun.database.log.thread;

import com.eayun.database.instance.model.CloudRDSInstance;
import com.eayun.database.log.model.CloudRDSLog;
import com.eayun.database.log.service.RDSLogService;

public class CloudRDSLogPublishThread implements Runnable{
	private RDSLogService rdsLogService;
	private CloudRDSInstance instance;
	
	public CloudRDSLogPublishThread (){}
	
	public CloudRDSLogPublishThread (RDSLogService rdsLogService,CloudRDSInstance instance){
		this.rdsLogService = rdsLogService;
		this.instance = instance;
	}
	
	@Override
	public void run() {
		CloudRDSLog rdsLog = new CloudRDSLog();
		rdsLog.setDcId(instance.getDcId());
		rdsLog.setPrjId(instance.getPrjId());
		rdsLog.setRdsInstanceId(instance.getRdsId());
		
		rdsLogService.publishLog(rdsLog,true);
	}
}

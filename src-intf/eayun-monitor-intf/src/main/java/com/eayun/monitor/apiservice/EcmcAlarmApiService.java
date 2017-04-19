package com.eayun.monitor.apiservice;


public interface EcmcAlarmApiService {

	/**
	 * 如删除对象，则需删除运维报警对象及其关联的报警信息、报警监控项等
	 * @param objectId
	 */
	public void cleanAlarmDataAfterDeletingObject(String objectId);
	
	
}

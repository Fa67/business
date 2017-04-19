package com.eayun.monitor.apiservice;


public interface AlarmApiService {
	
	/**
	 * 清除指定云主机的监控
	 * @param vmId
	 * @return
	 */
	public boolean cleanAlarmDataAfterDeletingVM(String vmId);
	
	
}

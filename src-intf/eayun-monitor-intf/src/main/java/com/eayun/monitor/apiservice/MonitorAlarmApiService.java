package com.eayun.monitor.apiservice;

import com.eayun.monitor.model.AlarmObject;


public interface MonitorAlarmApiService {
	
    /**
     * 删除监控报警项
     * @param alarmObject
     */
	public void deleteMonitorAlarmItemByAlarmObject(AlarmObject alarmObject);
	
	
	
	
}

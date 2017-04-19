package com.eayun.monitor.apiservice.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.monitor.apiservice.MonitorAlarmApiService;
import com.eayun.monitor.baseservice.BaseMonitorAlarmService;
import com.eayun.monitor.dao.MonitorAlarmItemDao;
import com.eayun.monitor.model.AlarmObject;
import com.eayun.monitor.model.BaseMonitorAlarmItem;


@Service
@Transactional
public class MonitorAlarmApiServiceImpl extends BaseMonitorAlarmService implements MonitorAlarmApiService {

	@Autowired
    private MonitorAlarmItemDao monitorAlarmItemDao;
	
	
	/**
	 * 删除监控报警项
	 */
	 @Override
	 public void deleteMonitorAlarmItemByAlarmObject(AlarmObject alarmObject) {
	    String sql = " from BaseMonitorAlarmItem where objectId=? and alarmRuleId=?";
	    List<BaseMonitorAlarmItem> baseMmtAlmItm = monitorAlarmItemDao.find(sql, alarmObject.getId(),alarmObject.getAlarmRuleId());
	    for (BaseMonitorAlarmItem baseMonitorAlarmItem : baseMmtAlmItm) {
	        monitorAlarmItemDao.delete(baseMonitorAlarmItem);
	    }
	    
	}

}

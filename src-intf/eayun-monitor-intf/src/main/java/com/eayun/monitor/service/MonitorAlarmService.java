package com.eayun.monitor.service;

import java.util.List;

import com.eayun.common.filter.SessionUserInfo;
import com.eayun.monitor.bean.AlarmMsgExcel;
import com.eayun.monitor.model.AlarmMessage;
import com.eayun.monitor.model.AlarmObject;
import com.eayun.monitor.model.MonitorAlarmItem;

public interface MonitorAlarmService {

    public void addMonitorAlarmItemByAlarmRule(String alarmRuleId);
    
    public void updateMonitorAlarmItemByAlarmRule(String alarmRuleId);

    public void deleteMonitorAlarmItemByAlarmRule(String alarmRuleId);

    public void deleteMonitorAlarmItemByAlarmObject(AlarmObject alarmObject);

    public List<MonitorAlarmItem> getMonitorAlarmItemList();

    public void updateMonitorAlarmItem(MonitorAlarmItem monitorAlarmItem);

    public void addAlarmMessage(AlarmMessage alarmMsg);

    public boolean eraseAlarmMsgByIds(List<String> checkedIds);

    public List<AlarmMsgExcel> getAlarmMessagesByUserIdForExcel(SessionUserInfo sessionUser);

    public void addMonitorAlarmItemByAlarmObject(AlarmObject alarmObject);

    
}

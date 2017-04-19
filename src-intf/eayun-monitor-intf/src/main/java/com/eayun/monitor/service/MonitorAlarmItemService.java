package com.eayun.monitor.service;

import java.util.List;

import com.eayun.monitor.model.MonitorAlarmItem;

public interface MonitorAlarmItemService {
    public List<MonitorAlarmItem> getMonitorAlarmItemList(String customId);
}

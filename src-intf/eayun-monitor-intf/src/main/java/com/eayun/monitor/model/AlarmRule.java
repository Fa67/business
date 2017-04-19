package com.eayun.monitor.model;

public class AlarmRule extends BaseAlarmRule {

    private static final long serialVersionUID = 9044071848908408219L;
    
    private String triggerCondition;
    
    private int alarmObjectNumber;
    
    private String lastModifyTime;
    
    private String monitorItemName;

    public String getTriggerCondition() {
        return triggerCondition;
    }

    public void setTriggerCondition(String triggerCondition) {
        this.triggerCondition = triggerCondition;
    }

    public int getAlarmObjectNumber() {
        return alarmObjectNumber;
    }

    public void setAlarmObjectNumber(int alarmObjectNumber) {
        this.alarmObjectNumber = alarmObjectNumber;
    }

    public String getLastModifyTime() {
        return lastModifyTime;
    }

    public void setLastModifyTime(String lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

    public String getMonitorItemName() {
        return monitorItemName;
    }

    public void setMonitorItemName(String monitorItemName) {
        this.monitorItemName = monitorItemName;
    }
    
}

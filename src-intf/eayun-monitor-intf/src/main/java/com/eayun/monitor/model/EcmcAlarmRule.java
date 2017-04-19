package com.eayun.monitor.model;

public class EcmcAlarmRule extends BaseEcmcAlarmRule {

	/**
	 *Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = -5112266701765534248L;
	
	private String triggerCondition;	//触发条件详情
	
	private int alarmObjectNumber;	//报警对象数量
	
	private String monitorItemName;		//监控项名称

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

	public String getMonitorItemName() {
		return monitorItemName;
	}

	public void setMonitorItemName(String monitorItemName) {
		this.monitorItemName = monitorItemName;
	}
	
}

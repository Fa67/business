package com.eayun.monitor.model;

public class EcmcContact extends BaseEcmcContact {
	
	private static final long serialVersionUID = 715408464814047016L;
	
	private boolean linkedToAlarmRule;
	private String currentCtcGrpId;

	public boolean isLinkedToAlarmRule() {
		return linkedToAlarmRule;
	}

	public void setLinkedToAlarmRule(boolean linkedToAlarmRule) {
		this.linkedToAlarmRule = linkedToAlarmRule;
	}

	public String getCurrentCtcGrpId() {
		return currentCtcGrpId;
	}

	public void setCurrentCtcGrpId(String currentCtcGrpId) {
		this.currentCtcGrpId = currentCtcGrpId;
	}

}

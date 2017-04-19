package com.eayun.monitor.model;

public class Contact extends BaseContact {

    private static final long serialVersionUID = 3787854573181556319L;
    
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

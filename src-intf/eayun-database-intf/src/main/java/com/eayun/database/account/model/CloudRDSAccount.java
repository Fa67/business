package com.eayun.database.account.model;

import java.util.List;

public class CloudRDSAccount extends BaseCloudRDSAccount {

    private String instanceName;
    
    private String status;
    
    private String chargeState;
    
    private List<String> dbIdList;
    
    private List<String> dbNameList;

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getChargeState() {
        return chargeState;
    }

    public void setChargeState(String chargeState) {
        this.chargeState = chargeState;
    }

    public List<String> getDbIdList() {
        return dbIdList;
    }

    public void setDbIdList(List<String> dbIdList) {
        this.dbIdList = dbIdList;
    }

    public List<String> getDbNameList() {
        return dbNameList;
    }

    public void setDbNameList(List<String> dbNameList) {
        this.dbNameList = dbNameList;
    }

}
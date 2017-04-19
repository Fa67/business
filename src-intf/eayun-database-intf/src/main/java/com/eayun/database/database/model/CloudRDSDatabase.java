package com.eayun.database.database.model;

import java.util.List;

public class CloudRDSDatabase extends BaseCloudRDSDatabase {
    /*云数据库名称*/
    private String instanceName;
    /*云数据库实例状态*/
    private String status;
    /*云数据库实例计费状态*/
    private String chargeState;
    
    private List<String> accessAccountList;

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

    public List<String> getAccessAccountList() {
        return accessAccountList;
    }

    public void setAccessAccountList(List<String> accessAccountList) {
        this.accessAccountList = accessAccountList;
    }
    
}
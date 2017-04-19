package com.eayun.database.relation.model;

public class CloudRDSRelation extends BaseCloudRDSRelation {

    private String accountName;
    
    private String databaseName;

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }
    
}

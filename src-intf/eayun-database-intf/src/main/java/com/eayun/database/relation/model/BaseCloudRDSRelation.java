package com.eayun.database.relation.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "cloud_rdsdbaccountrelation")
public class BaseCloudRDSRelation {

    private String relationId;
    private String accountId;
    private String databaseId;
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "relation_id", unique = true, nullable = false, length = 100)
    public String getRelationId() {
        return relationId;
    }
    public void setRelationId(String relationId) {
        this.relationId = relationId;
    }
    @Column(name = "account_id", length = 100)
    public String getAccountId() {
        return accountId;
    }
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
    @Column(name = "database_id", length = 100)
    public String getDatabaseId() {
        return databaseId;
    }
    public void setDatabaseId(String databaseId) {
        this.databaseId = databaseId;
    }
    
}
package com.eayun.database.database.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;

import com.eayun.eayunstack.model.RDSDatabase;
@Entity
@Table(name = "cloud_rdsdatabase")
public class BaseCloudRDSDatabase {

    private String databaseId;
    private String databaseName;
    private String instanceId;
    private String prjId;
    private String dcId;
    private String characterSet;
    private Date createTime;
    private String remark;
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "database_id", unique = true, nullable = false, length = 100)
    public String getDatabaseId() {
        return databaseId;
    }
    public void setDatabaseId(String databaseId) {
        this.databaseId = databaseId;
    }
    @Column(name = "database_name", length = 64)
    public String getDatabaseName() {
        return databaseName;
    }
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }
    @Column(name = "instance_id", length = 100)
    public String getInstanceId() {
        return instanceId;
    }
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }
    @Column(name = "prj_id", length = 100)
    public String getPrjId() {
        return prjId;
    }
    public void setPrjId(String prjId) {
        this.prjId = prjId;
    }
    @Column(name = "dc_id", length = 100)
    public String getDcId() {
        return dcId;
    }
    public void setDcId(String dcId) {
        this.dcId = dcId;
    }
    @Column(name = "character_set", length = 20)
    public String getCharacterSet() {
        return characterSet;
    }
    public void setCharacterSet(String characterSet) {
        this.characterSet = characterSet;
    }
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_time", length = 19)
    public Date getCreateTime() {
        return createTime;
    }
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    @Column(name = "remark", length = 200)
    public String getRemark() {
        return remark;
    }
    public void setRemark(String remark) {
        this.remark = remark;
    }
    
    public BaseCloudRDSDatabase () {};
    
    public BaseCloudRDSDatabase (RDSDatabase database, String dcId, String prjId, String instanceId) {
        this.databaseName = database.getName();
        this.characterSet = database.getCharacter_set();
        this.dcId = dcId;
        this.prjId = prjId;
        this.instanceId = instanceId;
        this.remark = "";
    }
    
}

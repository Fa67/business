package com.eayun.database.account.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "cloud_rdsdbaccount")
public class BaseCloudRDSAccount {

    private String accountId;
    private String accountName;
    private String instanceId;
    private String prjId;
    private String dcId;
    private String password;
    private Date createTime;
    private String remark;
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "account_id", unique = true, nullable = false, length = 100)
    public String getAccountId() {
        return accountId;
    }
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
    @Column(name = "account_name", length = 20)
    public String getAccountName() {
        return accountName;
    }
    public void setAccountName(String accountName) {
        this.accountName = accountName;
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
    @Column(name = "password", length = 30)
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
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
    
}
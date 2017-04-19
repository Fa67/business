package com.eayun.database.backup.model;

/**
 * RDSBackup实体类
 * @author fan.zhang
 */
public class CloudRDSBackup extends BaseCloudRDSBackup {

    private String instanceName;
    private String statusCN;//备份状态中文：NEW-创建中，COMPLETED-可用，FAILED-错误
    private String datastoreId;//根据versionId查的datastoreId
    private String versionType;//数据库类型，如 MySQL
    private String version;//数据库版本，如 5.5
    private String cusOrg;
    private String dcName;
    private String prjName;

    public String getCusOrg() {
        return cusOrg;
    }

    public void setCusOrg(String cusOrg) {
        this.cusOrg = cusOrg;
    }

    public String getDcName() {
        return dcName;
    }

    public void setDcName(String dcName) {
        this.dcName = dcName;
    }

    public String getPrjName() {
        return prjName;
    }

    public void setPrjName(String prjName) {
        this.prjName = prjName;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getStatusCN() {
        return statusCN;
    }

    public void setStatusCN(String statusCN) {
        this.statusCN = statusCN;
    }

    public String getDatastoreId() {
        return datastoreId;
    }

    public void setDatastoreId(String datastoreId) {
        this.datastoreId = datastoreId;
    }

    public String getVersionType() {
        return versionType;
    }

    public void setVersionType(String versionType) {
        this.versionType = versionType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}

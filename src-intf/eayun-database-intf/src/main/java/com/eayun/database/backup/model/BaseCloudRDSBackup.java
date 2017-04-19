package com.eayun.database.backup.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 云数据库备份表
 * @author fan.zhang
 */
@Entity
@Table(name = "cloud_rdsbackup")
public class BaseCloudRDSBackup implements Serializable{

    @Id
    @Column(name = "backup_id", unique = true, nullable = false, length = 100)
    String backupId;

    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = "create_time")
    Date createTime;

    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = "update_time")
    Date updateTime;

    @Column(name = "status", length = 32)
    String status;

    @Column(name = "description", length = 200)
    String description;

    @Column(name = "name", length = 64)
    String name;

    @Column(name = "instance_id", length = 64)
    String instanceId;

    @Column(name = "version_id", length = 100)
    String versionId;

    @Column(name = "category", length = 20)
    String category;

    @Column(name = "size", columnDefinition = "double(10,2)")
    double size;

    @Column(name = "location_ref", columnDefinition="text")
    String locationRef;

    @Column(name = "datacenter_id", length = 64)
    String datacenterId;

    @Column(name = "project_id", length = 100)
    String projectId;

    @Column(name = "is_visible", length = 1)
    String isVisible;

    @Column(name = "instance_exist", length = 1)
    String instanceExist;

    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = "instance_deletetime")
    Date instanceDeleteTime;

    @Column(name = "parent_id", length = 64)
    String parentId;

    @Column(name = "config_id", length = 100)
    String configId;

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getBackupId() {
        return backupId;
    }

    public void setBackupId(String backupId) {
        this.backupId = backupId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public String getLocationRef() {
        return locationRef;
    }

    public void setLocationRef(String locationRef) {
        this.locationRef = locationRef;
    }

    public String getDatacenterId() {
        return datacenterId;
    }

    public void setDatacenterId(String datacenterId) {
        this.datacenterId = datacenterId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getIsVisible() {
        return isVisible;
    }

    public void setIsVisible(String isVisible) {
        this.isVisible = isVisible;
    }

    public String getInstanceExist() {
        return instanceExist;
    }

    public void setInstanceExist(String instanceExist) {
        this.instanceExist = instanceExist;
    }

    public Date getInstanceDeleteTime() {
        return instanceDeleteTime;
    }

    public void setInstanceDeleteTime(Date instanceDeleteTime) {
        this.instanceDeleteTime = instanceDeleteTime;
    }
}

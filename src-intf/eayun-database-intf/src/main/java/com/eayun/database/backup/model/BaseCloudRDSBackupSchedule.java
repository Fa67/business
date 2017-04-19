package com.eayun.database.backup.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * 云数据库备份计划表
 * @author fan.zhang
 */
@Entity
@Table(name = "cloud_rdsbackupschedule")
public class BaseCloudRDSBackupSchedule {
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "id", unique = true, nullable = false, length = 32)
    private String id;

    @Column(name = "instance_id", length = 64)
    private String instanceId;

    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = "up_time")
    private Date upTime;

    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = "off_time")
    private Date offTime;

    @Column(name = "is_enabled", length = 1)
    private String isEnabled;

    @Column(name = "schedule_time", length = 20)
    private String scheduleTime;

    @Column(name = "datacenter_id", length = 64)
    private String datacenterId;

    @Column(name = "project_id", length = 100)
    private String projectId;

    @Column(name = "customer_id", length = 100)
    private String customerId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public Date getUpTime() {
        return upTime;
    }

    public void setUpTime(Date upTime) {
        this.upTime = upTime;
    }

    public Date getOffTime() {
        return offTime;
    }

    public void setOffTime(Date offTime) {
        this.offTime = offTime;
    }

    public String getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(String isEnabled) {
        this.isEnabled = isEnabled;
    }

    public String getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(String scheduleTime) {
        this.scheduleTime = scheduleTime;
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

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}

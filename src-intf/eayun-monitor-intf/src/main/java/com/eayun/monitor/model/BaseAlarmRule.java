package com.eayun.monitor.model;

import java.io.Serializable;
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
@Table(name = "ecsc_alarmrule")
public class BaseAlarmRule implements Serializable {

    private static final long serialVersionUID = 7636635107515462099L;

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "ar_id", unique = true, nullable = false, length = 32)
    private String            id;

    @Column(name = "ar_cusid", length = 32)
    private String            cusId;

    @Column(name = "ar_name", length = 64)
    private String            name;

    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = "ar_modifytime")
    private Date              modifyTime;		//修改时间

    @Column(name = "ar_monitoritem", length = 32)
    private String            monitorItem;		//监控项类型nodeId

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCusId() {
        return cusId;
    }

    public void setCusId(String cusId) {
        this.cusId = cusId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getMonitorItem() {
        return monitorItem;
    }

    public void setMonitorItem(String monitorItem) {
        this.monitorItem = monitorItem;
    }
    
}

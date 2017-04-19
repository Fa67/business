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

/**
 * ECSC监控报警项
 * 网络1.3新增监控项类型
 *                       
 * @Filename: BaseMonitorAlarmItem.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2017年3月2日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Entity
@Table(name = "ecsc_monitoralarmitem")
public class BaseMonitorAlarmItem implements Serializable{

    private static final long serialVersionUID = 4043001866111679977L;

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "mai_id", unique = true, nullable = false, length = 32)
    private String            id;
    
    @Column(name = "mai_objectid", length = 32)
    private String objectId;			//报警对象id（非报警对象资源的id）
    
    @Column(name = "mai_triggerid", length = 32)
    private String triggerId;			//触发条件id
    
    @Column(name = "mai_alarmruleid", length = 32)
    private String alarmRuleId;			//报警规则id
    
    @Column(name = "mai_isnotified", length = 1)
    private String isNotified;			//是否已报警
    
    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = "mai_modifiedTime")
    private Date modifiedTime;			//上次报警时间
    
    @Column(name = "mai_monitortype", length = 100)
    private String monitorType;			//监控项类型nodeId

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(String triggerId) {
        this.triggerId = triggerId;
    }

    public String getAlarmRuleId() {
        return alarmRuleId;
    }

    public void setAlarmRuleId(String alarmRuleId) {
        this.alarmRuleId = alarmRuleId;
    }

    public String getIsNotified() {
        return isNotified;
    }

    public void setIsNotified(String isNotified) {
        this.isNotified = isNotified;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

	public String getMonitorType() {
		return monitorType;
	}

	public void setMonitorType(String monitorType) {
		this.monitorType = monitorType;
	}
    
}

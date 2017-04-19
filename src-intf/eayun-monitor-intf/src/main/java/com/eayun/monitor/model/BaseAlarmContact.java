package com.eayun.monitor.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "ecsc_alarmcontact")
public class BaseAlarmContact implements Serializable{

    private static final long serialVersionUID = -8601748116201068690L;
    
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "ac_id", unique = true, nullable = false, length = 32)
    private String id;
    
    @Column(name = "ac_alarmruleid", length = 32)
    private String alarmRuleId;
    
    @Column(name = "ac_contactid", length = 32)
    private String contactId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlarmRuleId() {
        return alarmRuleId;
    }

    public void setAlarmRuleId(String alarmRuleId) {
        this.alarmRuleId = alarmRuleId;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

}

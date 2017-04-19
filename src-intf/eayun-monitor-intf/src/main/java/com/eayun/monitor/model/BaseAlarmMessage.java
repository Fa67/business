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
@Table(name = "ecsc_alarmmessage")
public class BaseAlarmMessage implements Serializable{

    private static final long serialVersionUID = -5492800070151037299L;

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "am_id", unique = true, nullable = false, length = 32)
    private String id;
    
    @Column(name = "am_vmid", length = 64)
    private String vmId;					//报警对象资源id
    
    @Column(name = "am_monitortype", length = 32)
    private String monitorType;				//监控项类型，网络1.3之后由原来的存储中文改为nodeId
    
    @Column(name = "am_alarmtype", length = 32)
    private String alarmType;				//报警类型，网络1.3之后由原来的存储中文改为nodeId
    
    @Column(name = "am_detail", length = 500)
    private String detail;					//报警详情
    
    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = "am_time")
    private Date time;						//报警时间
    
    @Column(name = "am_isprocessed", length = 1)
    private String isProcessed;				//是否已处理
    
    @Column(name = "am_monitoralarmitemid", length = 32)
    private String monitorAlarmItemId;		//监控报警项ID
    
    @Column(name = "am_alarmruleid", length=32)
    private String alarmRuleId;				//规则id
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getVmId() {
        return vmId;
    }
    public void setVmId(String vmId) {
        this.vmId = vmId;
    }
    public String getMonitorType() {
        return monitorType;
    }
    public void setMonitorType(String monitorType) {
        this.monitorType = monitorType;
    }
    public String getAlarmType() {
        return alarmType;
    }
    public void setAlarmType(String alarmType) {
        this.alarmType = alarmType;
    }
    public String getDetail() {
        return detail;
    }
    public void setDetail(String detail) {
        this.detail = detail;
    }
    public Date getTime() {
        return time;
    }
    public void setTime(Date time) {
        this.time = time;
    }
    public String getIsProcessed() {
        return isProcessed;
    }
    public void setIsProcessed(String isProcessed) {
        this.isProcessed = isProcessed;
    }
    public String getMonitorAlarmItemId() {
        return monitorAlarmItemId;
    }
    public void setMonitorAlarmItemId(String monitorAlarmItemId) {
        this.monitorAlarmItemId = monitorAlarmItemId;
    }
    public String getAlarmRuleId() {
        return alarmRuleId;
    }
    public void setAlarmRuleId(String alarmRuleId) {
        this.alarmRuleId = alarmRuleId;
    }
}

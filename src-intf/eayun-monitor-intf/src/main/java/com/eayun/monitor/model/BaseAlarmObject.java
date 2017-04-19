package com.eayun.monitor.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

/**
 * ECSC报警对象实体类
 * 网络1.3新增监控项类型
 *                       
 * @Filename: BaseAlarmObject.java
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
@Table(name = "ecsc_alarmobject")
public class BaseAlarmObject implements Serializable {

    private static final long serialVersionUID = 862749530526735012L;
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "ao_id", unique = true, nullable = false, length = 32)
    private String id;
    
    @Column(name = "ao_alarmruleid", length = 32)
    private String alarmRuleId;				//报警规则id
    
    @Column(name = "ao_vmid", length = 64)
    private String vmId;					//报警对象资源id
    
    @Column(name = "ao_monitortype", length = 100)
    private String monitorType;				//监控项类型nodeId(网络1.3新增监控项类型)
    
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
    
    

}

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
 * 运维监控报警项
 *                       
 * @Filename: BaseEcmcMonitoralarmitem.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2016年3月24日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Entity
@Table(name = "ecmc_monitoralarmitem")
public class BaseEcmcMonitorAlarmItem implements Serializable {

	/**
	 *Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 3584592082510436163L;

	@Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "mai_id", unique = true, nullable = false, length = 32)
    private String id;
    
	@Column(name = "mai_objectid", length = 32)
    private String objectId;			//报警对象id
    
	@Column(name = "mai_type", length = 32)
    private String objType;				//对象类型
    
	@Column(name = "mai_triggerid", length = 32)
    private String triggerId;			//触发条件id
    
	@Column(name = "mai_alarmruleid", length = 32)
    private String alarmRuleId;			//规则id
    
	@Column(name = "mai_isnotified", length = 1)
    private String isNotified;			//是否已产生报警信息
    
	@Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = "mai_modifiedTime")
    private Date modifiedTime;			//上次修改时间

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

	
	public String getObjType() {
		return objType;
	}

	public void setObjType(String objType) {
		this.objType = objType;
	}
    
}

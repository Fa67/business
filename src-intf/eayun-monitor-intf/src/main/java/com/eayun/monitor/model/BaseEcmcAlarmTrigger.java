package com.eayun.monitor.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

/**
 * 运维触发条件
 *                       
 * @Filename: BaseEcmcAlarmTrigger.java
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
@Table(name = "ecmc_alarmtrigger")
public class BaseEcmcAlarmTrigger implements Serializable {

	/**
	 *Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = -4185717799799431626L;
	
	@Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "at_id", unique = true, nullable = false, length = 32)
    private String            id;
    
	@Column(name = "at_alarmruleid", length = 32)
    private String            alarmRuleId;//规则ID
    
	@Column(name = "at_zb", length = 32)
    private String            zb;		//存储的是指标的nodeId，方便在缓存的数据字典中读取
    
	@Column(name = "at_operator", length = 32)
    private String            operator;	//运算符,仍然存储“>”、“<”等符号
    
	@Column(name = "at_threshold")
    private float             threshold;//阈值
    
	@Column(name = "at_unit")
    private String            unit;		//单位，存储“%”、“MB/s”等，获取由数据字典param1获取
    
	@Column(name = "at_lasttime")
    private int               lastTime;	//持续时间，单位：s
    
	@Column(name = "at_istriggered", length = 1)
    private String            isTriggered;//是否已触发

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

	public String getZb() {
		return zb;
	}

	public void setZb(String zb) {
		this.zb = zb;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public float getThreshold() {
		return threshold;
	}

	public void setThreshold(float threshold) {
		this.threshold = threshold;
	}

	
	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	
	public int getLastTime() {
		return lastTime;
	}

	public void setLastTime(int lastTime) {
		this.lastTime = lastTime;
	}

	
	public String getIsTriggered() {
		return isTriggered;
	}

	public void setIsTriggered(String isTriggered) {
		this.isTriggered = isTriggered;
	}

	@Override
	public String toString() {
		return "BaseEcmcAlarmTrigger{" +
				"id='" + id + '\'' +
				", alarmRuleId='" + alarmRuleId + '\'' +
				", zb='" + zb + '\'' +
				", operator='" + operator + '\'' +
				", threshold=" + threshold +
				", unit='" + unit + '\'' +
				", lastTime=" + lastTime +
				", isTriggered='" + isTriggered + '\'' +
				'}';
	}
}

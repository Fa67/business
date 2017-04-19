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
 *
 * 运维报警信息
 * @Filename: BaseEcmcAlarmMessage.java
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
	@Table(name = "ecmc_alarmmessage")
	public class BaseEcmcAlarmMessage implements Serializable {

	/**
	 *Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 4269787319321307331L;
	
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "am_id", unique = true, nullable = false, length = 32)
    private String id;
    
    @Column(name = "am_objectid", length = 36)
    private String objId;					//对象id（非报警对象）
    
    @Column(name = "am_monitortype", length = 32)
    private String monitorType;				//监控（对象）类型；	RDS1.0&网络1.3版本由原来的存储中文改为nodeId
    
    @Column(name = "am_alarmtype", length = 32)
    private String alarmType;				//报警类型；		RDS1.0&网络1.3版本由原来的存储中文改为nodeId
    
    @Column(name = "am_detail", length = 500)
    private String detail;					//报警详情
    
    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = "am_time")
    private Date time;						//报警时间
    
    @Column(name = "am_isprocessed", length = 1)
    private String isProcessed;				//报警标识（是否处理）
    
    @Column(name = "am_monitoralarmitemid", length = 32)
    private String monitorAlarmItemId;		//监控报警项ID
    
    @Column(name = "am_alarmruleid", length=32)
    private String alarmRuleId;				//报警规则id
    
    @Column(name = "cus_id", length=32)
    private String cusId;					//客户id
    
    @Column(name = "prj_id", length=32)
    private String prjId;					//项目id
    
    @Column(name = "dc_id", length=32)
    private String dcId;					//数据中心

    /*****************API报警版本新增*************/
	@Column(name = "ip", length = 32)
	private String ip ;						//客户访问映射的客户端IP地址

	@Column(name = "am_alarmtriggerid", length = 32)
	private String am_alarmtriggerid ;		//报警对应的触发条件信息ID
	/*****************API报警版本新增*************/

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getAm_alarmtriggerid() {
		return am_alarmtriggerid;
	}

	public void setAm_alarmtriggerid(String am_alarmtriggerid) {
		this.am_alarmtriggerid = am_alarmtriggerid;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getObjId() {
		return objId;
	}

	public void setObjId(String objId) {
		this.objId = objId;
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

	public String getCusId() {
		return cusId;
	}

	public void setCusId(String cusId) {
		this.cusId = cusId;
	}

	public String getPrjId() {
		return prjId;
	}

	public void setPrjId(String prjId) {
		this.prjId = prjId;
	}

	public String getDcId() {
		return dcId;
	}

	public void setDcId(String dcId) {
		this.dcId = dcId;
	}

	@Override
	public String toString() {
		return "BaseEcmcAlarmMessage{" +
				"id='" + id + '\'' +
				", objId='" + objId + '\'' +
				", monitorType='" + monitorType + '\'' +
				", alarmType='" + alarmType + '\'' +
				", detail='" + detail + '\'' +
				", time=" + time +
				", isProcessed='" + isProcessed + '\'' +
				", monitorAlarmItemId='" + monitorAlarmItemId + '\'' +
				", alarmRuleId='" + alarmRuleId + '\'' +
				", cusId='" + cusId + '\'' +
				", prjId='" + prjId + '\'' +
				", dcId='" + dcId + '\'' +
				", ip='" + ip + '\'' +
				", am_alarmtriggerid='" + am_alarmtriggerid + '\'' +
				'}';
	}
}

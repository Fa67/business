package com.eayun.monitor.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

/**
 * 运维报警对象
 *                       
 * @Filename: BaseEcmcAlarmobject.java
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
@Table(name = "ecmc_alarmobject")
public class BaseEcmcAlarmObject implements Serializable {

	private static final long serialVersionUID = -3481819385568134003L;
	
	@Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "ao_id", unique = true, nullable = false, length = 32)
    private String id;
    
	@Column(name = "ao_alarmruleid", length = 32)
    private String alarmRuleId;				//规则id
    
	@Column(name = "ao_type", length = 32)
    private String aoType;					//对象类型
    
	@Column(name = "ao_objectid", length = 36)
    private String aoObjectId;				//对象资源id
    
    @Column(name = "cus_id", length=32)
    private String cusId;					//客户id
    
    @Column(name = "prj_id", length=32)
    private String prjId;					//项目id
    
    @Column(name = "dc_id", length=32)
    private String dcId;					//数据中心
    
    
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

	public String getAoType() {
		return aoType;
	}

	public void setAoType(String aoType) {
		this.aoType = aoType;
	}

	public String getAoObjectId() {
		return aoObjectId;
	}

	public void setAoObjectId(String aoObjectId) {
		this.aoObjectId = aoObjectId;
	}
}

package com.eayun.physical.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * DcSwitch entity. @author MyEclipse Persistence Tools
 */
@Entity
@Table(name = "dc_switch")
public class BaseDcSwitch  implements
		java.io.Serializable {

	// Fields

	/**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -1923366154068160057L;
    private String id;
	private String name;
	private String switchModel;
	private String ipAddress;
	private Double portCapacity;
	private String interfaceModel;
	private String forwardFunc;
	private String dataCenterId;
	private String cabinetId;
	private Double spec;
	private String creUser;
    @JSONField (format="yyyy-MM-dd HH:mm:ss")
	private Timestamp creDate;
	private String responPerson;
	private String memo;
	private String switchId;
	private String responPersonMobile;

	// Constructors

	/** default constructor */
	public BaseDcSwitch() {
	}

	/** minimal constructor */
	public BaseDcSwitch(String id) {
		this.id = id;
	}

	/** full constructor */
	public BaseDcSwitch(String id, String name, String switchModel,
			String ipAddress, Double portCapacity, String interfaceModel,
			String forwardFunc, String dataCenterId, String cabinetId,
			Double spec, String creUser, Timestamp creDate,
			String responPerson, String memo, String switchId,
			String responPersonMobile) {
		this.id = id;
		this.name = name;
		this.switchModel = switchModel;
		this.ipAddress = ipAddress;
		this.portCapacity = portCapacity;
		this.interfaceModel = interfaceModel;
		this.forwardFunc = forwardFunc;
		this.dataCenterId = dataCenterId;
		this.cabinetId = cabinetId;
		this.spec = spec;
		this.creUser = creUser;
		this.creDate = creDate;
		this.responPerson = responPerson;
		this.memo = memo;
		this.switchId = switchId;
		this.responPersonMobile = responPersonMobile;
	}

	// Property accessors
	@Id
	@Column(name = "ID", unique = true, nullable = false, length = 50)
	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(name = "NAME", length = 50)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "SWITCH_MODEL", length = 50)
	public String getSwitchModel() {
		return this.switchModel;
	}

	public void setSwitchModel(String switchModel) {
		this.switchModel = switchModel;
	}

	@Column(name = "IP_ADDRESS", length = 50)
	public String getIpAddress() {
		return this.ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	@Column(name = "PORT_CAPACITY", precision = 0)
	public Double getPortCapacity() {
		return this.portCapacity;
	}

	public void setPortCapacity(Double portCapacity) {
		this.portCapacity = portCapacity;
	}

	@Column(name = "INTERFACE_MODEL", length = 50)
	public String getInterfaceModel() {
		return this.interfaceModel;
	}

	public void setInterfaceModel(String interfaceModel) {
		this.interfaceModel = interfaceModel;
	}

	@Column(name = "FORWARD_FUNC", length = 50)
	public String getForwardFunc() {
		return this.forwardFunc;
	}

	public void setForwardFunc(String forwardFunc) {
		this.forwardFunc = forwardFunc;
	}

	@Column(name = "DATA_CENTER_ID", length = 50)
	public String getDataCenterId() {
		return this.dataCenterId;
	}

	public void setDataCenterId(String dataCenterId) {
		this.dataCenterId = dataCenterId;
	}

	@Column(name = "CABINET_ID", length = 50)
	public String getCabinetId() {
		return this.cabinetId;
	}

	public void setCabinetId(String cabinetId) {
		this.cabinetId = cabinetId;
	}

	@Column(name = "SPEC", precision = 0)
	public Double getSpec() {
		return this.spec;
	}

	public void setSpec(Double spec) {
		this.spec = spec;
	}

	@Column(name = "CRE_USER", length = 50)
	public String getCreUser() {
		return this.creUser;
	}

	public void setCreUser(String creUser) {
		this.creUser = creUser;
	}

	@Column(name = "CRE_DATE", length = 11)
	public Timestamp getCreDate() {
		return this.creDate;
	}

	public void setCreDate(Timestamp creDate) {
		this.creDate = creDate;
	}

	@Column(name = "RESPON_PERSON", length = 50)
	public String getResponPerson() {
		return this.responPerson;
	}

	public void setResponPerson(String responPerson) {
		this.responPerson = responPerson;
	}

	@Column(name = "MEMO", length = 1000)
	public String getMemo() {
		return this.memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	@Column(name = "SWITCH_ID", length = 50)
	public String getSwitchId() {
		return this.switchId;
	}

	public void setSwitchId(String switchId) {
		this.switchId = switchId;
	}

	@Column(name = "RESPON_PERSON_MOBILE", length = 50)
	public String getResponPersonMobile() {
		return this.responPersonMobile;
	}

	public void setResponPersonMobile(String responPersonMobile) {
		this.responPersonMobile = responPersonMobile;
	}

}
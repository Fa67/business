package com.eayun.physical.model;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * DcCabinet entity. @author MyEclipse Persistence Tools
 */
@Entity
@Table(name = "dc_cabinet")
public class BaseDcCabinet  implements
		java.io.Serializable {

	// Fields

	/**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -9204047362716462432L;
    private String id;
	private String name;
	private Integer totalCapacity;//机柜总容量（U）
	private Integer usedCapacity;//机柜已使用容量（U）
	private String dataCenterId;//数据中心ID
	private String creUser;
	private Timestamp creDate;
	private String cabinetId;//机柜编号
	private String memo;

	// Constructors

	/** default constructor */
	public BaseDcCabinet() {
	}

	/** minimal constructor */
	public BaseDcCabinet(String id) {
		this.id = id;
	}

	/** full constructor */
	public BaseDcCabinet(String id, String name, Integer totalCapacity,
			Integer usedCapacity, String dataCenterId, String creUser,
			Timestamp creDate, String cabinetId, String memo) {
		this.id = id;
		this.name = name;
		this.totalCapacity = totalCapacity;
		this.usedCapacity = usedCapacity;
		this.dataCenterId = dataCenterId;
		this.creUser = creUser;
		this.creDate = creDate;
		this.cabinetId = cabinetId;
		this.memo = memo;
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

	@Column(name = "TOTAL_CAPACITY", precision = 0)
	public Integer getTotalCapacity() {
		return this.totalCapacity;
	}

	public void setTotalCapacity(Integer totalCapacity) {
		this.totalCapacity = totalCapacity;
	}

	@Column(name = "USED_CAPACITY", precision = 0)
	public Integer getUsedCapacity() {
		return this.usedCapacity;
	}

	public void setUsedCapacity(Integer usedCapacity) {
		this.usedCapacity = usedCapacity;
	}

	@Column(name = "DATA_CENTER_ID", length = 50)
	public String getDataCenterId() {
		return this.dataCenterId;
	}

	public void setDataCenterId(String dataCenterId) {
		this.dataCenterId = dataCenterId;
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

	@Column(name = "CABINET_ID", length = 50)
	public String getCabinetId() {
		return this.cabinetId;
	}

	public void setCabinetId(String cabinetId) {
		this.cabinetId = cabinetId;
	}

	@Column(name = "MEMO", length = 1000)
	public String getMemo() {
		return this.memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

}
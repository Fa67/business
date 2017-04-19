package com.eayun.physical.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "dc_cabinet_rf")
public class BaseDcCabinetRf {
	
	@GenericGenerator(name="generator", strategy="uuid.hex")
    @Id @GeneratedValue(generator="generator")
    @Column(name="ID", unique=true, nullable=false, length=50)
	private String id;//id
	
	@Column(name="CABINET_ID", length=50)
	private String cabinetId;//机柜id
	
	@Column(name="RE_ID", length=50)
	private String reId;//资源id
	
	@Column(name="RE_TYPE", length=10)
	private String reType;//资源类型（0:服务器/1:防火墙/2:存储/3:交换机）
	
	@Column(name="LOCATION", length=4)
	private Integer location;//占用机柜位置
	
	@Column(name="FLAG",  length=10)
	private String flag;//是否占用（0：未使用，1：已使用）
	
	@Column(name="DATA_CENTER_ID")
	private String data_center_id;//数据中心ID
	
	
	public String getData_center_id() {
		return data_center_id;
	}

	public void setData_center_id(String data_center_id) {
		this.data_center_id = data_center_id;
	}

	public BaseDcCabinetRf() {
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCabinetId() {
		return cabinetId;
	}

	public void setCabinetId(String cabinetId) {
		this.cabinetId = cabinetId;
	}

	public String getReId() {
		return reId;
	}

	public void setReId(String reId) {
		this.reId = reId;
	}

	public String getReType() {
		return reType;
	}

	public void setReType(String reType) {
		this.reType = reType;
	}

	public Integer getLocation() {
		return location;
	}

	public void setLocation(Integer location) {
		this.location = location;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

}

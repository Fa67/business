package com.eayun.unit.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2016年12月21日
 */
@Entity
@Table(name="cloud_area")
public class BaseCloudArea implements Serializable{

	@Id
	@Column(name="code",length=10,unique=true,nullable=false)
	private String code;                  //id
	
	@Column(name="parentcode",length=10)
	private String parentCode;            //父级ID
	
	@Column(name="cityname",length=64)
	private String cityName;              //城市名称

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getParentCode() {
		return parentCode;
	}

	public void setParentCode(String parentCode) {
		this.parentCode = parentCode;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	
	
}

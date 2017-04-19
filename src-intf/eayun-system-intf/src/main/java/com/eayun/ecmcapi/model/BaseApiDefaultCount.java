package com.eayun.ecmcapi.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "api_defaultcount")
public class BaseApiDefaultCount implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name = "dc_id", unique = true, nullable = false, length = 100)
	private String id;
	@Column(name = "dc_action")
	private String action;
	@Column(name = "dc_apitype")
	private String apiType;
	@Column(name = "dc_count")
	private int count;
	@Column(name = "dc_version")
	private String version;
	@Column(name = "dc_apitypename")
	private String apiTypeName;
	@Column(name = "dc_actionname")
	private String actionName;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getApiType() {
		return apiType;
	}
	public void setApiType(String apiType) {
		this.apiType = apiType;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getApiTypeName() {
		return apiTypeName;
	}
	public void setApiTypeName(String apiTypeName) {
		this.apiTypeName = apiTypeName;
	}
	public String getActionName() {
		return actionName;
	}
	public void setActionName(String actionName) {
		this.actionName = actionName;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	
	
}

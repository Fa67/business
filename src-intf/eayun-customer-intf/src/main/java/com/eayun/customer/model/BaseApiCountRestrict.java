package com.eayun.customer.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "api_countrestrict")
public class BaseApiCountRestrict implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name = "cr_id", unique = true, nullable = false, length = 100)
	private String id;
	@Column(name = "cr_cusid")
	private String cusId;
	@Column(name = "cr_version")
	private String version;
	@Column(name = "cr_action")
	private String action;
	@Column(name = "cr_count")
	private int count;
	@Column(name = "cr_apitype")
	private String apiType;
	@Column(name = "cr_actionname")
	private String actionName;
	@Column(name = "cr_apitypename")
	private String apiTypeName;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getCusId() {
		return cusId;
	}
	public void setCusId(String cusId) {
		this.cusId = cusId;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public String getApiType() {
		return apiType;
	}
	public void setApiType(String apiType) {
		this.apiType = apiType;
	}
	public String getActionName() {
		return actionName;
	}
	public void setActionName(String actionName) {
		this.actionName = actionName;
	}
	public String getApiTypeName() {
		return apiTypeName;
	}
	public void setApiTypeName(String apiTypeName) {
		this.apiTypeName = apiTypeName;
	}
	
	
}

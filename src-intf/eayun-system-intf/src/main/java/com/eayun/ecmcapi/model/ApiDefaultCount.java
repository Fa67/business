package com.eayun.ecmcapi.model;

public class ApiDefaultCount extends BaseApiDefaultCount{
	private String apiTypeName;
	private String actionName;
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
	
}

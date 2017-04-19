package com.eayun.database.log.model;

public class CloudRDSLog extends BaseCloudRdsLog{
	private double unitSize;
	private String unitType;
	private String rdsName;
	private int publishFileCount;
	
	public double getUnitSize() {
		return unitSize;
	}
	public void setUnitSize(double unitSize) {
		this.unitSize = unitSize;
	}
	public String getUnitType() {
		return unitType;
	}
	public void setUnitType(String unitType) {
		this.unitType = unitType;
	}
	public String getRdsName() {
		return rdsName;
	}
	public void setRdsName(String rdsName) {
		this.rdsName = rdsName;
	}
	public int getPublishFileCount() {
		return publishFileCount;
	}
	public void setPublishFileCount(int publishFileCount) {
		this.publishFileCount = publishFileCount;
	}
}

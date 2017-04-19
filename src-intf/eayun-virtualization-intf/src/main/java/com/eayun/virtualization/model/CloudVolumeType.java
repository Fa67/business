package com.eayun.virtualization.model;

public class CloudVolumeType extends BaseCloudVolumeType {

	private static final long serialVersionUID = 1L;
	
	private String dcName;//数据中心名称
	private String volumeTypeAs;//中文名称

	public String getDcName() {
		return dcName;
	}

	public void setDcName(String dcName) {
		this.dcName = dcName;
	}

	public String getVolumeTypeAs() {
		return volumeTypeAs;
	}

	public void setVolumeTypeAs(String volumeTypeAs) {
		this.volumeTypeAs = volumeTypeAs;
	}
	
	
	
	
	

}

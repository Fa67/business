package com.eayun.obs.model;

public class ObsStorage {
	private String obsName;
	private String bucketName;
	private String obsShowName;
	private long size;
	private String type;
	private boolean isFolder;
	private String createTime;
	public String getObsName() {
		return obsName;
	}
	public void setObsName(String obsName) {
		this.obsName = obsName;
	}
	public String getBucketName() {
		return bucketName;
	}
	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}
	public String getObsShowName() {
		return obsShowName;
	}
	public void setObsShowName(String obsShowName) {
		this.obsShowName = obsShowName;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public boolean isFolder() {
		return isFolder;
	}
	public void setFolder(boolean isFolder) {
		this.isFolder = isFolder;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	
}

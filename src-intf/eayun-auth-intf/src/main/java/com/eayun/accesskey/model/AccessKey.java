package com.eayun.accesskey.model;

public class AccessKey extends BaseAccessKey {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String creDate;		//创建时间
	private int count;		//已创建的非默认ak数量
	
	
	
	
	
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public String getCreDate() {
		return creDate;
	}
	public void setCreDate(String creDate) {
		this.creDate = creDate;
	}
	
}

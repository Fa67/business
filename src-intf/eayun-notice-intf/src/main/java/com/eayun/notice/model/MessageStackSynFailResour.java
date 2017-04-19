package com.eayun.notice.model;

import java.util.Date;

public class MessageStackSynFailResour {
	
	
	private String resourtype;//资源类型
	private String resourName;//资源名称
	private String resourID;//资源id
	private Date synTime;//同步时间
	
	public String getResourtype() {
		return resourtype;
	}
	public void setResourtype(String resourtype) {
		this.resourtype = resourtype;
	}
	public String getResourName() {
		return resourName;
	}
	public void setResourName(String resourName) {
		this.resourName = resourName;
	}
	public String getResourID() {
		return resourID;
	}
	public void setResourID(String resourID) {
		this.resourID = resourID;
	}
	public Date getSynTime() {
		return synTime;
	}
	public void setSynTime(Date synTime) {
		this.synTime = synTime;
	}

}

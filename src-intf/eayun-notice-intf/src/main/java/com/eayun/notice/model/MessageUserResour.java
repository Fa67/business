package com.eayun.notice.model;

import java.math.BigDecimal;
import java.util.Date;

public class MessageUserResour {
	private String cusName;//客户名称
	
	private  Date recoveryTime;//最近充值时间
	private String resourType;//资源类型
	private String resourname;//资源恢复异常的名称
	private String resourId;//资源ID
	public String getCusName() {
		return cusName;
	}
	public void setCusName(String cusName) {
		this.cusName = cusName;
	}
	public Date getRecoveryTime() {
		return recoveryTime;
	}
	public void setRecoveryTime(Date recoveryTime) {
		this.recoveryTime = recoveryTime;
	}
	public String getResourType() {
		return resourType;
	}
	public void setResourType(String resourType) {
		this.resourType = resourType;
	}
	public String getResourname() {
		return resourname;
	}
	public void setResourname(String resourname) {
		this.resourname = resourname;
	}
	public String getResourId() {
		return resourId;
	}
	public void setResourId(String resourId) {
		this.resourId = resourId;
	}
	
}

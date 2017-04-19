package com.eayun.syssetup.bean;

import com.eayun.common.tools.ExcelTitle;

public class CusOLExcel {

	@ExcelTitle(name="客户名称")
    private String cusName;
    
    @ExcelTitle(name="用户名")
    private String cusAccount;
    
    @ExcelTitle(name="IP")
    private String ip;
    
    @ExcelTitle(name="登录地址")
    private String loginAddr;
    
    @ExcelTitle(name="登录时间")
    private String  loginTime;

    @ExcelTitle(name="最后操作时间")
    private String  lastOpTime;

	public String getCusName() {
		return cusName;
	}

	public void setCusName(String cusName) {
		this.cusName = cusName;
	}

	public String getCusAccount() {
		return cusAccount;
	}

	public void setCusAccount(String cusAccount) {
		this.cusAccount = cusAccount;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getLoginAddr() {
		return loginAddr;
	}

	public void setLoginAddr(String loginAddr) {
		this.loginAddr = loginAddr;
	}

	public String getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(String loginTime) {
		this.loginTime = loginTime;
	}

	public String getLastOpTime() {
		return lastOpTime;
	}

	public void setLastOpTime(String lastOpTime) {
		this.lastOpTime = lastOpTime;
	}
    
}

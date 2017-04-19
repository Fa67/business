package com.eayun.syssetup.bean;

import java.io.Serializable;
import java.util.Date;

import com.eayun.common.model.IpInfo;

/**
 * ECSC在线人数统计
 *                       
 * @Filename: CusOLStatistics.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2016年8月11日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class CusOLStatistics implements Serializable {

	/**
	 *Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

	private String cusId;			//登录客户id
	
	private String cusName;		//登录客户名称
	
	private String cusAccount;		//登录客户账号
	
	private Date loginTime;			//登录时间
	
	private Date lastOpTime;		//最后操作时间
	
	private IpInfo ipInfo;			//登录ip信息
	
	private String ip;				//登录ip
	
	private String loginAddr;		//登录地址

	public String getCusId() {
		return cusId;
	}

	public void setCusId(String cusId) {
		this.cusId = cusId;
	}

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

	public Date getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(Date loginTime) {
		this.loginTime = loginTime;
	}

	public Date getLastOpTime() {
		return lastOpTime;
	}

	public void setLastOpTime(Date lastOpTime) {
		this.lastOpTime = lastOpTime;
	}

	public IpInfo getIpInfo() {
		return ipInfo;
	}

	public void setIpInfo(IpInfo ipInfo) {
		this.ipInfo = ipInfo;
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
	
	
}

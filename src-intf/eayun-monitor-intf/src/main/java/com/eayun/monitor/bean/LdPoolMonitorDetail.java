package com.eayun.monitor.bean;

import java.util.Date;

/**
 * 负载均衡mongo及redis指标对应model
 *                       
 * @Filename: LdPoolMonitorDetail.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2017年3月2日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class LdPoolMonitorDetail {

	private Date timestamp;		//记录时间		（计入redis时无需赋值）
	
	private Date realTime;		//数据实例插入时间（计入redis时无需赋值）
	
	private String ldPoolId;    //负载均衡id	（计入redis时无需赋值）
	
	private String mode; 		//负载均衡模式，0：普通；1：主备
	
	private String projectId;    //项目id		（计入redis时无需赋值）
	
	private int member;   		//成员数量
    
    private int masterMember;  	//主节点个数
    
    private int slaveMember;  	//从节点个数
    
    private int expMember; 		//不活跃节点个数
    
    private int expMaster;   	//不活跃主节点个数
    
    private int expSalve;      	//不活跃从节点个数
    
    private double expMemberRatio;   //不活跃节点百分比（分母为0时这里记为0）
    
    private double expMasterRatio;    //不活跃主节点百分比（分母为0时这里记为0）
    
    private double expSalveRatio;    //不活跃从节点百分比（分母为0时这里记为0）

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getLdPoolId() {
		return ldPoolId;
	}

	public void setLdPoolId(String ldPoolId) {
		this.ldPoolId = ldPoolId;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public int getMember() {
		return member;
	}

	public void setMember(int member) {
		this.member = member;
	}

	public int getMasterMember() {
		return masterMember;
	}

	public void setMasterMember(int masterMember) {
		this.masterMember = masterMember;
	}

	public int getSlaveMember() {
		return slaveMember;
	}

	public void setSlaveMember(int slaveMember) {
		this.slaveMember = slaveMember;
	}

	public int getExpMember() {
		return expMember;
	}

	public void setExpMember(int expMember) {
		this.expMember = expMember;
	}

	public int getExpMaster() {
		return expMaster;
	}

	public void setExpMaster(int expMaster) {
		this.expMaster = expMaster;
	}

	public int getExpSalve() {
		return expSalve;
	}

	public void setExpSalve(int expSalve) {
		this.expSalve = expSalve;
	}

	public double getExpMemberRatio() {
		return expMemberRatio;
	}

	public void setExpMemberRatio(double expMemberRatio) {
		this.expMemberRatio = expMemberRatio;
	}

	public double getExpMasterRatio() {
		return expMasterRatio;
	}

	public void setExpMasterRatio(double expMasterRatio) {
		this.expMasterRatio = expMasterRatio;
	}

	public double getExpSalveRatio() {
		return expSalveRatio;
	}

	public void setExpSalveRatio(double expSalveRatio) {
		this.expSalveRatio = expSalveRatio;
	}

	public Date getRealTime() {
		return realTime;
	}

	public void setRealTime(Date realTime) {
		this.realTime = realTime;
	}
    
    
}
